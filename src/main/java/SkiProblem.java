import javaslang.Function2;
import javaslang.Tuple;
import javaslang.collection.Seq;
import javaslang.collection.Stream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by eckyputrady on 8/7/16.
 */
public class SkiProblem {
    public static PathStats run(String fileName) {
        // read map
        SkiMap map = readMap(fileName);

        // get each point's distance and steepness lazily
        Seq<PathStats> paths = Stream.range(0, map.length()).map(idx -> memGetPathStats.apply(map, idx));

        // find the longest distance and the steepest point
        PathStats best = paths.foldLeft(null, SkiProblem::getBestPath);

        return best;
    }

    public static PathStats getBestPath(PathStats prev, PathStats cur) {
        if (prev == null) return cur;
        if (cur.length > prev.length) return cur;
        if (cur.length == prev.length && cur.steepness > prev.steepness) return cur;
        return prev;
    }

    // Cache the result of each call to getPathStats
    private static Function2<SkiMap,Integer,PathStats> memGetPathStats = Function2.of(SkiProblem::getPathStats).memoized();

    private static PathStats getPathStats(SkiMap map, int idx) {
        int elevation = map.get(idx);
        Seq<Integer> eligibleNextIndices = map.getNeighborIndicesOf(idx).filter(x -> map.get(x) < elevation);
        Seq<PathStats> allPathStats = eligibleNextIndices.map(x -> memGetPathStats.apply(map, x));
        PathStats best = allPathStats.foldLeft(null, SkiProblem::getBestPath);

        if (best == null)
            return new PathStats(elevation, 1, 0);
        else
            return new PathStats(elevation, best.length + 1, best.steepness + (elevation - best.startingElevation));
    }

    private static SkiMap readMap(String file) {
        try(Scanner sc = new Scanner(new BufferedReader(new FileReader(file)))) {
            SkiMap map = new SkiMap(sc.nextInt(), sc.nextInt());
            for (int i = 0; i < map.length(); ++i)
                map.put(i, sc.nextInt());
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return new SkiMap(0,0);
        }
    }

    private static class PathStats {

        public final int startingElevation;
        public final int length;
        public final int steepness;

        private PathStats(int startingElevation, int length, int steepness) {
            this.startingElevation = startingElevation;
            this.length = length;
            this.steepness = steepness;
        }

        @Override
        public String toString() {
            return "PathStats{" +
                    "length=" + length +
                    ", steepness=" + steepness +
                    '}';
        }
    }

    private static class SkiMap {
        private final int[][] map;
        private final int width;
        private final int height;
        private final int length;

        private SkiMap(int width, int height) {
            this.width = width;
            this.height = height;
            length = width * height;
            map = new int[height][width];
        }

        public void put(int idx, int data) {
            map[getY(idx)][getX(idx)] = data;
        }

        public int get(int idx) {
            return map[getY(idx)][getX(idx)];
        }

        public int length() {
            return length;
        }

        public Seq<Integer> getNeighborIndicesOf(int idx) {
            return Stream.of(
                    Tuple.of(getX(idx) + 1, getY(idx)), // E
                    Tuple.of(getX(idx) - 1, getY(idx)), // W
                    Tuple.of(getX(idx), getY(idx) + 1), // S
                    Tuple.of(getX(idx), getY(idx) - 1) // N
            )
            .filter(tuple -> tuple._1 >= 0 && tuple._1 < width && tuple._2 >= 0 && tuple._2 < height)
            .map(tuple -> getIdx(tuple._1, tuple._2));
        }

        private int getX(int idx) {
            return idx % width;
        }

        private int getY(int idx) {
            return idx / width;
        }

        private int getIdx(int x, int y) {
            return (y * width) + x;
        }
    }
}
