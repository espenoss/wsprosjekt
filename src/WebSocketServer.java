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

    private final int PORT;
    private Socket client;
    private ServerSocket server;
    InputStream in;
    OutputStream out;

    public WebSocketServer(int port){
       PORT = port;
    }

    public void onConnect(){}
    public abstract void onMessage(String message) throws IOException;

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
    protected String receiveMessage() throws IOException {

        byte[] header = new byte[2];
        byte[] mask = new byte[4];

        // Get message headers and message mask
        in.read(header,0,2);
        in.read(mask,0,4);

        int payloadLength = (Byte.toUnsignedInt(header[1]) & 0b111111); // Read bits 9 to 15 and convert to integer

        // Get payload
        byte[] message = new byte[payloadLength];
        in.read(message,0,payloadLength);

        String decoded = "";

        decoded = "";
        for (int i=0;i<payloadLength;i++){
            decoded += (char)(message[i]^mask[i%4]);
        }

        return decoded;
    }
    protected void sendMessage(String message) throws IOException {
        int FIN = (1 << 7);
        int OPCODE = 1;
        int MASK = (0 << 7);
        int PLENGTH = message.length();
        byte[] payload = message.getBytes();

        byte[] header = new byte[2];
        header[0] = (byte) (FIN + OPCODE);
        header[1] = (byte) (MASK + PLENGTH);

        out.write(header);
        out.write(payload);
    }

    public void serve(){
        try{
            server = new ServerSocket(PORT);

            do{
                client = server.accept();
                in = client.getInputStream();
                out = client.getOutputStream();
            }while(handshake() != 0);

            onConnect();

            onMessage(receiveMessage());


        }catch (IOException e){

        }finally {

        }
   }
    public void run(){serve();}
}
