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

/**
 * Created by Ein fugel on 01.05.2017.
 */
public abstract class WebSocketServer extends Thread {

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

    public void onConnect(){}
    public void onMessage(String message){}
    public void onClose(){}

    public void receiveMessage(Frame message){
        String decodedMessage = decodePayload(message.payload, message.MASK);
        onMessage(decodedMessage);
    }
    public void sendShortMessage(String message) throws IOException {

        byte[] header = assembleFrameHeader(1,0,0,0, OP_TEXT, 0,message.length());
        byte[] payload = message.getBytes();

        out.write(header);
        out.write(payload);
    }
    public void sendControlFrame(int opcode) throws IOException {
        byte[] header = assembleFrameHeader(1,0,0,0,opcode ,0,0);
        out.write(header);
    }
    public void awaitConnection() throws IOException {
        do{
            client = server.accept();
            in = client.getInputStream();
            out = client.getOutputStream();
        }while(handshake() != 0);

        onConnect();
    }
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
    public void closeConnection() throws IOException{
        sendControlFrame(OP_CLOSE);
        in.close();
        out.close();
        client.close();
        onClose();
    }

    public void serve(){
        try{

            // start listening for connections
            server = new ServerSocket(PORT);

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
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void run(){serve();}

    // helper methods
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
    private String decodePayload(byte[] payload, byte[] mask){
        String decoded = "";

        for (int i=0;i<payload.length;i++){
            decoded += (char)(payload[i]^mask[i%4]);
        }

        return decoded;
    }
}
