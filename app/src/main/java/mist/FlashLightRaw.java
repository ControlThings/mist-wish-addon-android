package mist;

import android.content.Context;

import mist.node.Endpoint;
import mist.node.MistNode;

/**
 * Created by jan on 10/19/17.
 */

public class FlashLightRaw {
    int dataValue = 3;


    public FlashLightRaw(Context context) {
        MistNode node = MistNode.getInstance();
        node.startMistApp("FlashListRaw", context);
        node.addEndpoint(new Endpoint("mist")
                .setRead(new Endpoint.ReadableInt() {
                    @Override
                    public void read(Peer peer, final Endpoint.ReadableIntResponse response) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        response.send(34);
                                        //response.error(222, "this is too cool");
                                    }
                                },
                                300);
                    }
                })
                .setWrite(new Endpoint.WritableInt() {
                    @Override
                    public void write(int value, Peer peer, Endpoint.WriteResponse response) {
                        if (dataValue > 7) {
                            dataValue = value;
                            response.send();
                        }
                        else {
                            response.error(444, "Value is less or equal to 7.");
                        }
                    }
                }));

    }
}