package pc.zhengyun.wps.impl.block.impl;

import static pc.zhengyun.util.CommonUtils.eq;
import static pc.zhengyun.util.CommonUtils.len;
import static pc.zhengyun.util.CommonUtils.ne;
import static pc.zhengyun.util.CommonUtils.uc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pc.zhengyun.wps.impl.block.Block;
import pc.zhengyun.util.CommonUtils;
import pc.zhengyun.wps.impl.dom.DomBounds;
import pc.zhengyun.wps.impl.dom.DomNode;
import pc.zhengyun.wps.impl.dom.DomVisualCues;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockRule {

    private int threshold = 40000;
    private Set<String> nonBlockSet = new HashSet<>(
        Arrays.asList("a", "abbr", "acronym", "b", "bdo", "big", "br", "button", "cite", "code", "dfn", "em", "i",
            "img", "input", "kbd", "label", "map", "object", "q", "samp", "script", "select", "small", "span", "strong",
            "sub", "sup", "textarea", "time", "tt"));

    public boolean dividable(Block block) {
        DomNode box = block.getBoxes().get(0);
        if (box.getNodeType() == 3) {
            return false;
        }
        String name = box.getNodeName();

        if (eq(name, "img")) {
            return false;
        }
        if (isBlock(name)) {
            return inlineRules(block);
        }
        if (eq(name, "table")) {
            return tableRules(block);
        }
        if (eq(name, "tr")) {
            return trRules(block);
        }
        if (eq(name, "td")) {
            return tdRules(block);
        }
        if (eq(name, "p")) {
            return pRules(block);
        }
        return otherRules(block);
    }

    public boolean otherRules(Block block) {
        return rule1(block)
            || rule2(block)
            || rule3(block)
            || rule4(block)
            || rule6(block)
            || rule7(block)
            || rule9(block)
            || rule10(block)
            || rule12(block);
    }

    public boolean pRules(Block block) {
        return rule1(block)
            || rule2(block)
            || rule3(block)
            || rule4(block)
            || rule5(block)
            || rule6(block)
            || rule7(block)
            || rule9(block)
            || rule10(block)
            || rule12(block);
    }

    public boolean tdRules(Block block) {
        return rule1(block)
            || rule2(block)
            || rule3(block)
            || rule4(block)
            || rule9(block)
            || rule10(block)
            || rule11(block)
            || rule13(block);
    }

    public boolean trRules(Block block) {
        return rule1(block)
            || rule2(block)
            || rule3(block)
            || rule7(block)
            || rule8(block)
            || rule10(block)
            || rule13(block);
    }

    public boolean tableRules(Block block) {
        return rule1(block)
            || rule2(block)
            || rule3(block)
            || rule8(block)
            || rule10(block)
            || rule13(block);
    }

    public boolean inlineRules(Block block) {
        return rule1(block)
            || rule2(block)
            || rule3(block)
            || rule4(block)
            || rule5(block)
            || rule6(block)
            || rule7(block)
            || rule9(block)
            || rule10(block)
            || rule12(block);
    }


    // Rule 1: If the DOM node is not a text node and it has no valid children,
    // then this node cannot be divided and will be cut.
    public boolean rule1(Block block) {
        DomNode node = block.getBoxes().get(0);
        if (!isTextNode(node) && !hasValidChildNode(node)) {
            block.setToDel(true);
            return false;
        }
        return true;
    }

    // Rule 2:If the DOM node has only one valid child and the child is not a text node,
    // then divide this node.
    public boolean rule2(Block block) {
        List<Block> children = block.getChildren();
        if (len(children) == 1) {
            DomNode node = children.get(0).getBoxes().get(0);
            return isValidNode(node) && !isTextNode(node);
        }
        return false;
    }

    // Rule 3: If the DOM node is the root node of the sub-DOM tree (corresponding to the block),
    // and there is only one sub DOM tree corresponding to this block, divide this node.
    public boolean rule3(Block block) {
        DomNode node = block.getBoxes().get(0);

        return 1 == block.getChildren().stream()
            .map(Block::getBoxes)
            .map(l -> l.get(0))
            .filter(box -> isOnlyOneDomSubTree(node, box))
            .count();
    }

    // Rule 4: If all of the child nodes of the DOM node are text nodes or virtual text nodes,
    // do not divide the node.
    // If the font size and font weight of all these child nodes are same,
    // set the DoC of the extracted block to 10.
    // Otherwise, set the DoC of this extracted block to 9.
    public boolean rule4(Block block) {
        List<DomNode> subBoxList = block.getBoxes().get(0).getChildNodes();
        if (uc(subBoxList).stream().allMatch(box -> isTextNode(box) || isVirtualTextNode(box))) {
            List<DomVisualCues> vcList = uc(subBoxList).stream()
                .map(DomNode::getVisualCues)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            if (vcList.stream().map(DomVisualCues::getFontSize).distinct().count() > 1
                || vcList.stream().map(DomVisualCues::getFontWeight).distinct().count() > 1) {
                block.setDoc(9);
            }
            return false;
        }
        return true;
    }

    // Rule 5:  If one of the child nodes of the DOM node is line-break node, then divide this DOM node
    public boolean rule5(Block block) {
        return block.getBoxes().get(0).getChildNodes().stream()
            .map(DomNode::getNodeName)
            .map(this::isBlock)
            .anyMatch(CommonUtils::rb);
    }

    // Rule 6: If one of the child nodes of the DOM node has HTML tag <HR>,
    // then divide this DOM node
    public boolean rule6(Block block) {
        return uc(block.getBoxes().get(0).getChildNodes()).stream()
            .map(DomNode::getNodeName)
            .anyMatch(n -> eq(n, "hr"));
    }

    // Rule 7: If the sum of all the child nodes' size is greater than this DOM node's size,
    // then divide this node.
    public boolean rule7(Block block) {
        DomNode node = block.getBoxes().get(0);
        if (node.getVisualCues() == null) {
            return false;
        }
        DomBounds b = node.getVisualCues().getBounds();
        return uc(node.getChildNodes()).stream()
            .map(DomNode::getVisualCues)
            .filter(Objects::nonNull)
            .map(DomVisualCues::getBounds)
            .anyMatch(cb -> cb.getX() < b.getX()
                || cb.getY() < b.getY()
                || cb.getRx() > b.getRx()
                || cb.getRy() > b.getRy());
    }

    // Rule 8: If the background color of this node is different from one of its children's,
    // divide this node and at the same time,
    // the child node with different background color will not be divided in this round.
    // Set the DoC value (6-8) for the child node based on the html tag of the child node and the size of the child node.
    public boolean rule8(Block block) {
        boolean ret = false;
        String bColor = block.getBoxes().get(0).getVisualCues().getBackgroundColor();
        for (Block b : block.getChildren()) {
            if (ne(b.getBoxes().get(0).getVisualCues().getBackgroundColor(), bColor)) {
                b.setIsDividable(false);
                rule13(b);
                ret = true;
            }
        }
        return ret;
    }

    // Rule 9: If the node has at least one text node child or at least one virtual text node child,
    // and the node's relative size is smaller than a threshold, then the node cannot be divided
    // Set the DoC value (from 5-8) based on the html tag of the node
    public boolean rule9(Block block) {
        DomNode node = block.getBoxes().get(0);

        int count = (int) uc(node.getChildNodes()).stream()
            .filter(box -> isTextNode(box) || isVirtualTextNode(box))
            .count();
        if (count > 0) {
            DomBounds bounds = node.getVisualCues().getBounds();
            if (bounds.getX() * bounds.getY() < threshold) {
                rule13(block);
                return false;
            }
        }
        return true;
    }

    // Rule 10: If the child of the node with maximum size are smaller than a threshold (relative size),
    // do not divide this node.
    // Set the DoC based on the html tag and size of this node.
    public boolean rule10(Block block) {
        DomNode node = block.getBoxes().get(0);
        int maxSize = uc(node.getChildNodes()).stream()
            .map(DomNode::getVisualCues)
            .filter(Objects::nonNull)
            .map(DomVisualCues::getBounds)
            .mapToInt(bounds -> bounds.getX() * bounds.getY())
            .max()
            .orElse(0);
        if (maxSize < threshold) {
            rule13(block);
            return false;
        }
        return true;
    }

    // Rule 11: If previous sibling node has not been divided, do not divide this node
    public boolean rule11(Block block) {
        List<Block> children = block.getParent().getChildren();
        int i = children.indexOf(block);
        return children.subList(0, i).stream()
            .map(Block::getIsDividable)
            .anyMatch(CommonUtils::t);
    }

    // Rule 12: Divide this node.
    public boolean rule12(Block block) {
        return true;
    }

    // Rule 13: Do not divide this node
    // Set the DoC value based on the html tag and size of this node.
    public boolean rule13(Block block) {
        block.setDoc(getDocByTagSize("", 0));
        return false;
    }

    public boolean hasValidChildNode(DomNode node) {
        List<DomNode> childNodes = node.getChildNodes();
        return childNodes != null && childNodes.stream().anyMatch(this::isValidNode);
    }

    // a node that can be seen through the browser.
    // The node's width and height are not equal to zero.
    public boolean isValidNode(DomNode node) {
        DomVisualCues visualCues = node.getVisualCues();
        return visualCues != null
            && ne(visualCues.getDisplay(), "none")
            && ne(visualCues.getVisibility(), "hidden")
            && ne(visualCues.getBounds().getWidth(), "0px")
            && ne(visualCues.getBounds().getHeight(), "0px");
    }

    // the DOM node corresponding to free text, which does not have an html tag
    public boolean isTextNode(DomNode node) {
        return node.getNodeType() == 3;
    }

    // Virtual text node (recursive definition):
    //    Inline node with only text node children is a virtual text node.
    //    Inline node with only text node and virtual text node children is a virtual text node.
    public boolean isVirtualTextNode(DomNode node) {
        List<DomNode> childNodes = node.getChildNodes();
        return childNodes == null || childNodes.stream().allMatch(box -> isTextNode(box) && isVirtualTextNode(box));
    }

    public boolean isBlock(String name) {
        return !nonBlockSet.contains(name);
    }

    public boolean isOnlyOneDomSubTree(DomNode pattern, DomNode target) {
        if (ne(pattern.getNodeName(), target.getNodeName())) {
            return false;
        }
        List<DomNode> patternChildren = pattern.getChildNodes();
        List<DomNode> targetChildren = target.getChildNodes();
        if (len(patternChildren) != len(targetChildren)) {
            return false;
        }
        for (int i = 0; i < len(patternChildren); i++) {
            if (!isOnlyOneDomSubTree(patternChildren.get(i), targetChildren.get(i))) {
                return false;
            }
        }
        return true;
    }

    public int getDocByTagSize(String tag, int size) {
        return 7;
    }
}
