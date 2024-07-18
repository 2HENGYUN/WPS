package pc.zhengyun.wps.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pc.zhengyun.wps.Wps;
import pc.zhengyun.wps.impl.block.Block;
import pc.zhengyun.wps.impl.block.BlockExtraction;
import pc.zhengyun.wps.impl.block.impl.MyBlockExtraction;
import pc.zhengyun.wps.impl.cluster.BlockClustering;
import pc.zhengyun.wps.impl.cluster.Cluster;
import pc.zhengyun.wps.impl.cluster.impl.MyBlockClustering;
import pc.zhengyun.wps.impl.dom.DomExtraction;
import pc.zhengyun.wps.impl.dom.DomNode;
import pc.zhengyun.wps.impl.dom.impl.HisDomExtraction;
import pc.zhengyun.wps.impl.graph.GraphGeneration;
import pc.zhengyun.wps.impl.graph.ScreenshotGeneration;

@Data
@AllArgsConstructor
public class DefaultWps implements Wps {

    public DefaultWps(String driverPath, int width, int height) {
        // 创建WebDriver实例
        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=" + width + "," + height);

        // 初始化WebDriver
        driver = new ChromeDriver(options);
        screenshotGeneration = new ScreenshotGeneration();
        domExtraction = new HisDomExtraction();
        blockExtraction = new MyBlockExtraction();
        blockClustering = new MyBlockClustering(width, height);
        graphGeneration = new GraphGeneration();
    }

    private WebDriver driver;
    private ScreenshotGeneration screenshotGeneration;
    private DomExtraction domExtraction;
    private BlockExtraction blockExtraction;
    private BlockClustering blockClustering;
    private GraphGeneration graphGeneration;

    @Override
    public void run(String url, String screenFile, String blocksFile, String clustersFile) {
        driver.get(url);
        screenshotGeneration.generate(screenFile, driver);
        DomNode root = domExtraction.extract(driver);
        List<Block> blockList = blockExtraction.extract(root);
        graphGeneration.generate(screenFile, blocksFile, blockList);
        List<Cluster> clusters = blockClustering.clustering(blockList);
        graphGeneration.generate(screenFile, clustersFile, clusters);
    }

    @Override
    public void close() {
        driver.quit();
    }
}
