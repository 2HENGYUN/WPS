package pc.zhengyun.wps.impl.graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GraphGeneration {

    public void generate(String raw, String lab, List<? extends Coordinate> coordinates) {
        try {
            Graph graph = new Graph(new File(raw));
            for (Coordinate coordinate : coordinates) {
                graph.drawRect(coordinate.getX(), coordinate.getY(), coordinate.getWidth(), coordinate.getHeight());
            }
            graph.write(new File(lab));
        } catch (IOException e) {
            throw new RuntimeException("标注cluster遇到错误", e);
        }
    }
}
