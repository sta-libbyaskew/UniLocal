import java.io.IOException;
import java.net.*;

//server created by default port

public class Server {

    private ServerSocket ss; // listen for client connection requests on this server socket

    public Server(int port) {
        try {
            ss = new ServerSocket(port);
            System.out.println("Server started ... listening on port " + port + " ...");
            while (true) {
                // waits until client requests a connection, then returns connection (socket)
                Socket conn = ss.accept();
                System.out.println("Server got new connection request from " + conn.getInetAddress());

                // create new handler for the connection
                ConnectionHandler ch = new ConnectionHandler(conn); 
                ch.handleClientRequest(); // handle the client request
            }
        } catch (IOException ioe) {
            System.out.println("Ooops " + ioe.getMessage());
        }
    }
}
