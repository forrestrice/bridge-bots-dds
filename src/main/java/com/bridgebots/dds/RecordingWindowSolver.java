package com.bridgebots.dds;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RecordingWindowSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private long nodesUsed = 0;
    private long hitsWithoutTermination = 0;
    private final Function<Board, List<Card>> cardSelectionFunction;
    private final TranspositionTable<AlphaBetaResult> transpositionTable = new TranspositionTable<>();

    public RecordingWindowSolver() {
        this(Board::restrictedNextPlays);
    }

    public RecordingWindowSolver(Function<Board, List<Card>> cardSelectionFunction) {
        this.cardSelectionFunction = cardSelectionFunction;
    }

    @Override
    public int solve(Deal deal, TrumpSuit trumpSuit, Direction declarer) {
        nodesUsed = 0;
        Board board = Board.forDeal(deal, trumpSuit, declarer.next());
        Stopwatch timer = Stopwatch.createStarted();

        int upperBound = board.getTricksAvailable();
        int lowerBound = 0;
        while (lowerBound != upperBound) {
            int target = chooseTarget(lowerBound, upperBound);
            LOG.info("Window solving for lowerBound {}, upperBound {}, target {}", lowerBound, upperBound, target);
            int windowResult = alphaBetaSolve(board, 0, target - 1, target);
            if (windowResult >= target) {
                lowerBound = target;
            } else {
                upperBound = target - 1;
            }
        }
        LOG.info("Computed window result {} with {} nodes in {}", lowerBound, nodesUsed, timer.stop());
        LOG.info("TranspositionTable stats: {} entries, {} queries, {} hits, {} hitrate, {} hitsWithoutTermination",
                transpositionTable.keyCount(),
                transpositionTable.getQueryCount(),
                transpositionTable.getHitCount(),
                (float) transpositionTable.getHitCount() / transpositionTable.getQueryCount(),
                hitsWithoutTermination
        );
        return lowerBound;
    }

    private int chooseTarget(int lowerBound, int upperBound) {
        return lowerBound + Math.max(1, (upperBound - lowerBound) / 2);
    }

    private int alphaBetaSolve(Board board, int depth, int alpha, int beta) {
        nodesUsed++;
        if (depth < 3) {
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed);
        }
        if (board.getCardsRemaining() == 0) {
            return 0;
        }

        //The maximizing player can always achieve at least 0 and can never achieve more than the tricks remaining
        if (beta <= 0 || board.getTricksAvailable() < beta) {
            return 0;
        }

        AlphaBetaResult cachedResult = null;
        if (board.getCurrentTrick().isEmpty()) {
            cachedResult = transpositionTable.get(board);
            if (cachedResult != null) {
                // Upper bound on declarer tricks remaining is alpha
                if (cachedResult.upperBound <= alpha) {
                    return cachedResult.upperBound;
                }
                if (cachedResult.lowerBound >= beta) {
                    return cachedResult.lowerBound;
                }
            }
        }

        if (cachedResult != null) {
            hitsWithoutTermination++;
        }

        int initialAlpha = alpha;
        int initialBeta = beta;
        Map<Card, Integer> cardValues = new HashMap<>();
        int value = board.offenseOnLead() ? -1 : 14;

        for (Card card : cardSelectionFunction.apply(board)) {
            board.makePlay(card);
            int currentTrickValue = board.getCurrentTrick().isEmpty() && board.offenseOnLead() ? 1 : 0;
            int cardResult = currentTrickValue + alphaBetaSolve(board, depth + 1, alpha - currentTrickValue, beta - currentTrickValue);
            board.undoPlay();
            cardValues.put(card, cardResult);
            if (board.offenseOnLead()) {
                value = Math.max(value, cardResult);
                alpha = Math.max(alpha, value);
            } else {
                value = Math.min(value, cardResult);
                beta = Math.min(beta, value);
            }
            if (alpha >= beta) {
                break;
            }
        }

        if (board.getCurrentTrick().isEmpty()) {
            AlphaBetaResult alphaBetaResult = new AlphaBetaResult(cardValues, initialAlpha, initialBeta, value, null);
            if (cachedResult != null) {
                alphaBetaResult = AlphaBetaResult.merge(alphaBetaResult, cachedResult);
            }
            //transpositionTable.merge(board, alphaBetaResult);
            transpositionTable.put(board, alphaBetaResult);
        }
        return value;
    }


    private static class AlphaBetaResult {
        final Board board;
        final int upperBound;
        final int lowerBound;
        final Map<Card, Integer> cardValues;

        AlphaBetaResult(Map<Card, Integer> cardValues, int alpha, int beta, int result, Board board) {
            if (result <= alpha) {
                upperBound = alpha;
                lowerBound = 0;
            }
            // result >= beta is guaranteed because we are zero window solving
            else {
                lowerBound = beta;
                upperBound = 14;
            }
            this.cardValues = cardValues;
            this.board = board;
        }

        AlphaBetaResult(Map<Card, Integer> cardValues, int upperBound, int lowerBound, Board board) {
            this.cardValues = cardValues;
            this.board = board;
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        public static AlphaBetaResult merge(AlphaBetaResult first, AlphaBetaResult second) {
            //TODO combine card values
            return new AlphaBetaResult(
                    first.cardValues,
                    Math.min(first.upperBound, second.upperBound),
                    Math.max(first.lowerBound, second.lowerBound),
                    first.board);
        }
    }
}
