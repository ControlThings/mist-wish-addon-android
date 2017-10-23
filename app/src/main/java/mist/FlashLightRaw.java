package mist;

import android.content.Context;

import java.util.ResourceBundle;

import mist.node.Endpoint;
import mist.node.MistNode;
import mist.request.Control;

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


        Control.read(new Peer(), "", new Control.ReadCb() {
            @Override
            public void err(int code, String msg) {
                super.err(code, msg);

            }

            @Override
            public void cbInt(int data) {
                super.cbInt(data);
            }
        });
    }
}