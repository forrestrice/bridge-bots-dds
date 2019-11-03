package com.bridgebots.dds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Card {

    public final Suit suit;
    public final Rank rank;
    public final int index;

    private Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.index = suit.ordinal() * 13 + rank.ordinal();
    }

    public String toString(){
        return rank.name() + " of " + suit.name();
    }

    public static final List<Card> DECK = buildDeck();

    private static List<Card> buildDeck(){
        List<Card> deck = new ArrayList<>(52);
        for(Suit suit : Suit.values()){
            for (Rank rank : Rank.values()){
                deck.add(new Card(suit, rank));
            }
        }
        return Collections.unmodifiableList(deck);
    }

    public static Card of(Suit suit, Rank rank){
        return DECK.get(suit.ordinal() * 13 + rank.ordinal());
    }

    public static Card ofIndex(int index){
        return DECK.get(index);
    }
}
