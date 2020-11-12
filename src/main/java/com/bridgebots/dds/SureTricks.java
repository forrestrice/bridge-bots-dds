package com.bridgebots.dds;

import java.util.BitSet;

public class SureTricks {

    public static int calculateSureTricks(Board board){
        TrumpSuit trumpSuit = board.getTrumpSuit();
        if(trumpSuit == TrumpSuit.NO_TRUMP) {
            return noTrumpSureTricks(board);
        } else {
            return suitSureTricks(board, trumpSuit);
        }
    }

    private static int suitSureTricks(Board board, TrumpSuit trumpSuit) {
        Direction lead = board.getLead();
        Hand leadHand = board.getHand(lead);
        BitSet playedCards = board.getPlayedCards();

        int trumpSuitOffset = trumpSuit.suit.ordinal() * 13;

        //maxOpponentTrumpLength =

        int leadTopTrumps = 0;



        //BitSet playedTrumps = playedCards.get(trumpSuitOffset, trumpSuitOffset + 13);
        //playedTrumps.cardinality()


        //First try to pull trump

        return 0;
    }

    private static int noTrumpSureTricks(Board board) {
        return 0;
    }
}
