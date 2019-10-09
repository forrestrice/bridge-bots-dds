package com.bridgebots.dds;

public enum Direction {
    NORTH(1, 2, 3),
    EAST(2, 3, 0),
    SOUTH(3, 0,  1),
    WEST(0, 1,2);


    private final int nextOrdinal;
    private final int partnerOrdinal;
    private final int previousOrdinal;

    Direction(int nextOrdinal, int partnerOrdinal, int previousOrdinal) {
        this.nextOrdinal = nextOrdinal;
        this.partnerOrdinal = partnerOrdinal;
        this.previousOrdinal = previousOrdinal;
    }

    public Direction next() {
        return Direction.values()[nextOrdinal];
    }

    public Direction previous() {
        return Direction.values()[previousOrdinal];
    }

    public Direction partner(){
        return Direction.values()[partnerOrdinal];
    }

    public Direction offset(int offset){
        return Direction.values()[(this.ordinal() + offset) % 4];
    }
}
