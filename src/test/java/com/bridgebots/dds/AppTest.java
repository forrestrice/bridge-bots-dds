package com.bridgebots.dds;

import org.junit.Test;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void nextMoves() {
        Hand northHand = new Hand("A 2", "", "", "");
        Hand southHand = new Hand("Q 3", "", "", "");
        Hand eastHand = new Hand("K 4", "", "", "");
        Hand westHand = new Hand("J 5", "", "", "");

        Board board = new Board(northHand, eastHand, southHand, westHand, TrumpSuit.CLUBS, Direction.NORTH);
        List<Card> nextPlays = board.nextPlays();
        System.out.println(nextPlays);

    }
}
