import java.io.*;
import java.net.*;

/**
 * Simple, non-blocking TCP client with input from keybaord
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class TcpClient1b {

    public static void main(String[] args) {
        if (args.length != 2) // user has not provided arguments
        {
            System.out.println("\n TcpClient1b <servername> <portnumber> \n");
            System.exit(0);
        }

        try {
            Socket connection;
            OutputStream tx;
            InputStream rx;
            byte[] buffer;
            String s;
            String quit = new String("quit");

            connection = startClient(args[0], args[1]);
            tx = connection.getOutputStream();
            rx = connection.getInputStream();

            System.out.print(" type something -> ");
            buffer = null;
            while (buffer == null) {
                buffer = ByteReader.readBytes(System.in, 80); // keyboard
                Thread.sleep(2000); // 2 seconds
            }
            ByteWriter.writeBytes(tx, buffer, buffer.length); // send to server
            System.out.println("Sending " + buffer.length + " bytes");

            buffer = null;
            while (buffer == null) {
                buffer = ByteReader.readBytes(rx, 80); // from server
                Thread.sleep(2000); // 2 seconds
            }

            s = new String(buffer); // / assume it is a printable string
            System.out.println("Received " + buffer.length + " bytes --> " + s);

            ByteWriter.writeLine(tx, quit);
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

            System.out.println("--* Connecting to " + hostname + ":" + port + " -> " + connection);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }

        return connection;
    }
}
