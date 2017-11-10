package com.highmobility.hmkit.WebSocket;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.phoenixframework.channels.Channel;
import org.phoenixframework.channels.ChannelEvent;
import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IMessageCallback;
import org.phoenixframework.channels.Socket;

import java.io.IOException;

/**
 * Created by root on 27/04/2017.
 */
class WebSocket {
    interface WebSocketInterface {
        void onCommandResponse(byte[] bytes);
        void onIncomingCommand(byte[] bytes);
    }

    public static final String path                 = "telematics";
    public static final String transportProtocol    = "websocket";
    public static final String eventSend            = "custom_command";
    public static final String eventReceive         = "command_response";

    Socket socket;
    Channel channel;

    WebSocket() {

    }

    void connect(String telematicsServiceID, String accessToken) throws IOException {
        socket = new Socket("ws://localhost:4000/socket/websocket");
        socket.connect();
        channel = socket.chan("hm:telematics:"+ telematicsServiceID, null);

        channel.join()
                .receive("ignore", new IMessageCallback() {
                    @Override
                    public void onMessage(Envelope envelope) {
                        System.out.println("IGNORE");
                    }
                })
                .receive("ok", new IMessageCallback() {
                    @Override
                    public void onMessage(Envelope envelope) {
                        System.out.println("JOINED with " + envelope.toString());
                    }
                });

        channel.on("new:msg", new IMessageCallback() {
            @Override
            public void onMessage(Envelope envelope) {
                System.out.println("NEW MESSAGE: " + envelope.toString());
            }
        });

        channel.on(ChannelEvent.CLOSE.getPhxEvent(), new IMessageCallback() {
            @Override
            public void onMessage(Envelope envelope) {
                System.out.println("CLOSED: " + envelope.toString());
            }
        });

        channel.on(ChannelEvent.ERROR.getPhxEvent(), new IMessageCallback() {
            @Override
            public void onMessage(Envelope envelope) {
                System.out.println("ERROR: " + envelope.toString());
            }
        });
    }

    void sendCommand(byte[] command) throws IOException {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                .put("user", "my_username")
                .put("body", "");

        channel.push("new:msg", node);
    }
}
