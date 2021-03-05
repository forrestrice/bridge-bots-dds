package com.bridgebots.dds;

import java.util.List;

public interface Hand {
    List<Card> allCards();

    List<Card> legalCards(Suit suitLed);

    List<Card> holding(Suit suit);

    void makePlay(Card cardPlayed);

    void undoPlay(Card card);

    Hand copy();
}
