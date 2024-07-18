package pc.zhengyun.wps;

import java.io.Closeable;

public interface Wps extends Closeable {

    void run(String url, String screenFile, String blocksFile, String clustersFile);

    @Override
    void close();
}
