package com.bridgebots.dds;

import java.util.EnumMap;
import java.util.Map;

public class Deal {
    private final Map<Direction, Hand> hands;
    private final Direction dealer;
    private final boolean nsVulnerable;
    private final boolean ewVulnerable;

    public Deal(Hand north, Hand south, Hand east, Hand west, Direction dealer, boolean nsVulnerable, boolean ewVulnerable) {
        this.dealer = dealer;
        this.nsVulnerable = nsVulnerable;
        this.ewVulnerable = ewVulnerable;
        this.hands = new EnumMap<>(Direction.class);
        hands.put(Direction.NORTH, north);
        hands.put(Direction.SOUTH, south);
        hands.put(Direction.EAST, east);
        hands.put(Direction.WEST, west);
    }

    public Map<Direction, Hand> getHands() {
        return hands;
    }
}
