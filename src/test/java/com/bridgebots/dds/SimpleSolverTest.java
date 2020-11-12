package com.bridgebots.dds;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSolverTest {

    @Test
    void testTwoCards() {
        Hand northHand = new BitSetHand("K 2", "", "", "");
        Hand southHand = new BitSetHand("Q", "3", "", "");
        Hand eastHand = new BitSetHand("A 4", "", "", "");
        Hand westHand = new BitSetHand("J 5", "", "", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.SOUTH, false, false);
        SimpleSolver solver = new SimpleSolver();
        assertEquals(1, solver.solve(deal, TrumpSuit.HEARTS, Direction.WEST));
    }

    @Test
    void testThreeCardsWithFinesseAndRuff() {
        SimpleSolver solver = new SimpleSolver();
        //finesse onside
        Hand northHand = new BitSetHand("A Q", "2", "", "");
        Hand southHand = new BitSetHand("4 3 2", "", "", "");
        Hand eastHand = new BitSetHand("", "A K Q", "", "");
        Hand westHand = new BitSetHand("K 5", "7", "", "");

        Deal onside = new Deal(northHand, southHand, eastHand, westHand, Direction.SOUTH, false, false);
        assertEquals(3, solver.solve(onside, TrumpSuit.CLUBS, Direction.SOUTH));

        //now switch east west so finesse is offside
        Deal offside = new Deal(northHand, southHand, westHand, eastHand, Direction.SOUTH, false, false);
        assertEquals(2, solver.solve(offside, TrumpSuit.CLUBS, Direction.SOUTH));
    }

    @Test
    @Disabled //Never completes
    void testFullHand(){
        SimpleSolver solver = new SimpleSolver();
        Hand northHand = new BitSetHand("A 5", "K Q J 10 4 3", "Q J 8", "6 4");
        Hand southHand = new BitSetHand("9 2", "8 5", "K 10 6", "A K J 8 7 3");
        Hand eastHand = new BitSetHand("Q J 8 6 4 3", "2", "A 5 3", "Q 10 5");
        Hand westHand = new BitSetHand("K 10 7", "A 9 7 6", "9 7 4 2", "9 2");

        Deal onside = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(3, solver.solve(onside, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled //probably never completes
    void testEightCards(){
        SimpleSolver solver = new SimpleSolver();
        Hand northHand = new BitSetHand("", "", "A K 6 5 4", "A 10 7");
        Hand southHand = new BitSetHand("", "A K Q J 10 9 8", "", "2");
        Hand eastHand = new BitSetHand("3 2", "", "Q J 10 9 8", "K");
        Hand westHand = new BitSetHand("A K Q", "4 3 2", "2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(5, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled //probably never completes
    void testSixCards(){
        SimpleSolver solver = new SimpleSolver();
        Hand northHand = new BitSetHand("", "", "A K 6 5", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J 10", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10 9", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled //probably never completes
    void testFiveCards(){
        SimpleSolver solver = new SimpleSolver();
        Hand northHand = new BitSetHand("", "", "A K 6", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.SOUTH));
        assertEquals(1, solver.solve(deal, TrumpSuit.SPADES, Direction.EAST));
        assertEquals(1, solver.solve(deal, TrumpSuit.SPADES, Direction.WEST));
    }

}