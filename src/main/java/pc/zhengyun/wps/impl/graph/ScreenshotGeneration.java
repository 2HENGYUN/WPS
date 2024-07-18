package pc.zhengyun.wps.impl.graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotGeneration {

    public void generate(String dest, WebDriver webDriver) {
        if (!(webDriver instanceof TakesScreenshot)) {
            throw new UnsupportedOperationException("暂时不支持此模拟器");
        }
        TakesScreenshot driver = (TakesScreenshot) webDriver;
        try {
            File file = new File(dest);
            file.getParentFile().mkdirs();
            Files.write(new File(dest).toPath(), driver.getScreenshotAs(OutputType.BYTES));
        } catch (IOException e) {
            throw new RuntimeException("保存原始截图出错", e);
        }
    }
}
