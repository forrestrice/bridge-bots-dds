package com.bridgebots.dds;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlphaBetaSolverTest {

    @Test
    @Disabled
        //probably never completes
    void testFiveCards(){
        AlphaBetaSolver solver = new AlphaBetaSolver();
        Hand northHand = new Hand("", "", "A K 6", "A 10");
        Hand southHand = new Hand("", "A K Q J", "", "2");
        Hand eastHand = new Hand("3", "", "Q J 10", "K");
        Hand westHand = new Hand("A K", "4 3", "2", "");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.SOUTH));
        assertEquals(1, solver.solve(deal, TrumpSuit.SPADES, Direction.EAST));
        assertEquals(1, solver.solve(deal, TrumpSuit.SPADES, Direction.WEST));
    }

    @Test
    @Disabled //probably never completes
    void testSixCards(){
        SimpleSolver solver = new SimpleSolver();
        Hand northHand = new Hand("", "", "A K 6 5", "A 10");
        Hand southHand = new Hand("", "A K Q J 10", "", "2");
        Hand eastHand = new Hand("3", "", "Q J 10 9", "K");
        Hand westHand = new Hand("A K", "4 3", "2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

}