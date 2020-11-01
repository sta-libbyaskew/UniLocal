import java.io.*;
import java.net.*;

/**
 * Simple, blocking TCP client
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class TcpClient1 {

    static int port_ = 55501; // You need to change this!

    public static void main(String[] args) {
        if (args.length != 2) // user has not provided arguments
        {
            System.out.println("\n TcpClient1 <servername> <string> \n");
            System.exit(0);
        }

        try {
            Socket connection;
            OutputStream tx;
            InputStream rx;
            byte[] buffer;
            int r;
            String s;

            connection = startClient(args[0]);
            tx = connection.getOutputStream();
            rx = connection.getInputStream();

            buffer = args[1].getBytes(); // stream of bytes to transmit
            System.out.println("Sending " + buffer.length + " bytes");
            tx.write(buffer); // send to server

            r = rx.read(buffer); // wait for data sent back from server
            s = new String(buffer);
            System.out.println("Received " + r + " bytes --> " + s);

            connection.close();
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    } // main()

    static Socket startClient(String hostname) {
        Socket connection = null;

        try {
            InetAddress address;

            address = InetAddress.getByName(hostname);
            connection = new Socket(address, port_); // make a socket

            System.out.println("--* Connecting to " + hostname + ":" + port_ + " -> " + connection);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }

        return connection;
    }
}
