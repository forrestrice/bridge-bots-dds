package com.bridgebots.dds;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class TranspositionTable<V> {
    private int queryCount = 0;
    private int hitCount = 0;


    private Map<TTKey, V> table = new ConcurrentHashMap<>();

    public void put(Board board, V value){
        table.put(calculateKey(board), value);
    }

    public V get(Board board){
        queryCount++;
        V result = table.get(calculateKey(board));
        if (result != null){
            hitCount++;
        }
        return result;
    }

    public int keyCount(){
        return table.keySet().size();
    }


    public static TTKey calculateKey(Board board) {
        Map<Integer, Direction> cardIndexToHolder = board.getCardIndexToHolder();

        Map<Suit, List<Direction>> relativeRanks = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            int suitOffset = suit.ordinal() * 13;
            for (int suitIndex = 12; suitIndex >= 0; suitIndex--) {
                Direction holder = cardIndexToHolder.get(suitIndex + suitOffset);
                if (holder != null) {
                    relativeRanks.computeIfAbsent(suit, k -> new ArrayList<>(13)).add(holder);
                }
            }
        }
        return new TTKey(relativeRanks, board.getLead());
    }

    public int getQueryCount() {
        return queryCount;
    }

    public int getHitCount() {
        return hitCount;
    }



    public static class TTKey {
        final Map<Suit, List<Direction>> relativeRanks;
        final Direction lead;

        public TTKey(Map<Suit, List<Direction>> relativeRanks, Direction lead) {
            this.relativeRanks = relativeRanks;
            this.lead = lead;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TTKey ttKey = (TTKey) o;

            if (!relativeRanks.equals(ttKey.relativeRanks)) return false;
            return lead == ttKey.lead;
        }

        @Override
        public int hashCode() {
            int result = relativeRanks.hashCode();
            result = 31 * result + lead.hashCode();
            return result;
        }
    }
}
