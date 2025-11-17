package com.springboot.dto;

public enum RegionCode {
    SEOUL_GYEONGGI, // 수도권
    GANGWON,
    CHUNGCHEONG,
    JEOLLA,
    GYEONGSANG,
    JEJU,
    ANY;

    public static RegionCode from(String v) {
        if (v == null) return ANY;
        v = v.trim().toUpperCase();
        return switch (v) {
            case "수도권", "서울경기", "SEOUL_GYEONGGI", "CAPITAL" -> SEOUL_GYEONGGI;
            case "강원", "GANGWON" -> GANGWON;
            case "충청", "CHUNGCHEONG" -> CHUNGCHEONG;
            case "전라", "JEOLLA" -> JEOLLA;
            case "경상", "GYEONGSANG" -> GYEONGSANG;
            case "제주", "JEJU" -> JEJU;
            default -> ANY;
        };
    }
}
