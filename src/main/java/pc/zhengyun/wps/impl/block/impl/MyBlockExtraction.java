package pc.zhengyun.wps.impl.block.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import pc.zhengyun.wps.impl.block.Block;
import pc.zhengyun.wps.impl.dom.DomNode;

@Data
public class MyBlockExtraction extends HisBlockExtraction {

    private Set<String> ignoredNodeNames = new HashSet<>(
        Arrays.asList("body", "div", "iframe", "table", "dd", "dl", "li", "ul"));

    @Override
    public List<Block> extract(DomNode body) {
        List<Block> blockList = super.extract(body);
        blockList.removeIf(block -> {
            String nodeName = block.getBoxes().get(0).getNodeName();
            return ignoredNodeNames.contains(nodeName);
        });
        return blockList;
    }
}
