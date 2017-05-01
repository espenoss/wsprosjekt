import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Created by Ein fugel on 01.05.2017.
 */
public class TestHTTPServer {
    public static void main(String[] args) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 80);
        HttpServer server = HttpServer.create(socketAddress, 0);

        server.createContext("/", new RootHandler());
        server.setExecutor(null);
        server.start();

    }

    public static class RootHandler implements HttpHandler {

        @Override

        public void handle(HttpExchange he) throws IOException {
            String response = "<h1>Server start success " +
            "if you see this message</h1>" + "<h1>Port: " + 80 + "</h1>";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
