package com.bridgebots.dds;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Function;

public class QuicktricksWindowSolver implements Solver {
    private static final Logger LOG = LogManager.getLogger();
    private final AlphaBetaQuicktricksEvaluator quicktricksEvaluator;
    private long nodesUsed = 0;
    private final Function<Board, List<Card>> cardSelectionFunction;

    public QuicktricksWindowSolver() {
        this(Board::nextPlays);
    }

    public QuicktricksWindowSolver(Function<Board, List<Card>> cardSelectionFunction) {
        this.cardSelectionFunction = cardSelectionFunction;
        this.quicktricksEvaluator = new AlphaBetaQuicktricksEvaluator(cardSelectionFunction);
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
        return lowerBound;
    }

    private int chooseTarget(int lowerBound, int upperBound) {
        return lowerBound + Math.max(1, (upperBound - lowerBound) / 2);
    }

    private int alphaBetaSolve(Board board, int depth, int alpha, int beta) {
        nodesUsed++;
        if (depth < 2) {
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed);
        }
        //TODO more efficient check
        if (board.nextPlays().isEmpty()) {
            return board.getDeclarerTricks();
        }
        if (board.getCurrentTrick().isEmpty()) {
            //If offense is on lead, can they achieve the target through quicktricks. If not, continue full search
            if (board.offenseOnLead()) {
                int quicktricksResult = quicktricksEvaluator.leaderQuicktricks(board, alpha, beta);
                if (quicktricksResult == beta) {
                    return beta;
                }
            } else {
                //If defense is on lead, can they prevent the target through quicktricks. If not, continue full search
                int defenseTarget = (board.getTricksAvailable() - beta) + 1;
                int defenseQuicktricksResult = quicktricksEvaluator.leaderQuicktricks(board, defenseTarget - 1, defenseTarget);
                if (defenseQuicktricksResult == defenseTarget) {
                    return alpha;
                }
            }
        }
        if (board.offenseOnLead()) {
            int value = -1;
            for (Card card : cardSelectionFunction.apply(board)) {
                board.makePlay(card);
                value = Math.max(value, alphaBetaSolve(board, depth + 1, alpha, beta));
                board.undoPlay();
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        } else {
            int value = 14;
            for (Card card : cardSelectionFunction.apply(board)) {
                board.makePlay(card);
                value = Math.min(value, alphaBetaSolve(board, depth + 1, alpha, beta));
                board.undoPlay();
                beta = Math.min(beta, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }
    }
}
