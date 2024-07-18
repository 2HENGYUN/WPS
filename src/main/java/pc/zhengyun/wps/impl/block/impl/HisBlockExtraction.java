package pc.zhengyun.wps.impl.block.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pc.zhengyun.wps.impl.block.Block;
import pc.zhengyun.wps.impl.block.BlockExtraction;
import pc.zhengyun.wps.impl.dom.DomBounds;
import pc.zhengyun.wps.impl.dom.DomNode;
import pc.zhengyun.wps.impl.dom.DomVisualCues;
import pc.zhengyun.util.CommonUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HisBlockExtraction implements BlockExtraction {

    private Set<String> nonBlockNames = new HashSet<>(Arrays.asList("script", "noscript", "style"));
    private BlockRule blockRule = new BlockRule();

    @Override
    public List<Block> extract(DomNode body) {
        Block root = new Block();

        initBlock(body, root, 0, 0);
        divideBlock(root);
        refreshBlock(root);

        List<Block> blockList = new ArrayList<>();

        Queue<Block> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            Block curr = queue.poll();
            if (curr.getWidth() > 0 && curr.getHeight() > 0) {
                blockList.add(curr);
            }
            for (Block child : curr.getChildren()) {
                queue.offer(child);
            }
        }

        return blockList;
    }

    public void initBlock(DomNode box, Block block, int level, int idx) {
        block.getBoxes().add(box);
        block.setId(level + "-" + idx);
        block.setLevel(level);
        if (box.getNodeType() != 3) {
            int i = 0;
            for (DomNode b : CommonUtils.uc(box.getChildNodes())) {
                if (!nonBlockNames.contains(b.getNodeName())) {
                    Block child = new Block();
                    child.setParent(block);
                    block.getChildren().add(child);
                    initBlock(b, child, level + 1, i++);
                }
            }
        }
    }

    public void divideBlock(Block block) {
        if (CommonUtils.t(block.getIsDividable()) && blockRule.dividable(block)) {
            block.getChildren().removeIf(child -> CommonUtils.t(child.getToDel()));
            for (Block child : block.getChildren()) {
                divideBlock(child);
            }
        }
    }

    public void refreshBlock(Block block) {
        List<DomBounds> bounds = block.getBoxes().stream()
            .map(DomNode::getVisualCues)
            .filter(Objects::nonNull)
            .map(DomVisualCues::getBounds)
            .collect(Collectors.toList());
        int x = bounds.stream().mapToInt(DomBounds::getX).min().orElse(0);
        int y = bounds.stream().mapToInt(DomBounds::getY).min().orElse(0);
        int rx = bounds.stream().mapToInt(DomBounds::getRx).max().orElse(0);
        int ry = bounds.stream().mapToInt(DomBounds::getRy).max().orElse(0);
        int width = rx - x;
        int height = ry - y;
        block.setX(x);
        block.setY(y);
        block.setWidth(width);
        block.setHeight(height);
        for (Block child : block.getChildren()) {
            refreshBlock(child);
        }
    }
}
