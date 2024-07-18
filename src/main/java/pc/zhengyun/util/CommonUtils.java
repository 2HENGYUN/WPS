package pc.zhengyun.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommonUtils {

    public static boolean eq(Object a, Object b) {
        return Objects.equals(a, b);
    }

    public static boolean ne(Object a, Object b) {
        return !eq(a, b);
    }

    public static int len(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static <T> Collection<T> uc(Collection<T> c) {
        return c == null ? Collections.emptyList() : c;
    }

    public static boolean t(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    public static boolean rb(Boolean b) {
        return !t(b);
    }

    public static List<String> readLines(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException("读取input-stream的lines出错", e);
        }
    }

    public static InputStream getResource(String resource) {
        return CommonUtils.class.getClassLoader().getResourceAsStream(resource);
    }

    public static InputStream getFile(String file) throws IOException {
        return Files.newInputStream(Paths.get(file));
    }
}
