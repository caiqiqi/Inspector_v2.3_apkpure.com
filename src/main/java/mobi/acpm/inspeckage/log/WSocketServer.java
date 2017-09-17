package mobi.acpm.inspeckage.log;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WSocketServer extends WebSocketServer {
    public WSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public WSocketServer(InetSocketAddress address) {
        super(address);
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        sendToClient("[Handshake Ok]");
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        sendToClient(webSocket + " close");
    }

    public void onMessage(WebSocket webSocket, String message) {
        sendToClient(message);
    }

    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
        if (webSocket == null) {
        }
    }

    public void sendToClient(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}
