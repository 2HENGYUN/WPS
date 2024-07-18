package pc.zhengyun.wps.impl.dom.impl;

import static pc.zhengyun.util.CommonUtils.getResource;
import static pc.zhengyun.util.CommonUtils.readLines;

import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import pc.zhengyun.wps.impl.dom.DomExtraction;
import pc.zhengyun.wps.impl.dom.DomNode;

public class HisDomExtraction implements DomExtraction {

    @Override
    public DomNode extract(WebDriver webDriver) {
        if (!(webDriver instanceof JavascriptExecutor)) {
            throw new UnsupportedOperationException("暂时不支持此模拟器");
        }
        JavascriptExecutor driver = (JavascriptExecutor) webDriver;
        String js;
        try (InputStream is = getResource("dom.js")) {
            List<String> lines = readLines(is);
            js = String.join(System.lineSeparator(), lines);
        } catch (IOException e) {
            throw new RuntimeException("读取dom.js出错", e);
        }
        return JSON.parseObject((String) driver.executeScript(js), DomNode.class);
    }
}
