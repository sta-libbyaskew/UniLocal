import java.io.*;
import java.net.*;

/**
 * Simple, blocking TCP server
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class TcpServer1 {

    static int port_ = 55501; // You need to change this!
    static ServerSocket server_;

    public static void main(String[] args) {
        startServer();

        while (true) // forever
        {
            try {
                Socket connection;
                OutputStream tx;
                InputStream rx;
                byte[] buffer;
                int r;
                String s;

                connection = server_.accept(); // blocks waiting for connection
                tx = connection.getOutputStream();
                rx = connection.getInputStream();

                System.out.println(
                        "New connection ... "
                                + connection.getInetAddress().getHostName()
                                + ":"
                                + connection.getPort());

                buffer = new byte[1024]; // buffer to hold incoming bytes
                r = rx.read(buffer); // blocks waiting for data
                s = new String(buffer);
                System.out.println("Received " + r + " bytes --> " + s);

                System.out.println("Sending " + r + " bytes");
                tx.write(buffer); // send data back to client

                connection.close();
            } catch (IOException e) {
                System.err.println("IO Exception: " + e.getMessage());
            }
        }
    }

    public static void startServer() {
        try {
            InetAddress address;
            String hostname;

            address = InetAddress.getLocalHost(); // not needed
            hostname = address.getHostName(); // not needed

            server_ = new ServerSocket(port_); // make a socket

            System.out.println("--* Starting server " + hostname + ":" + port_ + " -> " + server_);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }

    protected void finalize() // tidy up when program ends
            {
        try {
            server_.close();
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }
}
