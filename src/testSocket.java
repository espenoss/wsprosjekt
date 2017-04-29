import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();

        String data = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();

        Matcher get = Pattern.compile("^GET").matcher(data);

        if (get.find()) {
            // Obtain the value of Sec-WebSocket-Key request header without any leading and trailing whitespace
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();

            // Link it with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
            // Compute SHA-1 and Base64 code of it
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

            // Write it back as value of Sec-WebSocket-Accept response header as part of a HTTP response.
            out.write(response, 0, response.length);
        }




    }

    private static void cPrint(String message){
        System.out.println(message);
    }
}
