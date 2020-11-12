package com.bridgebots.dds;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlphaBetaSolverTest {

    @Test
    void testFiveCards(){
        AlphaBetaSolver solver = new AlphaBetaSolver();
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

    @Test
    void testSixCards(){
        AlphaBetaSolver solver = new AlphaBetaSolver();
        Hand northHand = new BitSetHand("", "", "A K 6 5", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J 10", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10 9", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void testSevenCards(){
        AlphaBetaSolver solver = new AlphaBetaSolver();
        Hand northHand = new BitSetHand("", "", "A K 7 6 5", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J 10 9", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K", "4 3", "3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void testSevenCardsBitSetHands(){
        AlphaBetaSolver solver = new AlphaBetaSolver();
        Hand northHand = new BitSetHand("", "", "A K 7 6 5", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J 10 9", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K", "4 3", "3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }
    @Test
    void testSevenCardsBitSetHandsWithRestriction(){
        AlphaBetaSolver solver = new AlphaBetaSolver(Board::restrictedNextPlays);
        Hand northHand = new BitSetHand("", "", "A K 7 6 5", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J 10 9", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K", "4 3", "3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(4, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void testEightCardsBitSetHandsWithRestriction(){
        AlphaBetaSolver solver = new AlphaBetaSolver(Board::restrictedNextPlays);
        Hand northHand = new BitSetHand("7", "", "A K 7 6 5", "A 10");
        Hand southHand = new BitSetHand("4", "A K Q J 10 9", "", "2");
        Hand eastHand = new BitSetHand("5 3", "", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K Q", "4 3", "3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(3, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void testTenCardsBitSetHandsWithRestriction(){
        AlphaBetaSolver solver = new AlphaBetaSolver(Board::restrictedNextPlays);
        Hand northHand = new BitSetHand("7 2", "", "A K 7 6 5", "A 10 3");
        Hand southHand = new BitSetHand("9 4", "A K Q J 10 9", "", "5 2");
        Hand eastHand = new BitSetHand("6 5 3", "6", "Q J 10 9", "K 4");
        Hand westHand = new BitSetHand("A K Q", "8 4 3", "8 3 2", "3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.WEST, true, true);
        assertEquals(7, solver.solve(deal, TrumpSuit.SPADES, Direction.NORTH));
    }

    @Test
    @Disabled
    void fullDeal(){
        //https://www.quicktricks.org/sites/quicktricks.bridgeclubs.net/files/club/results/recap/qt191021.htm board 1
        AlphaBetaSolver solver = new AlphaBetaSolver(Board::restrictedNextPlays);
        Hand northHand = new BitSetHand("A K J 9 7 6", "K", "7 6 3", "J 8 6");
        Hand southHand = new BitSetHand("10 5 3", "Q 8 7 2", "Q 8 5 4 2", "A");
        Hand eastHand = new BitSetHand("Q 2", "J 6 5", "J 10 9", "Q 10 7 4 2");
        Hand westHand = new BitSetHand("8 4", "A 10 9 4 3", "A K", "K 9 5 3");

        Deal deal = new Deal(northHand, southHand, eastHand, westHand, Direction.NORTH, false, false);
        assertEquals(8, solver.solve(deal, TrumpSuit.SPADES, Direction.EAST));
    }


}