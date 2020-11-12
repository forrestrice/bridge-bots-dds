package com.bridgebots.dds;


import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleSolver {
    private static final Logger LOG = LogManager.getLogger();
    private long nodesUsed = 0;

    public int solve(Deal deal, TrumpSuit trumpSuit, Direction declarer) {
        nodesUsed = 0;
        Board board = Board.forDeal(deal, trumpSuit, declarer.next());
        Stopwatch timer = Stopwatch.createStarted();
        int result = minMax(board, 0);
        LOG.info("Computed minMax result {} with {} nodes in {}", result, nodesUsed, timer.stop());
        return result;
    }

    private int minMax(Board board, int depth) {
        nodesUsed++;
        if(depth < 3){
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed);
        }
        //TODO more efficient check
        if (board.nextPlays().isEmpty()) {
            return board.getDeclarerTricks();
        }
        if (board.offenseOnLead()) {
            int value = -1;
            for (Card card : board.nextPlays()) {
                board.makePlay(card);
                value = Math.max(value, minMax(board, depth +1 ));
                board.undoPlay();
            }
            return value;
        } else {
            int value = 14;
            for (Card card : board.nextPlays()) {
                board.makePlay(card);
                value = Math.min(value, minMax(board, depth + 1));
                board.undoPlay();
            }
            return value;
        }
    }
}
