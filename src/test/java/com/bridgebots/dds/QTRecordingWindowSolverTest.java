package com.bridgebots.dds;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QTRecordingWindowSolverTest {

    @Test
    void testSevenCardsBitSetHandsWithRestriction(){
        RecordingWindowSolver recordingWindowSolver = new RecordingWindowSolver();
        QTRecordingWindowSolver qtRecordingWindowSolver = new QTRecordingWindowSolver();

        Hand northHand = new BitSetHand("", "", "A K 7 6 5", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J 10 9", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K", "4 3", "3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, recordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(4, qtRecordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void testTenCardsBitSetHandsWithRestriction(){
        RecordingWindowSolver recordingWindowSolver = new RecordingWindowSolver();
        QTRecordingWindowSolver qtRecordingWindowSolver = new QTRecordingWindowSolver();
        Hand northHand = new BitSetHand("7 2", "", "A K 7 6 5", "A 10 3");
        Hand southHand = new BitSetHand("9 4", "A K Q J 10 9", "", "5 2");
        Hand eastHand = new BitSetHand("6 5 3", "6", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K Q", "8 4 3", "8 3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        //assertEquals(7, recordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(7, recordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(7, qtRecordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }



    @Test
    @Disabled
    void fullDeal(){
        //https://www.quicktricks.org/sites/quicktricks.bridgeclubs.net/files/club/results/recap/qt191021.htm board 1
        RecordingWindowSolver recordingWindowSolver = new RecordingWindowSolver();
        QTRecordingWindowSolver qtRecordingWindowSolver = new QTRecordingWindowSolver();
        Hand northHand = new BitSetHand("A K J 9 7 6", "K", "7 6 3", "J 8 6");
        Hand southHand = new BitSetHand("10 5 3", "Q 8 7 2", "Q 8 5 4 2", "A");
        Hand eastHand = new BitSetHand("Q 2", "J 6 5", "J 10 9", "Q 10 7 4 2");
        Hand westHand = new BitSetHand("8 4", "A 10 9 4 3", "A K", "K 9 5 3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.NORTH, false, false);
        assertEquals(8, recordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.EAST));
        assertEquals(8, qtRecordingWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.EAST));
    }
}
