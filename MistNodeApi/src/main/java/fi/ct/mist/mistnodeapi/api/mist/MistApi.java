package fi.ct.mist.mistnodeapi.api.mist;

/**
 * Created by jeppe on 11/14/16.
 */

class MistApi {

    private MistApi() {
    }


    private static class MistApiHolder {
        private static final MistApi INSTANCE = new MistApi();
    }

    public static MistApi getInstance() {
        return MistApiHolder.INSTANCE;
    }

    synchronized native void write(Endpoint ep);

    synchronized native void read(Endpoint ep);

    synchronized native void invoke(Endpoint ep);

    synchronized native void startMistApp(String appName);


}