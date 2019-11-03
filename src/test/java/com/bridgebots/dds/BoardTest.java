package com.bridgebots.dds;


import org.junit.jupiter.api.Test;

import java.util.List;

public class BoardTest {

    @Test
    public void nextMoves() {
        Hand northHand = new ListHand("K 2", "", "", "");
        Hand southHand = new ListHand("Q", "3", "", "");
        Hand eastHand = new ListHand("A 4", "", "", "");
        Hand westHand = new ListHand("J 5", "", "", "");

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
}
