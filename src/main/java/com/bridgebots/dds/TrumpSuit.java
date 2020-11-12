package com.bridgebots.dds;

public enum TrumpSuit {
    CLUBS(Suit.CLUBS),
    DIAMONDS(Suit.DIAMONDS),
    HEARTS(Suit.HEARTS),
    SPADES(Suit.SPADES),
    NO_TRUMP(null);

    public final Suit suit;

    TrumpSuit(Suit suit) {
        this.suit = suit;
    }

    public boolean isTrump(Card card){
        return card.suit == this.suit;
    }
}
