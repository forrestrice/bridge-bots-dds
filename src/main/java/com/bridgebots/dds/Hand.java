package com.bridgebots.dds;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bridgebots.dds.DealStringUtils.parseSuitString;

public class Hand {
    private final Map<Suit, List<Rank>> ranksBySuit;

    public Hand(List<Rank> clubs, List<Rank> diamonds, List<Rank> hearts, List<Rank> spades) {
        ranksBySuit = new EnumMap<Suit, List<Rank>>(Suit.class);
        ranksBySuit.put(Suit.CLUBS, clubs);
        ranksBySuit.put(Suit.DIAMONDS, diamonds);
        ranksBySuit.put(Suit.HEARTS, hearts);
        ranksBySuit.put(Suit.SPADES, spades);
    }

    public Hand(String clubs, String diamonds, String hearts, String spades) {
        this(parseSuitString(clubs), parseSuitString(diamonds), parseSuitString(hearts), parseSuitString(spades));
    }

    public List<Card> allCards() {
        List<Card> allCards = new ArrayList<>();
        ranksBySuit.forEach((suit, ranks) -> ranks.forEach(rank -> allCards.add(Card.of(suit, rank))));
        return allCards;
    }

    public List<Card> legalCards(Suit suitLed) {
        List<Rank> followSuit = ranksBySuit.get(suitLed);
        if (followSuit.isEmpty()) {
            return allCards();
        } else {
            return followSuit.stream().map(r -> Card.of(suitLed, r)).collect(Collectors.toList());
        }
    }

    public void makePlay(Card cardPlayed) {
        ranksBySuit.get(cardPlayed.suit).remove(cardPlayed.rank);
    }
}
