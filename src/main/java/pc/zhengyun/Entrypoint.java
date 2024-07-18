package pc.zhengyun;

import static pc.zhengyun.util.CommonUtils.getFile;
import static pc.zhengyun.util.CommonUtils.readLines;
import static pc.zhengyun.util.CommonUtils.uc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import pc.zhengyun.wps.Wps;
import pc.zhengyun.wps.impl.DefaultWps;

public class Entrypoint {

    private static final String P_URLS = "urls";
    private static final String P_DRIVER = "driver";
    private static final String P_OUTPUT = "output";
    private static final String P_WIDTH = "width";
    private static final String P_HEIGHT = "height";
    private static final List<String> REQUIRED_PARAMS = new ArrayList<>();
    private static final List<String> OPTIONAL_PARAMS = new ArrayList<>();
    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();
    private static final String USAGE;

    static {
        Map<String, String> tips = new HashMap<>();
        tips.put(P_URLS, "urls-file-path");
        tips.put(P_DRIVER, "chrome-driver-path");
        tips.put(P_OUTPUT, "output-path");
        tips.put(P_WIDTH, "image-width");
        tips.put(P_HEIGHT, "image-height");
        REQUIRED_PARAMS.add(P_URLS);
        REQUIRED_PARAMS.add(P_DRIVER);
        OPTIONAL_PARAMS.add(P_OUTPUT);
        OPTIONAL_PARAMS.add(P_WIDTH);
        OPTIONAL_PARAMS.add(P_HEIGHT);
        DEFAULT_VALUES.put(P_OUTPUT, "." + File.separator + "output");
        DEFAULT_VALUES.put(P_WIDTH, "1920");
        DEFAULT_VALUES.put(P_HEIGHT, "1080");

        //拼凑cmd
        List<String> parts = new ArrayList<>();
        parts.add("调用命令: sh run.sh");
        REQUIRED_PARAMS.stream()
            .map(k -> "--" + k + "=<" + tips.getOrDefault(k, "?") + ">")
            .forEach(parts::add);
        OPTIONAL_PARAMS.stream()
            .map(k -> "[--" + k + "=<" + tips.getOrDefault(k, "?") + ">]")
            .forEach(parts::add);
        String cmd = String.join(" ", parts);

        //拼凑default
        parts.clear();
        parts.add("默认值:");
        OPTIONAL_PARAMS.stream()
            .map(k -> "    --" + k + " = " + DEFAULT_VALUES.getOrDefault(k, "?"))
            .forEach(parts::add);
        String defaults = String.join(System.lineSeparator(), parts);

        USAGE = String.join(System.lineSeparator(), cmd, defaults);
    }

    public static void main(String[] args) {
        if (Arrays.stream(args).anyMatch(p -> p.equals("--help") || p.equals("-h"))) {
            System.out.println(USAGE);
            return;
        }

        List<String> urls;
        String driverPath;
        File output;
        Integer width;
        Integer height;
        try {
            Map<String, String> params = parseParams(args);
            urls = getParam(params, P_URLS, p -> {
                try (InputStream is = getFile(p)) {
                    return readLines(is);
                } catch (IOException e) {
                    throw new IllegalArgumentException("读取urls出错", e);
                }
            });
            driverPath = getParam(params, P_DRIVER);
            output = getParam(params, P_OUTPUT, p -> {
                File file = new File(p);
                if (file.exists() && !file.isDirectory()) {
                    throw new IllegalArgumentException("指定的output不是文件夹");
                }
                file.mkdirs();
                return file;
            });
            width = getParam(params, P_WIDTH, Integer::parseInt);
            height = getParam(params, P_HEIGHT, Integer::parseInt);
        } catch (IllegalArgumentException e) {
            System.out.println("获取参数错误: " + e.getMessage());
            System.out.println();
            System.out.println(USAGE);
            return;
        }
        System.out.println("开始运行..............");
        System.out.println();
        entry(urls, driverPath, output, width, height);
    }

    private static Map<String, String> parseParams(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                String[] split = arg.split("=");
                if (split.length == 2) {
                    String key = split[0];
                    String value = split[1];
                    while (key.startsWith("-")) {
                        key = key.substring(1);
                    }
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private static <T> T getParam(Map<String, String> params, String key, Function<String, T> mapper) {
        String param = getParam(params, key);
        return mapper.apply(param);
    }

    private static String getParam(Map<String, String> params, String key) {
        boolean c = params.containsKey(key);
        if (!c) {
            if (REQUIRED_PARAMS.contains(key)) {
                throw new IllegalArgumentException("缺少必填参数: " + key);
            }
            if (OPTIONAL_PARAMS.contains(key)) {
                if (DEFAULT_VALUES.containsKey(key)) {
                    return DEFAULT_VALUES.get(key);
                }
                throw new RuntimeException("可选参数没有默认值: " + key);
            }
            throw new RuntimeException("未定义的参数: " + key);
        }
        return params.get(key);
    }

    private static void entry(List<String> urls, String driverPath, File output, Integer width, Integer height) {
        String base = output.getPath();
        if (!base.endsWith(File.separator)) {
            base += File.separator;
        }

        List<TestCase> testCases = tcs(base, urls);
        try (Wps wps = new DefaultWps(driverPath, width, height)) {
            for (TestCase testCase : testCases) {
                System.out.printf("-----------> Running: %d ----> %s\n", testCase.id, testCase.url);
                try {
                    wps.run(testCase.url, testCase.screen, testCase.blocks, testCase.clusters);
                } catch (Throwable e) {
                    System.out.printf("<----------- Failed: %d\n", testCase.id);
                    throw e;
                }
                System.out.printf("<----------- Success: %d ----> ( %s , %s , %s)\n", testCase.id, testCase.screen,
                    testCase.blocks, testCase.clusters);
            }
        }
    }

    private static List<TestCase> tcs(String base, List<String> urls) {
        List<TestCase> testCases = new ArrayList<>();
        int id = 1;
        for (String url : uc(urls)) {
            testCases.add(new TestCase(
                id,
                url,
                base + id + File.separator + "screen.png",
                base + id + File.separator + "blocks.png",
                base + id + File.separator + "clusters.png"
            ));
            id++;
        }
        return testCases;
    }

    @Data
    @AllArgsConstructor
    private static class TestCase {

        private final int id;
        private final String url;
        private final String screen;
        private final String blocks;
        private final String clusters;
    }
}
