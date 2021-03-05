package com.bridgebots.dds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import static com.bridgebots.dds.DealStringUtils.parseSuitString;

public class BitSetHand implements Hand, Serializable {
    private static final long serialVersionUID = 1L;
    private final BitSet bitSet = new BitSet(52);

    public BitSetHand(List<Rank> clubs, List<Rank> diamonds, List<Rank> hearts, List<Rank> spades) {
        List<List<Rank>> suitList = List.of(clubs, diamonds, hearts, spades);
        for (int suitIndex = 0; suitIndex < 4; suitIndex++) {
            List<Rank> suitRanks = suitList.get(suitIndex);
            for (Rank suitRank : suitRanks) {
                bitSet.set(13 * suitIndex + suitRank.ordinal());
            }
        }
    }

    public BitSetHand(String clubs, String diamonds, String hearts, String spades) {
        this(parseSuitString(clubs), parseSuitString(diamonds), parseSuitString(hearts), parseSuitString(spades));
    }

    @Override
    public Hand copy(){
        return new BitSetHand(this);
    }

    public BitSetHand(BitSetHand toCopy){
        this.bitSet.or(toCopy.bitSet);
    }

    @Override
    public List<Card> allCards() {
        List<Card> allCards = new ArrayList<>();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            allCards.add(Card.ofIndex(i));
        }
        return allCards;
    }

    @Override
    public List<Card> legalCards(Suit suitLed) {
        int suitOffset = suitLed.ordinal() * 13;
        BitSet suitBitSet = bitSet.get(suitOffset, suitOffset + 13);
        if (suitBitSet.isEmpty()) {
            return allCards();
        } else {
            List<Card> followSuit = new ArrayList<>();
            for (int i = suitBitSet.nextSetBit(0); i >= 0; i = suitBitSet.nextSetBit(i + 1)) {
                followSuit.add(Card.ofIndex(i + suitOffset));
            }
            return followSuit;
        }
    }

    @Override
    public List<Card> holding(Suit suit){
        int suitOffset = suit.ordinal() * 13;
        BitSet suitBitSet = bitSet.get(suitOffset, suitOffset + 13);
        List<Card> holding = new ArrayList<>();
        for (int i = suitBitSet.nextSetBit(0); i >= 0; i = suitBitSet.nextSetBit(i + 1)) {
            holding.add(Card.ofIndex(i + suitOffset));
        }
        return holding;
    }

    @Override
    public void makePlay(Card cardPlayed) {
        bitSet.clear(cardPlayed.index);
    }

    @Override
    public void undoPlay(Card card) {
        bitSet.set(card.index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitSetHand that = (BitSetHand) o;
        return bitSet.equals(that.bitSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitSet);
    }
}
