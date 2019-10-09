package com.bridgebots.dds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSolverTest {

    @Test
    void testTwoCards() {
        Hand northHand = new Hand("K 2", "", "", "");
        Hand southHand = new Hand("Q", "3", "", "");
        Hand eastHand = new Hand("A 4", "", "", "");
        Hand westHand = new Hand("J 5", "", "", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.SOUTH, false, false);
        SimpleSolver solver = new SimpleSolver();
        assertEquals(1, solver.solve(deal, TrumpSuit.HEARTS, Direction.WEST));
    }

    @Test
    void testThreeCardsWithFinesseAndRuff() {
        SimpleSolver solver = new SimpleSolver();
        //finesse onside
        Hand northHand = new Hand("A Q", "2", "", "");
        Hand southHand = new Hand("4 3 2", "", "", "");
        Hand eastHand = new Hand("", "A K Q", "", "");
        Hand westHand = new Hand("K 5", "7", "", "");

        Deal onside = new Deal(northHand, southHand, eastHand, westHand, Direction.SOUTH, false, false);
        assertEquals(3, solver.solve(onside, TrumpSuit.CLUBS, Direction.SOUTH));

        //now switch east west so finesse is offside
        Deal offside = new Deal(northHand, southHand, westHand, eastHand, Direction.SOUTH, false, false);
        assertEquals(2, solver.solve(offside, TrumpSuit.CLUBS, Direction.SOUTH));
    }

    @Test
    void testFullHand(){
        SimpleSolver solver = new SimpleSolver();
        Hand northHand = new Hand("A 5", "K Q J 10 4 3", "Q J 8", "6 4");
        Hand southHand = new Hand("9 2", "8 5", "K 10 6", "A K J 8 7 3");
        Hand eastHand = new Hand("Q J 8 6 4 3", "2", "A 5 3", "Q 10 5");
        Hand westHand = new Hand("K 10 7", "A 9 7 6", "9 7 4 2", "9 2");

        Deal onside = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(3, solver.solve(onside, TrumpSuit.SPADES, Direction.NORTH));
    }

}