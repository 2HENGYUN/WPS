package pc.zhengyun.wps.impl.block;

import java.util.List;
import pc.zhengyun.wps.impl.dom.DomNode;

public interface BlockExtraction {

    List<Block> extract(DomNode body);
}
