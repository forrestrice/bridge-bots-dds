package com.bridgebots.dds;

import java.util.EnumMap;

public enum Direction {
    NORTH(2),
    SOUTH(3),
    EAST(1),
    WEST(0);


    private final int nextOrdinal;

    Direction(int nextOrdinal) {
        this.nextOrdinal = nextOrdinal;
    }

    public Direction next() {
        return Direction.values()[nextOrdinal];
    }
}
