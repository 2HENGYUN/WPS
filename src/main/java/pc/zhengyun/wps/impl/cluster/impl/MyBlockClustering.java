package pc.zhengyun.wps.impl.cluster.impl;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pc.zhengyun.wps.impl.block.Block;
import pc.zhengyun.wps.impl.cluster.BlockClustering;
import pc.zhengyun.wps.impl.cluster.Cluster;
import pc.zhengyun.util.GraphUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyBlockClustering implements BlockClustering {

    private int outlierSize;
    private int alignGap;
    private double alpha;
    private double beta;
    private int minPts = 3;

    public MyBlockClustering(int width, int height) {
        outlierSize = 1;
        alignGap = 2;
        beta = min(width, height);
    }

    @Override
    public List<Cluster> clustering(List<Block> blockList) {
        blockList.removeIf(block -> block.getWidth() <= outlierSize || block.getHeight() <= outlierSize);

        int N = blockList.size();

        double[][] matrix = new double[N][N];
        List<Double> validDis = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            Block a = blockList.get(i);
            for (int j = i + 1; j < N; j++) {
                Block b = blockList.get(j);
                double dv = visualDistance(a, b);
                if (dv < beta / 2) {
                    validDis.add(dv);
                }
                matrix[i][j] = matrix[j][i] = dv;
            }
        }

        double eps = validDis.stream().mapToDouble(d -> d).sum() / (validDis.size() + 1);

        List<List<Integer>> clusters = GraphUtils.DBSCAN(matrix, eps, minPts);
        return clusters.stream()
            .map(c -> c.stream().map(blockList::get).collect(Collectors.toList()))
            .map(this::cluster)
            .collect(Collectors.toList());
    }

    private double visualDistance(Block a, Block b) {
        int dx = distance(a.getX(), a.getRx(), b.getX(), b.getRx());
        int dy = distance(a.getY(), a.getRy(), b.getY(), b.getRy());
        return dx + dy;
    }

    private int distance(int al, int ar, int bl, int br) {
        int dl = al - bl;
        int dr = ar - br;
        int t = dl * dr;
        int d = min(abs(dl), abs(dr));
        if (t <= 0) {
            return d;
        }
        int dc = ar - bl;
        return min(d, abs(dc));
    }

    private Cluster cluster(List<Block> blocks) {
        int x = blocks.stream().mapToInt(Block::getX).min().orElse(0);
        int y = blocks.stream().mapToInt(Block::getY).min().orElse(0);
        int rx = blocks.stream().mapToInt(Block::getRx).max().orElse(0);
        int ry = blocks.stream().mapToInt(Block::getRy).max().orElse(0);
        int w = rx - x;
        int h = ry - y;
        return new Cluster(x, y, w, h, blocks);
    }
}
