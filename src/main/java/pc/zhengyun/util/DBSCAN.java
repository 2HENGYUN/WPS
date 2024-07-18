package pc.zhengyun.util;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DBSCAN {

    private final double[][] matrix;
    private final double eps;
    private final int minPts;
    private boolean[] visited;

    public void run() {
        int n = matrix.length;
        int[] cnt = new int[n];
        //标记-紧密相连的边
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int d = matrix[i][j] <= eps ? 1 : 0;
                matrix[i][j] = matrix[j][i] = d;
                cnt[i]++;
                cnt[j]++;
            }
        }
        //取消标记-边界节点到边界节点的边
        for (int i = 0; i < n; i++) {
            if (0 < cnt[i] && cnt[i] < minPts - 1) {
                for (int j = 0; j < n; j++) {
                    if (0 < cnt[j] && cnt[j] < minPts - 1) {
                        matrix[i][j] = matrix[j][i] = 0;
                    }
                }
            }
        }
    }

    public List<List<Integer>> extractClusters() {
        int n = matrix.length;
        visited = new boolean[n];
        List<List<Integer>> subGraphs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                List<Integer> subgraph = new ArrayList<>();
                dfs(i, subgraph);
                subGraphs.add(subgraph);
            }
        }

        return subGraphs;
    }

    private void dfs(int vertex, List<Integer> subgraph) {
        visited[vertex] = true;
        subgraph.add(vertex);

        for (int neighbor = 0; neighbor < matrix[vertex].length; neighbor++) {
            if (matrix[vertex][neighbor] > 0 && !visited[neighbor]) {
                dfs(neighbor, subgraph);
            }
        }
    }
}
