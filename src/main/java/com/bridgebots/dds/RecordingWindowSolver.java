package com.bridgebots.dds;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RecordingWindowSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private long nodesUsed = 0;
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
        LOG.info("TranspositionTable stats: {} entries, {} queries, {} hits, {} hitrate",
                transpositionTable.keyCount(),
                transpositionTable.getQueryCount(),
                transpositionTable.getHitCount(),
                (float) transpositionTable.getHitCount() / transpositionTable.getQueryCount()
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
        /* wrong
        if(board.getDeclarerTricks() > alpha || alpha > board.getDeclarerTricks() + board.getTricksAvailable()){
            return 0;
        }*/
        if(beta <= 0 || board.getTricksAvailable() < beta){
            return 0;
        }


        if (board.getCurrentTrick().isEmpty()) {
            AlphaBetaResult cachedResult = transpositionTable.get(board);
            if (cachedResult != null) {
                /*
                if (cachedResult.board != board) {
                    LOG.debug("mismatched boards in TT HIT");
                    LOG.debug("----CURRENT BOARD----");
                    LOG.debug(board.toLogString());
                    LOG.debug("----CACHED BOARD----");
                    LOG.debug(cachedResult.board.toLogString());
                    LOG.debug("--------------------");
                }*/
                //LOG.debug("TT hit at depth = {}, alpha={}, beta={}", depth, alpha, beta);
                //If searching same alpha and beta, safe to return result
                /*
                if (cachedResult.alpha == alpha && cachedResult.beta == beta) {
                    return cachedResult.result;
                }*/
                /*
                if (cachedResult.result >= beta){
                    return cachedResult.result;
                }*/
                /*
                if (cachedResult.alpha == alpha && cachedResult.beta == beta) {
                    return cachedResult.result;
                } else {
                    LOG.debug("mismatched alpha/beta={}/{}, cached alpha/beta={}/{}, results={}, declarerTricks={}",
                            alpha, beta, cachedResult.alpha, cachedResult.beta, cachedResult.cardValues, board.getDeclarerTricks());
                }*/
            }
        }
        int initialAlpha = alpha;
        int initialBeta = beta;
        Map<Card, Integer> cardValues = new HashMap<>();
        int value;
        if (board.offenseOnLead()) {
            value = -1;
            for (Card card : cardSelectionFunction.apply(board)) {
                board.makePlay(card);
                int currentTrickValue = 0;
                if (board.getCurrentTrick().isEmpty() && board.offenseOnLead()) {
                    currentTrickValue = 1;
                }
                int cardResult = currentTrickValue + alphaBetaSolve(board, depth + 1, alpha - currentTrickValue, beta - currentTrickValue);
                board.undoPlay();
                cardValues.put(card, cardResult);
                value = Math.max(value, cardResult);
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            //LOG.debug("offense returns value={}, alpha={}, beta={}", value, alpha, beta);
        } else {
            value = 14;
            for (Card card : cardSelectionFunction.apply(board)) {
                board.makePlay(card);
                int currentTrickValue = 0;
                if (board.getCurrentTrick().isEmpty() && board.offenseOnLead()) {
                    currentTrickValue = 1;
                }
                int cardResult = currentTrickValue + alphaBetaSolve(board, depth + 1, alpha - currentTrickValue, beta - currentTrickValue);
                board.undoPlay();
                value = Math.min(value, cardResult);
                cardValues.put(card, cardResult);
                beta = Math.min(beta, value);
                if (alpha >= beta) {
                    break;
                }
            }
            //LOG.debug("defense returns value={}, alpha={}, beta={}", value, alpha, beta);
        }
        if (board.getCurrentTrick().isEmpty()) {
            AlphaBetaResult alphaBetaResult = new AlphaBetaResult(cardValues, initialAlpha, initialBeta, value, null);
            transpositionTable.put(board, alphaBetaResult);
        }
        return value;
    }


    private static class AlphaBetaResult {
        final Map<Card, Integer> cardValues;
        final int alpha;
        final int beta;
        final int result;
        final Board board;

        AlphaBetaResult(Map<Card, Integer> cardValues, int alpha, int beta, int result, Board board) {
            this.cardValues = cardValues;
            this.alpha = alpha;
            this.beta = beta;
            this.result = result;
            this.board = board;
        }
    }
}
