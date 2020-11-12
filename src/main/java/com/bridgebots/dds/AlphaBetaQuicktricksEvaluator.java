package com.bridgebots.dds;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class AlphaBetaQuicktricksEvaluator {
    private static final Logger LOG = LogManager.getLogger();
    private final Function<Board, List<Card>> cardSelectionFunction;
    private long nodesUsed = 0;

    public AlphaBetaQuicktricksEvaluator(Function<Board, List<Card>> cardSelectionFunction) {
        this.cardSelectionFunction = cardSelectionFunction;
    }

    public int leaderQuicktricks(Board board){
        return leaderQuicktricks(board, -1, 14);
    }

    public int leaderQuicktricks(Board board, int alpha, int beta){
        if(!board.getCurrentTrick().isEmpty()){
            throw new IllegalStateException("cannot evaluate quicktricks for an inprogress trick");
        }
        return leaderQuicktricksAlphaBeta(board, 0, alpha, beta, board.getLead());
    }

    private int leaderQuicktricksAlphaBeta(Board board, int depth, int alpha, int beta, Direction leader) {
        nodesUsed++;
        if (depth < 1) {
            LOG.debug("depth={}, nodesUsed={}", depth, nodesUsed);
        }
        if(board.getCurrentTrick().isEmpty()){
            if(leader != board.getLead() && leader.partner() != board.getLead()){
                return board.getTricks(leader);
            }
            else if (board.nextPlays().isEmpty()){
                return board.getTricks(leader);
            }
        }

        if (board.teamOnLead(leader)) {
            int quicktricks = board.getTricks(leader);
            alpha = Math.max(alpha, quicktricks);
            if (alpha < beta){
                for (Card card : greedySelectionOrdering(board)) {
                    board.makePlay(card);
                    quicktricks = Math.max(quicktricks, leaderQuicktricksAlphaBeta(board, depth + 1, alpha, beta, leader));
                    board.undoPlay();
                    alpha = Math.max(alpha, quicktricks);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            return quicktricks;
        } else {
            int value = 14;
            for (Card card : greedySelectionOrdering(board)) {
                board.makePlay(card);
                value = Math.min(value, leaderQuicktricksAlphaBeta(board, depth + 1, alpha, beta, leader));
                board.undoPlay();
                beta = Math.min(beta, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }
    }

    private List<Card> greedySelectionOrdering(Board board){
        List<Card> possiblePlays = cardSelectionFunction.apply(board);
        if (board.getCurrentTrick().isEmpty()){
            possiblePlays.sort(Comparator.comparingInt(c -> c.rank.rankScore * -1));
            return possiblePlays;
        } else {
            Suit ledSuit = board.getCurrentTrick().get(0).suit;
            Function<Card,Integer> playHigh = c -> c.suit == ledSuit || c.suit == board.getTrumpSuit().suit ? -1 : 1;
            possiblePlays.sort(Comparator.comparingInt(c -> c.rank.rankScore * playHigh.apply(c)));
            return possiblePlays;
        }
        /*
        Suit ledSuit = board.getCurrentTrick().isEmpty() ? null : board.getCurrentTrick().get(0).suit;
        Function<Card,Integer> playHigh = c -> c.suit == board.getTrumpSuit().suit || c.suit == ledSuit ? -1 : 1;
        possiblePlays.sort(Comparator.comparingInt(c -> c.rank.rankScore * playHigh.apply(c)));
        return possiblePlays;*/
    }
}
