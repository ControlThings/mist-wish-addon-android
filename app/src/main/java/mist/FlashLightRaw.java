package mist;

import android.content.Context;

import java.util.Timer;

import mist.node.EndpointRaw;
import mist.node.MistNode;

/**
 * Created by jan on 10/19/17.
 */

public class FlashLightRaw {
    int dataValue = 3;


    public FlashLightRaw(Context context) {
        final MistNode node = MistNode.getInstance();
        node.startMistApp("FlashListRaw", context);
        node.addEndpoint(new EndpointRaw("mist")
                .setRead(new EndpointRaw.ReadableInt() {
                    @Override
                    public void read(Peer peer, final EndpointRaw.ReadableIntResponse response) {
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
                .setWrite(new EndpointRaw.WritableInt() {
                    @Override
                    public void write(int value, Peer peer, EndpointRaw.WriteResponse response) {
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