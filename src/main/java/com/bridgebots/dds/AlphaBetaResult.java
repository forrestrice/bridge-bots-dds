package com.bridgebots.dds;

import java.util.Map;

class AlphaBetaResult {
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
