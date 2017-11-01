package mistNodeApi.wishRequest;

import mistNodeApi.ErrorCallback;

/**
 * Created by jan on 8/30/17.
 */

public class Host {

    public static void config(ConfigCb callback) {
        HostConfig.request(callback);
    }

    public interface ConfigCb extends ErrorCallback {
        public void cb(String version, byte[] hid);
    }
}
