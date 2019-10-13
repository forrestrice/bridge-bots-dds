package com.bridgebots.dds;


import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AlphaBetaSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private long nodesUsed = 0;

    @Override
    public int solve(Deal deal, TrumpSuit trumpSuit, Direction declarer) {
        nodesUsed = 0;
        Board board = Board.forDeal(deal, trumpSuit, declarer.next());
        Stopwatch timer = Stopwatch.createStarted();
        int result = minMax(board, 0, -1, 14);
        LOG.info("Computed minMax result {} with {} nodes in {}", result, nodesUsed, timer.stop());
        return result;
    }

    private int minMax(Board board, int depth, int alpha, int beta) {
        nodesUsed++;
        if(depth < 3){
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed);
        }
        if (board.nextPlays().isEmpty()) {
            return board.getDeclarerTricks();
        }
        if (board.offenseOnLead()) {
            int value = -1;
            for (Card card : board.nextPlays()) {
                board.makePlay(card);

                value = Math.max(value, minMax(board, depth +1, alpha, beta));
                board.undoPlay();
                alpha = Math.max(alpha, value);
                if (alpha >= beta){
                    break;
                }
            }
            return value;
        } else {
            int value = 14;
            for (Card card : board.nextPlays()) {
                board.makePlay(card);
                value = Math.min(value, minMax(board, depth + 1, alpha, beta));
                board.undoPlay();
                beta = Math.min(beta, value);
                if (alpha >= beta){
                    break;
                }
            }
            return value;
        }
    }
}
