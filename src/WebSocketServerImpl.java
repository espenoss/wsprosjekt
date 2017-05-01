import java.io.IOException;

/**
 * Created by Ein fugel on 01.05.2017.
 */
public class WebSocketServerImpl extends WebSocketServer{
    WebSocketServerImpl(int port){
        super(port);
    }

    @Override
    public void onConnect() {
        System.out.println("Client connected");
    }

    @Override
    public void onMessage(String message) throws IOException {
        System.out.println(message);
        sendMessage(message);
    }

    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServerImpl(80);
        server.run();
    }
}
