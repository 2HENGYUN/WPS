package pc.zhengyun.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

public class OPTICS {

    private double[][] distanceMatrix;
    private int minPts;
    private double eps;
    @Getter
    private int[] orderedPoints;
    @Getter
    private double[] reachabilityDistances;

    public OPTICS(double[][] distanceMatrix, int minPts, double eps) {
        this.distanceMatrix = distanceMatrix;
        this.minPts = minPts;
        this.eps = eps;
        this.orderedPoints = new int[distanceMatrix.length];
        this.reachabilityDistances = new double[distanceMatrix.length];
    }

    public void run() {
        boolean[] processed = new boolean[distanceMatrix.length];
        int currentPosition = 0;

        for (int i = 0; i < distanceMatrix.length; i++) {
            if (!processed[i]) {
                expandClusterOrder(i, processed, currentPosition);
            }
        }
    }

    private void expandClusterOrder(int point, boolean[] processed, int currentPosition) {
        processed[point] = true;
        orderedPoints[currentPosition] = point;
        currentPosition++;

        List<Integer> seeds = getNeighbors(point);
        if (seeds.size() >= minPts) {
            seeds.sort(Comparator.comparingDouble(p -> distanceMatrix[point][p]));

            while (!seeds.isEmpty()) {
                int currentPoint = seeds.remove(0);
                if (!processed[currentPoint]) {
                    processed[currentPoint] = true;
                    orderedPoints[currentPosition] = currentPoint;
                    reachabilityDistances[currentPosition] = Math.max(distanceMatrix[point][currentPoint],
                        getCoreDist(point));
                    currentPosition++;

                    List<Integer> newNeighbors = getNeighbors(currentPoint);
                    if (newNeighbors.size() >= minPts) {
                        mergeNewNeighbors(seeds, newNeighbors, currentPoint);
                    }
                }
            }
        }
    }

    private List<Integer> getNeighbors(int point) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < distanceMatrix.length; i++) {
            if (distanceMatrix[point][i] <= eps) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    private double getCoreDist(int point) {
        List<Integer> neighbors = getNeighbors(point);
        if (neighbors.size() < minPts) {
            return Double.POSITIVE_INFINITY;
        }
        neighbors.sort(Comparator.comparingDouble(p -> distanceMatrix[point][p]));
        return distanceMatrix[point][neighbors.get(minPts - 1)];
    }

    private void mergeNewNeighbors(List<Integer> seeds, List<Integer> newNeighbors, int centerPoint) {
        for (int neighbor : newNeighbors) {
            if (!seeds.contains(neighbor)) {
                int position = Collections.binarySearch(seeds, neighbor,
                    Comparator.comparingDouble(p -> distanceMatrix[centerPoint][p]));
                if (position < 0) {
                    seeds.add(-position - 1, neighbor);
                }
            }
        }
    }

    public List<List<Integer>> extractClustersAutomatically() {
        List<List<Integer>> clusters = new ArrayList<>();
        List<Integer> currentCluster = new ArrayList<>();

        for (int i = 0; i < orderedPoints.length; i++) {
            if (isLocalMaximum(i) || i == 0) {
                if (!currentCluster.isEmpty()) {
                    clusters.add(new ArrayList<>(currentCluster));
                    currentCluster.clear();
                }
            }
            currentCluster.add(orderedPoints[i]);
        }

        if (!currentCluster.isEmpty()) {
            clusters.add(currentCluster);
        }

        return clusters;
    }

    private boolean isLocalMaximum(int index) {
        if (index == 0) {
            return reachabilityDistances[index] > reachabilityDistances[index + 1];
        } else if (index == reachabilityDistances.length - 1) {
            return reachabilityDistances[index] > reachabilityDistances[index - 1];
        } else {
            return reachabilityDistances[index] > reachabilityDistances[index - 1] &&
                reachabilityDistances[index] > reachabilityDistances[index + 1];
        }
    }

    public static void main(String[] args) {
        // 示例使用
        double[][] distanceMatrix = {
            {0, 1, 4, 5, 8},
            {1, 0, 3, 4, 7},
            {4, 3, 0, 1, 4},
            {5, 4, 1, 0, 3},
            {8, 7, 4, 3, 0}
        };
        int minPts = 2;
        double eps = 5.0;

        OPTICS optics = new OPTICS(distanceMatrix, minPts, eps);
        optics.run();

        System.out.println("Ordered points: " + Arrays.toString(optics.getOrderedPoints()));
        System.out.println("Reachability distances: " + Arrays.toString(optics.getReachabilityDistances()));

        // 自动提取聚类
        List<List<Integer>> clusters = optics.extractClustersAutomatically();

        System.out.println("Automatically extracted clusters:");
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ": " + clusters.get(i));
        }
    }
}