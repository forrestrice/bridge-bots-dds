package com.bridgebots.dds;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuicktricksWindowSolverTest {

    @Test
    void testFiveCards() {
        WindowSolver windowSolver = new WindowSolver(Board::restrictedNextPlays);
        QuicktricksWindowSolver quicktricksWindowSolver = new QuicktricksWindowSolver(Board::restrictedNextPlays);

        Hand northHand = new BitSetHand("", "", "A K 6", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, windowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(4, quicktricksWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(4, windowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void testTenCardsBitSetHandsWithRestriction(){
        WindowSolver windowSolver = new WindowSolver(Board::restrictedNextPlays);
        QuicktricksWindowSolver quicktricksWindowSolver = new QuicktricksWindowSolver(Board::restrictedNextPlays);
        Hand northHand = new BitSetHand("7 2", "", "A K 7 6 5", "A 10 3");
        Hand southHand = new BitSetHand("9 4", "A K Q J 10 9", "", "5 2");
        Hand eastHand = new BitSetHand("6 5 3", "6", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K Q", "8 4 3", "8 3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(7, windowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        //assertEquals(7, quicktricksWindowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        //assertEquals(7, windowSolver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

}