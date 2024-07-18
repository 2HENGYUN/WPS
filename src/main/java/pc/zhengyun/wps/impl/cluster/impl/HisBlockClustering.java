package pc.zhengyun.wps.impl.cluster.impl;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static pc.zhengyun.util.CommonUtils.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class HisBlockClustering implements BlockClustering {

    private int outlierSize;
    private int alignGap;
    private double alpha;
    private double beta;
    private int minPts = 2;

    public HisBlockClustering(int width, int height) {
        outlierSize = 1;
        alignGap = 2;
        alpha = max(width, height);
        beta = min(width, height);
    }

    @Override
    public List<Cluster> clustering(List<Block> blockList) {
        blockList.removeIf(block -> block.getWidth() <= outlierSize || block.getHeight() <= outlierSize);

        int N = blockList.size();
        int T = N > 0 ? treeDepth(blockList.get(0)) : 1;
        double alpha = this.alpha / T;

        double[][] matrix = new double[N][N];
        List<Double> validDis = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            Block a = blockList.get(i);
            for (int j = i + 1; j < N; j++) {
                Block b = blockList.get(j);
                double dv = visualDistance(a, b) * N / alpha;
                if (dv < beta / 2) {
                    validDis.add(dv);
                }
                double dl = logicalDistance(a, b);
                if (dl == -1) {
                    dl = 2 * (T - 1);
                }
                double da = alignDistance(a, b);
                double d = dv + alpha * (dl * (2 - da) / 2);
                matrix[i][j] = matrix[j][i] = d;
            }
        }

        double eps = validDis.stream().mapToDouble(d -> d).sum() / (validDis.size() + 1);

        List<List<Integer>> clusters = GraphUtils.DBSCAN(matrix, eps, minPts);
        return clusters.stream()
            .map(c -> c.stream().map(blockList::get).collect(Collectors.toList()))
            .map(this::mergeBlocks)
            .collect(Collectors.toList());
    }

    private double visualDistance(Block b1, Block b2) {
        int x_cor = (b1.getX() - b2.getX()) * (b1.getX() + b1.getWidth() - b2.getX() - b2.getWidth());
        int y_cor = (b1.getY() - b2.getY()) * (b1.getY() + b1.getHeight() - b2.getY() - b2.getHeight());
        int dx = x_cor <= 0 ? 0 : min(abs(b1.getX() - b2.getX()),
            abs(b1.getX() + b1.getWidth() - b2.getX() - b2.getWidth()));
        int dy = y_cor <= 0 ? 0 : min(abs(b1.getY() - b2.getY()),
            abs(b1.getY() + b1.getHeight() - b2.getY() - b2.getHeight()));
        return dx + dy;
    }

    private double logicalDistance(Block a, Block b) {
        Set<Block> path = new HashSet<>();
        Block c = a;
        while (c != null) {
            path.add(c);
            c = c.getParent();
        }
        c = b;
        while (c != null) {
            if (path.contains(c)) {
                return a.getLevel() + b.getLevel() - 2 * c.getLevel();
            }
            c = c.getParent();
        }
        return -1;
    }

    private double alignDistance(Block a, Block b) {
        double r = 0;
        if (abs(a.getX() - b.getX()) <= alignGap
            || abs(a.getRx() - b.getRx()) <= alignGap
            || abs(a.getY() - b.getY()) <= alignGap
            || abs(a.getRy() - b.getRy()) <= alignGap) {
            r = 0.8;
        }
        if (r == 0.8) {
            if (eq(a.getWidth(), b.getWidth())
                || eq(a.getHeight(), b.getHeight())) {
                r = 1.0;
            }
        }
        return r;
    }

    private Cluster mergeBlocks(List<Block> blocks) {
        int x = blocks.stream().mapToInt(Block::getX).min().orElse(0);
        int y = blocks.stream().mapToInt(Block::getY).min().orElse(0);
        int rx = blocks.stream().mapToInt(Block::getRx).max().orElse(0);
        int ry = blocks.stream().mapToInt(Block::getRy).max().orElse(0);
        int w = rx - x;
        int h = ry - y;
        return new Cluster(x, y, w, h, blocks);
    }

    private int treeDepth(Block block) {
        while (block.getParent() != null) {
            block = block.getParent();
        }
        return dfs(block);
    }

    private int dfs(Block block) {
        if (block == null) {
            return 0;
        }
        return block.getChildren().stream().mapToInt(this::dfs).max().orElse(0) + 1;
    }
}
