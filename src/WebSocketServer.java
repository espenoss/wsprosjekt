import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketServer extends Thread {

    public final static int OP_CONTINUATION = 0;
    public final static int OP_TEXT = 1;
    public final static int OP_BINARY = 2;
    public final static int OP_CLOSE = 8;
    public final static int OP_PING = 9;
    public final static int OP_PONG = 10;

    private final int PORT;
    private Socket client;
    private ServerSocket server;
    InputStream in;
    OutputStream out;

    public WebSocketServer(int port){
       PORT = port;
    }

    // override these methods to implement own functionality
    // fires when a client has connected
    public void onConnect(){}
    // fires when a text message has been received
    public void onMessage(String message){}
    // fires when a connection to a client has been closed
    public void onClose(){}

    // methods for sending to client
    // send short (<126 byte) text message
    protected void sendShortMessage(String message) throws IOException {

        byte[] header = assembleFrameHeader(1,0,0,0, OP_TEXT, 0,message.length());
        byte[] payload = message.getBytes();

        out.write(header);
        out.write(payload);
    }
    // send control frame with no payload (close, ping, etc.)
    protected void sendControlFrame(int opcode) throws IOException {
        byte[] header = assembleFrameHeader(1,0,0,0,opcode ,0,0);
        out.write(header);
    }

    // methods for receiving from client
    // waits for frame from client and returns it
    public Frame awaitFrame() throws IOException {


        byte[] header = new byte[2];
        byte[] mask = new byte[4];

        in.read(header,0,2);
        in.read(mask, 0, 4);

        int payloadLength = (Byte.toUnsignedInt(header[1]) & 0b1111111); // Read bits 9 to 15 and convert to integer
        byte[] payLoad = new byte[payloadLength];
        in.read(payLoad,0,payloadLength);


        Frame received = new Frame( (header[0] >> 7) & 0b1,
                (header[0] >> 6) & 0b1,
                (header[0] >> 5) & 0b1,
                (header[0] >> 4) & 0b1,
                header[0] & 0b1111,
                mask,
                payloadLength,
                payLoad
        );

        return received;
    }
    // decodes message from text frame and returns it
    public String receiveMessage(Frame message){
        String decodedMessage = decodePayload(message.payload, message.MASK);
        onMessage(decodedMessage);
        return decodedMessage;
    }

    // methods for managing connection
    public void openServer() throws IOException{
        // start listening for connections
        server = new ServerSocket(PORT);
    }
    public void closeServer() throws IOException{
        server.close();
    }
    // establish connection to client and verify handshake
    public void awaitConnection() throws IOException {
        do{
            // establish connection
            client = server.accept();
            in = client.getInputStream();
            out = client.getOutputStream();

            // verify that handshake is correct
        }while(handshake() != 0);

        // callback method
        onConnect();
    }
    // close connection to client
    public void closeConnection() throws IOException{
        sendControlFrame(OP_CLOSE);
        in.close();
        out.close();
        client.close();
        onClose();
    }

    // method for running the server
    public void serve(){
        try{

            openServer();

            while(true){

                // establish connection to client
                awaitConnection();

                boolean connectionOpen = true;

                while(connectionOpen){
                    Frame receivedFrame= awaitFrame();

                    if(receivedFrame.OPCODE == OP_TEXT){
                        receiveMessage(receivedFrame);
                    }else if(receivedFrame.OPCODE == OP_PING){
                        sendControlFrame(OP_PONG);
                    }else if(receivedFrame.OPCODE == OP_CLOSE){
                        connectionOpen = false;
                        closeConnection();
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                closeConnection();
                closeServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // method for running the server in a thread
    public void run(){serve();}

    // helper methods
    // makes putting together a header easier
    private byte[] assembleFrameHeader(int fin, int rsv1, int rsv2, int rsv3, int opcode, int mask, int payloadLen){
        int FIN = fin << 7;
        int RSV1 = rsv1 << 6;
        int RSV2 = rsv2 << 5;
        int RSV3 = rsv3 << 4;
        int OPCODE = opcode;
        int MASK = mask << 7;
        int PAYLOADLEN = payloadLen;

        byte[] header = new byte[2];
        header[0] = (byte) (FIN + RSV1 + RSV2 + RSV3 + OPCODE);
        header[1] = (byte) (MASK + PAYLOADLEN);

        return header;
    }
    // handshake protocol
    private int handshake() throws IOException {

        Scanner inData = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n");
        String data = inData.next();
        Matcher get = Pattern.compile("^GET").matcher(data);

        if (get.find()) {
            // Obtain the value of Sec-WebSocket-Key request header without any leading and trailing whitespace
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();

            // Link with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
            // Compute SHA-1 and Base64 code
            byte[] response = new byte[0];
            try {
                response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter
                        .printBase64Binary(
                                MessageDigest
                                        .getInstance("SHA-1")
                                        .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                                .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            // Write back as value of Sec-WebSocket-Accept response header as part of a HTTP response.
            out.write(response, 0, response.length);

            return 0;
        }

        return -1;
    }
    // decode masked message
    private String decodePayload(byte[] payload, byte[] mask){
        String decoded = "";

        for (int i=0;i<payload.length;i++){
            decoded += (char)(payload[i]^mask[i%4]);
        }

        return decoded;
    }

    public class Frame {
        public final int FINAL;
        public final int RSV1;
        public final int RSV2;
        public final int RSV3;
        public final int OPCODE;
        public final byte[] MASK;
        public final int PAYLOADLEN;
        public final byte[] payload;

        public Frame(int FINAL, int RSV1, int RSV2, int RSV3, int OPCODE, byte[] MASK, int PAYLOADLEN, byte[] payload) {
            this.FINAL = FINAL;
            this.RSV1 = RSV1;
            this.RSV2 = RSV2;
            this.RSV3 = RSV3;
            this.OPCODE = OPCODE;
            this.MASK = MASK;
            this.PAYLOADLEN = PAYLOADLEN;
            this.payload = payload;
        }
    }
}
