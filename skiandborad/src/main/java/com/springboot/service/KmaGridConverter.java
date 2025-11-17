package com.springboot.service;

public class KmaGridConverter {
    // 그대로 사용 가능 (생략 안 함 원하면 전체 코드 넣어줄게)
    public record Point(int nx, int ny) {}
    public static Point toGrid(double lat, double lon) {
        // ... 변환 코드 ...
        return new Point(60, 127); // TODO 실제 구현 넣기
    }
}
