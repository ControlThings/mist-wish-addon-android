package mistNodeApi;

/**
 * Created by akaustel on 12/8/16.
 */

public interface ErrorCallback {
    public void err(int code, String msg);
    public void end();
}
