package com.springboot.dto;

public enum GearType {
    SKI, SNOWBOARD, BOTH;

    public static GearType from(String v) {
        if (v == null) return BOTH;
        v = v.trim().toUpperCase();
        return switch (v) {
            case "스키", "SKI" -> SKI;
            case "보드", "BOARD", "SNOWBOARD" -> SNOWBOARD;
            default -> BOTH;
        };
    }
}
