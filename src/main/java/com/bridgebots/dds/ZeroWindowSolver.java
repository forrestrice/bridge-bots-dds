package com.bridgebots.dds;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Function;

public class ZeroWindowSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private final Function<Board, List<Card>> cardSelectionFunction;
    private long nodesUsed = 0;

    public ZeroWindowSolver(){
        this(Board::nextPlays);
    }

    public ZeroWindowSolver(Function<Board, List<Card>> cardSelectionFunction){
        this.cardSelectionFunction = cardSelectionFunction;
    }

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
        if(depth < 20){
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed);
        }
        if (board.nextPlays().isEmpty()) {
            return board.getDeclarerTricks();
        }
        if (board.offenseOnLead()) {
            int value = -1;
            for (Card card : cardSelectionFunction.apply(board)) {
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
            for (Card card : cardSelectionFunction.apply(board)) {
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
