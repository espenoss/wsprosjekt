import java.io.IOException;

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

    public static void manualServer() throws IOException{
        WebSocketServer server = new WebSocketServer(80);

        // start listening on given port
        server.openServer();
        // wait for client to connect
        server.awaitConnection();
        // send message to client
        server.sendShortMessage("Heisann!");
        // wait for reply
        Frame received = server.awaitFrame();
        // assume its a message and decode it
        String reply = server.receiveMessage(received);
        System.out.println(reply);
        // thats all we wanted to do, close connection and close down the server
        server.closeConnection();
        server.closeServer();
    }

    public static void main(String[] args) throws IOException {
        //WebSocketServer server = new WebSocketServerImpl(80);
        //server.run();

        manualServer();
    }
}
