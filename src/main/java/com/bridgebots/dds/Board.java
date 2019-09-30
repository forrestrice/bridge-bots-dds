package com.bridgebots.dds;

import java.util.*;

public class Board {

    private final Map<Direction, Hand> hands;
    private final TrumpSuit trumpSuit;
    //private final Comparator<Card> cardComparator;
    private Direction lead;
    private final List<Card> currentTrick;
    private final List<Card> history = new ArrayList<>();

    public Board(Hand north, Hand south, Hand east, Hand west, TrumpSuit trumpSuit, Direction lead){
        this(north, south, east, west, trumpSuit, lead, new ArrayList<>(3));
    }

    public Board(Hand north, Hand south, Hand east, Hand west, TrumpSuit trumpSuit, Direction lead, List<Card> currentTrick){
        this.hands = new EnumMap<>(Direction.class);
        hands.put(Direction.NORTH, north);
        hands.put(Direction.SOUTH, south);
        hands.put(Direction.EAST, east);
        hands.put(Direction.WEST, west);

        this.trumpSuit = trumpSuit;
        this.lead = lead;
        this.currentTrick = currentTrick;
        //this.cardComparator = cardComparator(trumpSuit);
    }

    public List<Card> nextPlays(){
        Hand hand = hands.get(lead);
        if(currentTrick.isEmpty()){
            return hand.allCards();
        } else {
            Suit suitLed = currentTrick.get(0).suit;
            return hand.legalCards(suitLed);
        }
    }

    public void makePlay(Card cardPlayed){
        history.add(cardPlayed);
        currentTrick.add(cardPlayed);
        hands.get(lead).makePlay(cardPlayed);
        
        if(currentTrick.size() == 4){
            completeTrick();
        } else {
            lead = lead.next();
        }
    }

    private void completeTrick() {
        if(trumpSuit != TrumpSuit.NO_TRUMP){
            //currentTrick.stream().max()
            
        }
        currentTrick.clear();
    }
    
    private Comparator<Card> cardComparator(TrumpSuit trumpSuit, Suit suitLed){
        return Comparator.comparing(trumpSuit::isTrump)
                .thenComparing(c -> c.suit == suitLed)
                .thenComparing(c -> c.rank.ordinal());
    }
}
