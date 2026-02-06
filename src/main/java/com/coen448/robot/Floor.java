package com.coen448.robot;

public class Floor {
    private final int n;
    private final boolean[][] marked;

    public Floor(int n) {
        if (n <= 0) throw new IllegalArgumentException("Grid size must be positive");
        this.n = n;
        this.marked = new boolean[n][n];
    }

    public int size() {
        return n;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < n && y >= 0 && y < n;
    }

    public void clear() {
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                marked[y][x] = false;
            }
        }
    }

    public void mark(int x, int y) {
        if (!inBounds(x, y)) throw new IllegalArgumentException("Coordinates out of bounds: (" + x + ", " + y + ")");
        marked[y][x] = true;
    }

    public boolean isMarked(int x, int y) {
        if (!inBounds(x, y)) throw new IllegalArgumentException("Coordinates out of bounds: (" + x + ", " + y + ")");
        return marked[y][x];
    }


    public String renderWithIndices() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int x = 0; x < n; x++) {
            sb.append(String.format("%3d", x));
        }
        sb.append("\n");

        for (int y = 0; y < n; y++) {
            sb.append(String.format("%3d", y));
            for (int x = 0; x < n; x++) {
                sb.append(marked[y][x] ? "  *" : "   ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
