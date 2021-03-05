package com.bridgebots.dds;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bridgebots.dds.DealStringUtils.parseSuitString;

public class ListHand implements Hand {
    private final Map<Suit, List<Rank>> ranksBySuit;

    public ListHand(List<Rank> clubs, List<Rank> diamonds, List<Rank> hearts, List<Rank> spades) {
        ranksBySuit = new EnumMap<Suit, List<Rank>>(Suit.class);
        ranksBySuit.put(Suit.CLUBS, clubs);
        ranksBySuit.put(Suit.DIAMONDS, diamonds);
        ranksBySuit.put(Suit.HEARTS, hearts);
        ranksBySuit.put(Suit.SPADES, spades);
    }

    public ListHand(String clubs, String diamonds, String hearts, String spades) {
        this(parseSuitString(clubs), parseSuitString(diamonds), parseSuitString(hearts), parseSuitString(spades));
    }

    public ListHand(ListHand listHand) {
        this.ranksBySuit = new EnumMap<Suit, List<Rank>>(Suit.class);
        listHand.ranksBySuit.forEach(ranksBySuit::put);
    }

    //TODO this is broken with restricted next plays - cards are returned in reverse order
    @Override
    public List<Card> allCards() {
        List<Card> allCards = new ArrayList<>();
        ranksBySuit.forEach((suit, ranks) -> ranks.forEach(rank -> allCards.add(Card.of(suit, rank))));
        return allCards;
    }

    //TODO this is broken with restricted next plays - cards are returned in reverse order
    @Override
    public List<Card> legalCards(Suit suitLed) {
        List<Rank> followSuit = ranksBySuit.get(suitLed);
        if (followSuit.isEmpty()) {
            return allCards();
        } else {
            return followSuit.stream().map(r -> Card.of(suitLed, r)).collect(Collectors.toList());
        }
    }

    @Override
    public List<Card> holding(Suit suit) {
        return  ranksBySuit.get(suit).stream().map(r -> Card.of(suit, r)).collect(Collectors.toList());
    }

    @Override
    public void makePlay(Card cardPlayed) {
        ranksBySuit.get(cardPlayed.suit).remove(cardPlayed.rank);
    }

    @Override
    public void undoPlay(Card card){
        ranksBySuit.get(card.suit).add(card.rank);
    }

    @Override
    public Hand copy() {
        return new ListHand(this);
    }
}
