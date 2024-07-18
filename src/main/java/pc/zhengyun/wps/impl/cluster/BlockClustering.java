package pc.zhengyun.wps.impl.cluster;

import java.util.List;
import pc.zhengyun.wps.impl.block.Block;

public interface BlockClustering {

    List<Cluster> clustering(List<Block> blockList);
}
