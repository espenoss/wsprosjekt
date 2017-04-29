import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ein fugel on 29.04.2017.
 */
public class testSocket {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        final int PORTNR = 8080;

        ServerSocket server = new ServerSocket(PORTNR);
        cPrint("Server kjører, venter på tilkobling");

        Socket client = server.accept();
        cPrint("Klient tilkoblet");

        InputStream in = client.getInputStream();

        OutputStream out = client.getOutputStream();

        Scanner inData = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n");

        String data = inData.next();

        Matcher get = Pattern.compile("^GET").matcher(data);

        if (get.find()) {

            // Obtain the value of Sec-WebSocket-Key request header without any leading and trailing whitespace
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();

            // Link with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
            // Compute SHA-1 and Base64 code
            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
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

            // Write back as value of Sec-WebSocket-Accept response header as part of a HTTP response.
            out.write(response, 0, response.length);
        }

        // Connection established

        // Receive messages until "avlsutt is received"
        String decoded = "";

        while(!decoded.equals("avslutt")){

            // Read
            byte[] header = new byte[2];
            byte[] mask = new byte[4];

            in.read(header,0,2);
            in.read(mask,0,4);


            int payloadLength = (Byte.toUnsignedInt(header[1]) & 0b111111); // Read bits 9 to 15 and convert to integer
            cPrint("Lengde: " + payloadLength);

            byte[] message = new byte[payloadLength];
            in.read(message,0,payloadLength);


            decoded = "";
            for (int i=0;i<payloadLength;i++){
                decoded += (char)(message[i]^mask[i%4]);
            }

            cPrint(decoded);
        }


        // Send message to client - "Ha det!"
        String message = "Ha det!";

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


        // Send connection close control frame

        FIN = (1 << 7);
        OPCODE = 8;
        PLENGTH = 0;
        header[0] = (byte) (FIN + OPCODE);
        header[1] = (byte) (MASK + PLENGTH);
        out.write(header);

        server.close();

        while(true); // wait forever

    }


    public static void sendMessage(String message){
        int FIN = (1 << 7);
        int OPCODE = 1; // TEXT
        int MASK = (0 << 7);
        int PLENGTH = message.length();
        byte[] payload = message.getBytes();

        byte[] header = new byte[2];
        header[0] = (byte) (FIN + OPCODE);
        header[1] = (byte) (MASK + PLENGTH);

        // TODO



    }

    // helper methods for printing to console
    private static void cPrint(String message){
        System.out.println(message);
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
}
