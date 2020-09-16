package com.bridgebots.dds;

import java.util.*;

public class Board {

    private final Map<Direction, Hand> hands;
    private final TrumpSuit trumpSuit;
    private final TrickEvaluator trickEvaluator;
    private final Direction declarer;
    private int nsTricks = 0;
    private int ewTricks = 0;
    private Direction lead;
    private List<Card> currentTrick;
    private final List<Card> history = new ArrayList<>(52);
    private final List<Direction> trickLeaderHistory = new ArrayList<>(13);
    private final BitSet playedCards;

    public static Board of(Hand north, Hand south, Hand east, Hand west, TrumpSuit trumpSuit, Direction lead) {
        Map<Direction, Hand> hands = new EnumMap<>(Direction.class);
        hands.put(Direction.NORTH, north);
        hands.put(Direction.SOUTH, south);
        hands.put(Direction.EAST, east);
        hands.put(Direction.WEST, west);
        return new Board(hands, trumpSuit, lead, new ArrayList<>(3), lead.previous());
    }

    public static Board forDeal(Deal deal, TrumpSuit trumpSuit, Direction lead) {
        return new Board(deal.getHands(), trumpSuit, lead, new ArrayList<>(3), lead.previous());
    }

    private Board(Map<Direction, Hand> hands, TrumpSuit trumpSuit, Direction lead, List<Card> currentTrick, Direction declarer) {
        this.hands = hands;
        this.trickEvaluator = new TrickEvaluator(trumpSuit);
        this.trumpSuit = trumpSuit;
        this.lead = lead;
        this.currentTrick = currentTrick;
        this.declarer = declarer;
        //If this not a complete deal mark missing cards as played
        this.playedCards = new BitSet(52);
        playedCards.set(0, 52);
        hands.values().stream()
                .map(Hand::allCards)
                .flatMap(Collection::stream)
                .forEach(c -> playedCards.set(c.index, false));
    }

    public List<Card> nextPlays() {
        Hand hand = hands.get(lead);
        if (currentTrick.isEmpty()) {
            return hand.allCards();
        } else {
            Suit suitLed = currentTrick.get(0).suit;
            return hand.legalCards(suitLed);
        }
    }

    public List<Card> restrictedNextPlays() {
        List<Card> allPlays = nextPlays();
        List<Card> restrictedPlays = new ArrayList<>();
        for (int i = 0; i < allPlays.size() - 1; i++) {
            Card play = allPlays.get(i);
            Card nextPlay = allPlays.get(i + 1);
            //Determine if this card and the next card are equivalent plays
            if (play.suit == nextPlay.suit) {
                boolean touching = true;
                for (int j = play.index + 1; j < nextPlay.index; j++) {
                    if (!playedCards.get(j)) {
                        touching = false;
                        break;
                    }
                }
                if (!touching) {
                    restrictedPlays.add(play);
                }
            } else {
                restrictedPlays.add(play);
            }
        }
        //We can always play the last card since it is the highest of its suit
        restrictedPlays.add(allPlays.get(allPlays.size() - 1));
        return restrictedPlays;
    }

    public void makePlay(Card cardPlayed) {
        history.add(cardPlayed);
        currentTrick.add(cardPlayed);
        playedCards.set(cardPlayed.index);
        hands.get(lead).makePlay(cardPlayed);
        if (currentTrick.size() == 4) {
            completeTrick();
        } else {
            lead = lead.next();
        }
    }

    public Card undoPlay() {
        int lastMoveIndex = history.size() - 1;
        if (currentTrick.isEmpty()) {
            if (lead == Direction.NORTH || lead == Direction.SOUTH) {
                nsTricks--;
            } else {
                ewTricks--;
            }
            int trickIndex = trickLeaderHistory.size() - 1;
            lead = trickLeaderHistory.remove(trickIndex).previous();
            currentTrick = new ArrayList<>(history.subList(lastMoveIndex - 3, lastMoveIndex));
        } else {
            currentTrick.remove(currentTrick.size() - 1);
            lead = lead.previous();
        }
        Card undone = history.remove(lastMoveIndex);
        playedCards.clear(undone.index);
        hands.get(lead).undoPlay(undone);
        return undone;
    }

    private void completeTrick() {
        trickLeaderHistory.add(lead.next());
        lead = trickEvaluator.computeTrickWinner(currentTrick, lead);
        if (lead == Direction.NORTH || lead == Direction.SOUTH) {
            nsTricks++;
        } else {
            ewTricks++;
        }
        currentTrick.clear();
    }

    public int getNsTricks() {
        return nsTricks;
    }

    public int getEwTricks() {
        return ewTricks;
    }

    public int getDeclarerTricks() {
        return (declarer == Direction.NORTH || declarer == Direction.SOUTH) ? nsTricks : ewTricks;
    }

    public boolean offenseOnLead() {
        return declarer == lead || declarer.partner() == lead;
    }

    private static class TrickEvaluator {
        private final Comparator<Card> cardComparator;
        private Suit suitLed;

        private TrickEvaluator(TrumpSuit trumpSuit) {
            this.cardComparator = Comparator.comparing(trumpSuit::isTrump)
                    .thenComparing(c -> c.suit == suitLed)
                    .thenComparing(c -> c.rank.ordinal());
        }

        private Direction computeTrickWinner(List<Card> trick, Direction lastDirection) {
            suitLed = trick.get(0).suit;
            Card winner = null;
            int offset = 1;

            for (int index = 0; index < 4; index++) {
                if (winner == null) {
                    winner = trick.get(index);
                } else {
                    Card current = trick.get(index);
                    if (cardComparator.compare(current, winner) > 0) {
                        winner = current;
                        offset = 1 + index;
                    }
                }
            }
            return lastDirection.offset(offset);
        }
    }
}
