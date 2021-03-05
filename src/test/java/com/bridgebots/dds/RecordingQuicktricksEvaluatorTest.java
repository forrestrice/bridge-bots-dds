package com.bridgebots.dds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordingQuicktricksEvaluatorTest {


    @Test
    void testFiveCards(){
        Hand northHand = new BitSetHand("", "", "A K 6", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);

        RecordingQuicktricksEvaluator qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);
        Board board = Board.forDeal(deal, TrumpSuit.NO_TRUMP, Direction.SOUTH);
        assertEquals(5, qtEvaluator.leaderQuicktricks(board, 5));

        //reset TT table
        qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);
        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.SOUTH);
        assertEquals(0, qtEvaluator.leaderQuicktricks(board, 1));

        //reset TT table
        qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);
        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.EAST);
        assertEquals(2, qtEvaluator.leaderQuicktricks(board, 3));

        //reset TT table
        qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);
        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.EAST);
        assertEquals(2, qtEvaluator.leaderQuicktricks(board, 2));

        //reset TT table
        qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);
        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.EAST);
        assertEquals(1, qtEvaluator.leaderQuicktricks(board, 1));
    }

    /*
    @Test
    void testTwoCards(){
        Hand northHand = new BitSetHand("2", "", "", "A");
        Hand southHand = new BitSetHand("", "2", "A", "");
        Hand eastHand = new BitSetHand("", "A", "", "2");
        Hand westHand = new BitSetHand("A", "", "2", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        RecordingQuicktricksEvaluator qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);

        Board board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.SOUTH);
        assertEquals(1, qtEvaluator.leaderQuicktricks(board));

        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.NORTH);
        assertEquals(0, qtEvaluator.leaderQuicktricks(board));

        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.EAST);
        assertEquals(1, qtEvaluator.leaderQuicktricks(board));
        board = Board.forDeal(deal, TrumpSuit.CLUBS, Direction.WEST);
        assertEquals(1, qtEvaluator.leaderQuicktricks(board));
    }


    @Test
    void testFiveCardsWindow() {
        RecordingQuicktricksEvaluator qtEvaluator = new RecordingQuicktricksEvaluator(Board::restrictedNextPlays);

        Hand northHand = new BitSetHand("", "", "A K 6", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        Board board = Board.forDeal(deal, TrumpSuit.SPADES, Direction.EAST);
        assertTrue(qtEvaluator.leaderQuicktricks(board, 1, 2) < 2);
        //assertEquals(4, windowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        //assertEquals(4, quicktricksWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }*/

}