package pc.zhengyun.wps.impl.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Graph {

    private final BufferedImage bufferedImage;
    private final Graphics2D graphics;
    private final int stroke = 4;

    public Graph(File file) throws IOException {
        this.bufferedImage = ImageIO.read(file);
        this.graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setColor(Color.RED);
        graphics.setStroke(new BasicStroke(stroke));
    }

    public void drawRect(int x, int y, int w, int h) {
        x *= 2;
        y *= 2;
        w *= 2;
        h *= 2;
        x += stroke / 2;
        y += stroke / 2;
        w -= stroke / 2;
        h -= stroke / 2;
        graphics.drawRect(x, y, w, h);
    }

    public void write(File file) throws IOException {
        ImageIO.write(bufferedImage, "png", file);
    }
}
