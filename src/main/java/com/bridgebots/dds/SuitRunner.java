package com.bridgebots.dds;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuitRunner {

    /**
     * @param hands in play order, starting with player on lead
     */
    /*
    public void runSuit(Map<Direction, List<Rank>> hands, Direction lead) {
        EnumSet<Rank> playedRanks = EnumSet.complementOf(
                hands.values().stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Rank.class)))
        );
        //Set<Rank> playedRanks = Set.
        runSuitRecursive(hands, playedCards, lead);


    }

    private void runSuitRecursive(Map<Direction, List<Rank>> hands, EnumSet<Rank> playedCards, Direction lead) {
        List<Rank> leadHand = hands.get(lead);
        List<Rank> distinctPlays = getDistinctPlays(leadHand, playedCards);
        for (Rank play : distinctPlays){
            makePlay(play);

            undoPlay(play);
        }

    }

    //Duplicates code in Board. Could be combined later
    private List<Rank> getDistinctPlays(List<Rank> leadHand, EnumSet<Rank> playedCards) {
        List<Rank> plays = new ArrayList<>();
        for (int i = 0; i < leadHand.size() - 1; i++) {
            Rank play = leadHand.get(i);
            Rank nextPlay = leadHand.get(i + 1);
            boolean touching = true;
            for (int j = play.rankScore + 1; j < nextPlay.rankScore; j++) {
                if (!playedCards.contains(Rank.fromScore(j))) {
                    touching = false;
                    break;
                }
            }
            if (!touching) {
                plays.add(play);
            }
        }
        plays.add(leadHand.get(leadHand.size() - 1));
        return plays;
    }


    public static class SuitRunnerResult {
        private final int tricks;
        private final boolean retainLead;

        public SuitRunnerResult(int tricks, boolean retainLead) {
            this.tricks = tricks;
            this.retainLead = retainLead;
        }
    }*/
}
