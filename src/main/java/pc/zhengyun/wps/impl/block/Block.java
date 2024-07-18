package pc.zhengyun.wps.impl.block;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pc.zhengyun.wps.impl.dom.DomNode;
import pc.zhengyun.wps.impl.graph.Coordinate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block implements Coordinate {

    private String id;
    private Integer x = 0;
    private Integer y = 0;
    private Integer width = 0;
    private Integer height = 0;
    private Boolean isDividable = true;
    private List<DomNode> boxes = new ArrayList<>();

    private Integer Doc = 0;
    private Boolean toDel = false;

    private Block parent = null;
    private List<Block> children = new ArrayList<>();

    private int level = 0;

    public Integer getRx() {
        return x + width;
    }

    public Integer getRy() {
        return y + height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Block)) {
            return false;
        }

        Block block = (Block) o;

        return id.equals(block.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s : %s [%d %d %d %d]",
            id,
            boxes.isEmpty() ? "<EMPTY>" : boxes.get(0).getNodeName(),
            x, y, width, height);
    }
}
