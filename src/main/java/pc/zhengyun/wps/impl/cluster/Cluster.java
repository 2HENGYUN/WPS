package pc.zhengyun.wps.impl.cluster;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import pc.zhengyun.wps.impl.block.Block;
import pc.zhengyun.wps.impl.graph.Coordinate;

@Data
@AllArgsConstructor
public class Cluster implements Coordinate {

    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private List<Block> blocks;
}
