package com.springboot.service;

public class KmaGridConverter {

    public record Point(int nx, int ny) {}
    public static Point toGrid(double lat, double lon) {
        
        return new Point(60, 127); 
    }
}
