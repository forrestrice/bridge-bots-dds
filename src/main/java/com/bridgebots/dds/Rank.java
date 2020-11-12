package com.bridgebots.dds;

import com.google.common.collect.ImmutableMap;

import java.util.Map;


public enum Rank {


    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14);

    private static final Map<String, Rank> RANK_STRINGS = ImmutableMap.<String, Rank>builder()
            .put("2", Rank.TWO)
            .put("3", Rank.THREE)
            .put("4", Rank.FOUR)
            .put("5", Rank.FIVE)
            .put("6", Rank.SIX)
            .put("7", Rank.SEVEN)
            .put("8", Rank.EIGHT)
            .put("9", Rank.NINE)
            .put("10", Rank.TEN)
            .put("T", Rank.TEN)
            .put("J", Rank.JACK)
            .put("Q", Rank.QUEEN)
            .put("K", Rank.KING)
            .put("A", Rank.ACE)
            .build();

    public final int rankScore;

    Rank(int rankScore) {
        this.rankScore = rankScore;
    }

    public static Rank parseRank(String rankString) {
        return RANK_STRINGS.get(rankString);
    }

    public static Rank fromScore(int score) {
        return Rank.values()[score - 2];
    }
}
