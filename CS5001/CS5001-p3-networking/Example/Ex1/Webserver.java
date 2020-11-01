import java.util.*;
import java.io.*;
import java.net.*;

// A Webserver waits for clients to connect, then starts a separate
// thread to handle the request.
public class Webserver {
    private static ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        serverSocket=new ServerSocket(12345);  // Start, listen on port 80
        while (true) {
            try {
                Socket s=serverSocket.accept();  // Wait for a client to connect
                new ClientHandler(s);  // Handle the client in a separate thread
            }
            catch (Exception x) {
                System.out.println(x);
            }
        }
    }
}
