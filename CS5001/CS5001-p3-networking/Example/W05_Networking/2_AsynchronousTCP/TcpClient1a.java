import java.io.*;
import java.net.*;

/**
 * Simple, blocking TCP client with port number argument
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class TcpClient1a {

    public static void main(String[] args) {
        if (args.length != 3) // user has not provided arguments
        {
            System.out.println("\n TcpClient1a <servername> <portnumber> <string> \n");
            System.exit(0);
        }

        try {
            Socket connection;
            OutputStream tx;
            InputStream rx;
            byte[] buffer;
            int r;
            String s;

            connection = startClient(args[0], args[1]);
            tx = connection.getOutputStream();
            rx = connection.getInputStream();

            buffer = args[2].getBytes(); // stream of bytes to transmit
            System.out.println("Sending " + buffer.length + " bytes");
            Thread.sleep(2000); // 2 seconds
            tx.write(buffer); // send to server

            r = rx.read(buffer); // wait for data sent back from server
            s = new String(buffer);
            System.out.println("Received " + r + " bytes --> " + s);

            connection.close();
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Interrupted Exception: " + e.getMessage());
        }
    }

    static Socket startClient(String hostname, String portnumber) {
        Socket connection = null;

        try {
            InetAddress address;
            int port;

            address = InetAddress.getByName(hostname);
            port = Integer.parseInt(portnumber);

            connection = new Socket(address, port); // make a socket

            System.out.println("--* Connecting to " + hostname + ":" + port + "-> " + connection);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }

        return connection;
    }
}
