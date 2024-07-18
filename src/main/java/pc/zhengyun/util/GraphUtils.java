package pc.zhengyun.util;

import java.util.List;

public class GraphUtils {

    public static List<List<Integer>> OPTICS(double[][] matrix, double eps, int minPts) {
        OPTICS optics = new OPTICS(matrix, minPts, eps);
        optics.run();
        return optics.extractClustersAutomatically();
    }

    public static List<List<Integer>> DBSCAN(double[][] matrix, double eps, int minPts) {
        DBSCAN dbscan = new DBSCAN(matrix, eps, minPts);
        dbscan.run();
        return dbscan.extractClusters();
    }
}
