package pc.zhengyun.wps.impl.dom;

import lombok.Data;

@Data
public class DomBounds {

    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Integer top;
    private Integer right;
    private Integer bottom;
    private Integer left;

    public Integer getRx() {
        return x + width;
    }

    public Integer getRy() {
        return y + height;
    }
}
