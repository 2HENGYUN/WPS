package pc.zhengyun.wps.impl.dom;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class DomVisualCues {

    private DomBounds bounds;
    @JSONField(name = "font-size")
    private String fontSize;
    @JSONField(name = "font-weight")
    private String fontWeight;
    @JSONField(name = "background-color")
    private String backgroundColor;
    private String display;
    private String visibility;
    private Integer opacity;
    private String text;
    private String className;
}
