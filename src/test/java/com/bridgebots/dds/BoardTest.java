package com.bridgebots.dds;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardTest {

    @Test
    public void nextMoves() {
        Hand northHand = new BitSetHand("K 2", "", "", "");
        Hand southHand = new BitSetHand("Q", "3", "", "");
        Hand eastHand = new BitSetHand("A 4", "", "", "");
        Hand westHand = new BitSetHand("J 5", "", "", "");

        Board board = Board.of(northHand, southHand, eastHand, westHand, TrumpSuit.CLUBS, Direction.NORTH);

        for(int i = 0; i < 4; i++){
            List<Card> nextPlays = board.nextPlays();
            System.out.println("Could Play: " + nextPlays);
            Card aPlay = nextPlays.get(0);
            System.out.println("Played: " + aPlay);
            board.makePlay(aPlay);
        }
        System.out.println("Trick over, nextPlays = " + board.nextPlays());

        for(int i = 0; i < 4; i++){
            System.out.println("undoing");
            Card undone = board.undoPlay();
            System.out.println("undid " + undone);
        }
        System.out.println(board);
    }

    @Test
    void testClone(){
        Hand northHand = new BitSetHand("", "", "A K 6", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "");
        Board board = Board.of(northHand, southHand, eastHand, westHand, TrumpSuit.SPADES, Direction.EAST);
        Board clone = new Board(board);
        assertEquals(board, clone);

    }
}
