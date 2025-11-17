package com.springboot.dto;

public enum TransportMode {
    CAR, PUBLIC, SHUTTLE;

    public static TransportMode from(String v) {
        if (v == null) return CAR;
        v = v.trim().toUpperCase();
        return switch (v) {
            case "대중교통", "PUBLIC", "TRANSIT" -> PUBLIC;
            case "셔틀", "SHUTTLE", "버스" -> SHUTTLE;
            default -> CAR;
        };
    }
}
