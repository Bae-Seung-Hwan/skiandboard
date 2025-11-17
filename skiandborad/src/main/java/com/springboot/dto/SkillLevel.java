package com.springboot.dto;

public enum SkillLevel {
    BEGINNER,        // 초급
    INTERMEDIATE,    // 중급
    ADVANCED,        // 고급
    EXPERT;          // 숙련

    public static SkillLevel from(String v) {
        if (v == null) return INTERMEDIATE;
        v = v.trim().toUpperCase();
        // 한글 허용
        switch (v) {
            case "초급" -> { return BEGINNER; }
            case "중급" -> { return INTERMEDIATE; }
            case "고급" -> { return ADVANCED; }
            case "숙련", "최상급", "엑스퍼트" -> { return EXPERT; }
        }
        try { return SkillLevel.valueOf(v); } catch (Exception e) { return INTERMEDIATE; }
    }
}
