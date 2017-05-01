import java.io.IOException;

/**
 * Created by Ein fugel on 01.05.2017.
 */


// example implementation of echo server
public class WebSocketServerImpl extends WebSocketServer{
    WebSocketServerImpl(int port){
        super(port);
    }

    @Override
    public void onConnect() {
        System.out.println("Client connected");
    }

    @Override
    public void onMessage(String message){
        System.out.println(message);
        try {
            sendShortMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(){
        System.out.println("Client disconnected");
    }

    public static void main(String[] args) throws IOException {
        WebSocketServer server = new WebSocketServerImpl(80);
        server.run();
    }
}
