package pc.zhengyun.wps.impl.dom;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.List;
import lombok.Data;

@Data
public class DomNode {

    private Integer nodeType;
    private String tagName;
    private String nodeName;
    private String nodeValue;
    @JSONField(name = "visual_cues")
    private DomVisualCues visualCues;
    private List<DomNode> childNodes;

    public String getNodeName() {
        return nodeName == null ? tagName : nodeName;
    }
}
