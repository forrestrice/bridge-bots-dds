package com.bridgebots.dds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TranspositionTableTest {

    @Test
    void testTTKey() {
        Hand northHand = new BitSetHand("", "", "A K 6", "A 10");
        Hand southHand = new BitSetHand("", "A K Q J", "", "2");
        Hand eastHand = new BitSetHand("3", "", "Q J 10", "K");
        Hand westHand = new BitSetHand("A K", "4 3", "2", "");
        Board board1 = Board.of(northHand, southHand, eastHand, westHand, TrumpSuit.SPADES, Direction.EAST);
        Board board2 = Board.of(northHand, southHand, eastHand, westHand, TrumpSuit.SPADES, Direction.EAST);

        assertEquals(TranspositionTable.calculateKey(board1), TranspositionTable.calculateKey(board2));

        board1.makePlay(Card.of(Suit.HEARTS, Rank.TEN));
        board2.makePlay(Card.of(Suit.HEARTS, Rank.QUEEN));
        assertEquals(TranspositionTable.calculateKey(board1), TranspositionTable.calculateKey(board2));

        board1.makePlay(Card.of(Suit.DIAMONDS, Rank.ACE));
        board2.makePlay(Card.of(Suit.SPADES, Rank.TWO));
        assertNotEquals(TranspositionTable.calculateKey(board1), TranspositionTable.calculateKey(board2));

    }

}