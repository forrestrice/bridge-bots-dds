package com.bridgebots.dds;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class RecordingQuicktricksEvaluator {
    private final Function<Board, List<Card>> cardSelectionFunction;
    private final TranspositionTable<AlphaBetaResult> transpositionTable = new TranspositionTable<>();
    private long nodesUsed = 0;
    private int hitsWithoutTermination;

    public RecordingQuicktricksEvaluator(Function<Board, List<Card>> cardSelectionFunction) {
        this.cardSelectionFunction = cardSelectionFunction;
    }

    //Return
    public int leaderQuicktricks(Board board, int target){
        if(!board.getCurrentTrick().isEmpty()){
            throw new IllegalStateException("cannot evaluate quicktricks for an inprogress trick");
        }
        return quicktricksAlphaBeta(board, 0, target -1, target, board.getLead());
    }


    private int quicktricksAlphaBeta(Board board, int depth, int alpha, int beta, Direction maximizingTeamLead){
        nodesUsed++;
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

        if (cachedResult != null){
            hitsWithoutTermination++;
        }

        int initialAlpha = alpha;
        int initialBeta = beta;
        int value = board.teamOnLead(maximizingTeamLead) ? -1 : 14;

        for (Card card : greedySelectionOrdering(board)) {
            board.makePlay(card);
            int cardResult;
            if (board.getCurrentTrick().isEmpty() && !board.teamOnLead(maximizingTeamLead)) {
                //The other team won a trick, terminate with zero remaining quicktricks
                cardResult = 0;
            } else {
                int currentTrickValue = board.getCurrentTrick().isEmpty() && board.teamOnLead(maximizingTeamLead) ? 1 : 0;
                cardResult = currentTrickValue + quicktricksAlphaBeta(board, depth + 1, alpha - currentTrickValue, beta - currentTrickValue, maximizingTeamLead);
            }
            board.undoPlay();
            if (board.teamOnLead(maximizingTeamLead)) {
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

        if(board.getCurrentTrick().isEmpty()){
            AlphaBetaResult alphaBetaResult = new AlphaBetaResult(null, initialAlpha, initialBeta, value, null);
            if (cachedResult != null) {
                alphaBetaResult = AlphaBetaResult.merge(alphaBetaResult, cachedResult);
            }
            transpositionTable.put(board, alphaBetaResult);

        }
        return value;
    }

    public long getNodesUsed() {
        return nodesUsed;
    }

    public TranspositionTable<AlphaBetaResult> getTranspositionTable(){
        return transpositionTable;
    }

    public int getHitsWithoutTermination() {
        return hitsWithoutTermination;
    }

    private List<Card> greedySelectionOrdering(Board board) {
        List<Card> possiblePlays = cardSelectionFunction.apply(board);
        if (board.getCurrentTrick().isEmpty()) {
            possiblePlays.sort(Comparator.comparingInt(c -> c.rank.rankScore * -1));
            return possiblePlays;
        } else {
            Suit ledSuit = board.getCurrentTrick().get(0).suit;
            Function<Card, Integer> playHigh = c -> c.suit == ledSuit || c.suit == board.getTrumpSuit().suit ? -1 : 1;
            possiblePlays.sort(Comparator.comparingInt(c -> c.rank.rankScore * playHigh.apply(c)));
            return possiblePlays;
        }
    }
}
