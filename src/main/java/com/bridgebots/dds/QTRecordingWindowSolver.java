package com.bridgebots.dds;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class QTRecordingWindowSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private long nodesUsed = 0;
    private long hitsWithoutTermination = 0;
    private final Function<Board, List<Card>> cardSelectionFunction;
    private final TranspositionTable<AlphaBetaResult> transpositionTable = new TranspositionTable<>();
    private final RecordingQuicktricksEvaluator quicktricksEvaluator;
    private boolean enableQTPruning;

    public QTRecordingWindowSolver() {
        this(Board::restrictedNextPlays, false);
    }

    public QTRecordingWindowSolver(Function<Board, List<Card>> cardSelectionFunction, boolean enableQTPruning) {
        this.cardSelectionFunction = cardSelectionFunction;
        quicktricksEvaluator = new RecordingQuicktricksEvaluator(cardSelectionFunction);
        this.enableQTPruning = enableQTPruning;
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

        LOG.info("QuicktricksEvaluator Stats: nodes {}", quicktricksEvaluator.getNodesUsed());
        TranspositionTable<AlphaBetaResult> qtTranspositionTable = quicktricksEvaluator.getTranspositionTable();
        LOG.info("QuicktricksEvaluator TranspositionTable stats: {} entries, {} queries, {} hits, {} hitrate, {} hitsWithoutTermination",
                qtTranspositionTable.keyCount(),
                qtTranspositionTable.getQueryCount(),
                qtTranspositionTable.getHitCount(),
                (float) qtTranspositionTable.getHitCount() / qtTranspositionTable.getQueryCount(),
                quicktricksEvaluator.getHitsWithoutTermination()
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

        //Check quicktricks for leader to see if they can resolve the bounds
        if (board.getCurrentTrick().isEmpty()) {
            if (board.offenseOnLead()){
                int offenseQtResult = quicktricksEvaluator.leaderQuicktricks(board, beta);
                if (offenseQtResult >= beta){
                    writeToTranspositionTable(cachedResult, Collections.emptyMap(), initialAlpha, initialBeta, offenseQtResult, board);
                    return offenseQtResult;
                } else if (enableQTPruning){
                    alpha = Math.max(alpha, offenseQtResult);
                }
            } else {
                int defenseQtTarget = board.getTricksAvailable() - beta + 1;
                int defenseQtResult = quicktricksEvaluator.leaderQuicktricks(board, defenseQtTarget);
                if (defenseQtResult >= defenseQtTarget){
                    int alphaBetaResult = board.getTricksAvailable() - defenseQtResult;
                    writeToTranspositionTable(cachedResult, Collections.emptyMap(), initialAlpha, initialBeta, alphaBetaResult, board);
                    return alphaBetaResult;
                } else if (enableQTPruning){
                    beta = Math.min(beta, board.getTricksAvailable() - defenseQtResult);
                }
            }
        }

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
            writeToTranspositionTable(cachedResult, cardValues, initialAlpha, initialBeta, value, board);
        }
        return value;
    }

    void writeToTranspositionTable(AlphaBetaResult cachedResult, Map<Card, Integer> cardValues, int initialAlpha, int initialBeta, int value, Board board){
        AlphaBetaResult alphaBetaResult = new AlphaBetaResult(cardValues, initialAlpha, initialBeta, value, null);
        if (cachedResult != null) {
            alphaBetaResult = AlphaBetaResult.merge(alphaBetaResult, cachedResult);
        }
        transpositionTable.put(board, alphaBetaResult);

    }
}
