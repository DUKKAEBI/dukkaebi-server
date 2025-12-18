package com.ducami.dukkaebi.domain.user.domain.enums;

public enum GrowthType {
    WISP(0, 49),        // 도깨비불 0~49 점
    COPPER(50, 149),    // 동깨비 50~149
    IRON(150, 499),     // 철깨비 150~499
    SILVER(500, 999),   // 은깨비 500~999
    GOLD(1000, 2999),   // 금깨비 1000~2999
    JADE(3000, 4999),   // 옥깨비 3000~4999
    GOD(5000, Integer.MAX_VALUE); // 신깨비 5000~

    private final int minScore;
    private final int maxScore;

    GrowthType(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static GrowthType fromScore(int score) {
        for (GrowthType type : values()) {
            if (score >= type.minScore && score <= type.maxScore) {
                return type;
            }
        }
        return WISP; // 기본값
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
