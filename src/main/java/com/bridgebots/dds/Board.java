package com.bridgebots.dds;


import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Board implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private final Map<Direction, Hand> hands;
    private final TrumpSuit trumpSuit;
    private final TrickEvaluator trickEvaluator;
    private final Direction declarer;
    private int tricksAvailable;
    private int nsTricks = 0;
    private int ewTricks = 0;
    private int cardsRemaining = 0;
    private Direction lead;
    private List<Card> currentTrick;
    private final List<Card> history;
    private final List<Direction> trickLeaderHistory;
    private final BitSet playedCards;
    private final Map<Integer, Direction> cardIndexToHolder;

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

    private Board(Map<Direction, Hand> hands,
                  TrumpSuit trumpSuit,
                  Direction lead,
                  List<Card> currentTrick,
                  Direction declarer) {
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
        this.tricksAvailable = hands.get(lead).allCards().size();
        this.cardsRemaining = hands.values().stream().map(Hand::allCards).mapToInt(Collection::size).sum();
        this.cardIndexToHolder = new HashMap<>();
        hands.forEach((dir, hand) -> {
            hand.allCards().forEach(card -> cardIndexToHolder.put(card.index, dir));
        });
        this.trickLeaderHistory = new ArrayList<>(13);
        this.history = new ArrayList<>(52);
    }

    public Board(Board toClone) {
        this.hands = new EnumMap<>(Direction.class);
        toClone.hands.forEach((k,v) -> hands.put(k, v.copy()));

        this.trickEvaluator = toClone.trickEvaluator;
        this.trumpSuit = toClone.trumpSuit;
        this.lead = toClone.lead;
        this.currentTrick = new ArrayList<>(toClone.currentTrick);
        this.declarer = toClone.declarer;
        this.nsTricks = toClone.nsTricks;
        this.ewTricks = toClone.ewTricks;
        this.playedCards = (BitSet) toClone.playedCards.clone();
        this.tricksAvailable = toClone.tricksAvailable;
        this.cardsRemaining = toClone.cardsRemaining;
        this.cardIndexToHolder = new HashMap<>(toClone.cardIndexToHolder);
        this.trickLeaderHistory = new ArrayList<>(toClone.trickLeaderHistory);
        this.history = new ArrayList<>(toClone.history);
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
        //playedCards.set(cardPlayed.index);
        cardIndexToHolder.remove(cardPlayed.index);
        cardsRemaining--;
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
            tricksAvailable++;
            int trickIndex = trickLeaderHistory.size() - 1;
            lead = trickLeaderHistory.remove(trickIndex).previous();
            history.subList(history.size() - 4, history.size()).forEach(c -> playedCards.clear(c.index));
            currentTrick = new ArrayList<>(history.subList(lastMoveIndex - 3, lastMoveIndex));
        } else {
            currentTrick.remove(currentTrick.size() - 1);
            lead = lead.previous();
        }
        Card undone = history.remove(lastMoveIndex);
        //playedCards.clear(undone.index);
        cardsRemaining++;
        hands.get(lead).undoPlay(undone);
        cardIndexToHolder.put(undone.index, lead);
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
        tricksAvailable--;
        currentTrick.forEach(c -> playedCards.set(c.index));
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

    public int getTricks(Direction direction) {
        return (direction == Direction.NORTH || direction == Direction.SOUTH) ? nsTricks : ewTricks;
    }

    public Map<Integer, Direction> getCardIndexToHolder() {
        return cardIndexToHolder;
    }

    public boolean offenseOnLead() {
        return declarer == lead || declarer.partner() == lead;
    }

    public boolean teamOnLead(Direction direction) {
        return direction == lead || direction.partner() == lead;
    }

    public TrumpSuit getTrumpSuit() {
        return trumpSuit;
    }

    public Direction getLead() {
        return lead;
    }

    public Hand getHand(Direction direction) {
        return hands.get(direction);
    }

    public BitSet getPlayedCards() {
        return playedCards;
    }

    public List<Card> getCurrentTrick() {
        return currentTrick;
    }

    public int getTricksAvailable() {
        return tricksAvailable;
    }

    public int getCardsRemaining() {
        return cardsRemaining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return tricksAvailable == board.tricksAvailable &&
                nsTricks == board.nsTricks &&
                ewTricks == board.ewTricks &&
                cardsRemaining == board.cardsRemaining &&
                hands.equals(board.hands) &&
                trumpSuit == board.trumpSuit &&
                declarer == board.declarer &&
                lead == board.lead &&
                currentTrick.equals(board.currentTrick) &&
                history.equals(board.history) &&
                trickLeaderHistory.equals(board.trickLeaderHistory) &&
                playedCards.equals(board.playedCards) &&
                cardIndexToHolder.equals(board.cardIndexToHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hands, trumpSuit, declarer, tricksAvailable, nsTricks, ewTricks, cardsRemaining, lead,
                currentTrick, history, trickLeaderHistory, playedCards, cardIndexToHolder);
    }


    private static class TrickEvaluator implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Comparator<Card> cardComparator;
        private Suit suitLed;

        private TrickEvaluator(TrumpSuit trumpSuit) {
            this.cardComparator = Comparator.comparing(trumpSuit::isTrump)
                    .thenComparing(c -> c.suit == suitLed)
                    .thenComparing(c -> c.rank.ordinal());
        }

        private Direction computeTrickWinner(List<Card> trick, Direction lastDirection) {
            suitLed = trick.get(0).suit;
            Card winner = trick.get(0);
            int offset = 1;

            for (int index = 1; index < 4; index++) {
                Card current = trick.get(index);
                if (cardComparator.compare(current, winner) > 0) {
                    winner = current;
                    offset = 1 + index;
                }
            }
            return lastDirection.offset(offset);
        }
    }

    public String toLogString() {
        StringBuilder sb = new StringBuilder().append("\n");
        sb.append("tricksAvailable:").append(tricksAvailable).append("\n");
        sb.append("nsTricks:").append(nsTricks).append("\n");
        sb.append("ewTricks:").append(ewTricks).append("\n");
        sb.append("cardsRemaining:").append(cardsRemaining).append("\n");
        sb.append("trumpSuit:").append(trumpSuit).append("\n");
        sb.append("declarer:").append(declarer).append("\n");
        sb.append("lead:").append(lead).append("\n");
        sb.append("lead:").append(lead).append("\n");
        sb.append("currentTrick:").append(currentTrick).append("\n");
        sb.append("history:").append(history).append("\n");
        sb.append("trickLeaderHistory:").append(trickLeaderHistory).append("\n");
        for (Direction direction : Direction.values()) {
            sb.append(direction).append("\n");
            for (Suit suit : Suit.values()) {
                sb.append(hands.get(direction).holding(suit));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
