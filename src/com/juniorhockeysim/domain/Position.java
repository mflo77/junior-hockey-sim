package com.juniorhockeysim.domain;

public enum Position {
    FORWARD,
    DEFENSE,
    GOALIE;

    @Override
    public String toString() {
        switch (this) {
            case FORWARD: return "F";
            case DEFENSE: return "D";
            case GOALIE:  return "G";
            default: return name();
        }
    }
}
