import java.io.IOException;
import java.net.*;
import java.net.Socket;

//server created by default port

public class WebServer {

    private ServerSocket ss; // listen for client connection requests on this server socket
    private static int PORT;
    public static String FILE_PATH;

    public WebServer(int port, String filepath) {
        this.PORT = port;
        this.FILE_PATH = filepath;
        try {
            ss = new ServerSocket(port);
            System.out.println("Server started ... listening on port " + port + " ...");
            while (true) {
                // waits until client requests a connection, then returns connection (socket)
                Socket conn = ss.accept();
                System.out.println("Server got new connection request from " + conn.getInetAddress());
                // create new handler for the connection
                ConnectionHandler ch = new ConnectionHandler(conn);
                ch.handleRequest(); // handle the client request
            }
        } catch (IOException ioe) {
            System.out.println("Ooops " + ioe.getMessage());
        }
    }
}
