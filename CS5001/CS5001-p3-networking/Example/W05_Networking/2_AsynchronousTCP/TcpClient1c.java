import java.io.*;
import java.net.*;

/**
 * Simple, non-blocking TCP client with input from keybaord using asynchronous Socket connection.
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class TcpClient1c {

    static int soTimeout_ = 10; // 10 ms
    static int sleepTime_ = 2000; // 2 seconds

    public static void main(String[] args) {
        if (args.length != 2) // user has not provided arguments
        {
            System.out.println("\n TcpClient1c <servername> <portnumber> \n");
            System.exit(0);
        }

        try {
            Socket connection;
            OutputStream tx;
            InputStream rx;
            byte[] buffer;
            String s = new String("");
            String quit = new String("quit");
            int r;

            connection = startClient(args[0], args[1]);
            tx = connection.getOutputStream();
            rx = connection.getInputStream();

            while (!quit.equalsIgnoreCase(s.trim())) {
                System.out.print("\ntype here ---> ");
                buffer = new byte[128];
                r = System.in.read(buffer, 0, buffer.length); // keyboard
                if (r < 0) { // this means there is problem with the System.in object
                    System.err.println("problem with System.in ...");
                } else if (r > 0) { // something has been read from the keyboard

                    s = new String(buffer);
                    if (quit.equalsIgnoreCase(s.trim())) break;

                    System.out.println("Sending " + r + " bytes");
                    tx.write(buffer, 0, r); // to server

                    r = 0;
                    while (r == 0) {
                        try { // wait for server to respond
                            buffer = new byte[128];
                            r = rx.read(buffer, 0, buffer.length); // from server
                            if (r < 0) { // this means there is problem with the rx object
                                // quite possibly becuase the remote end has
                                // dissappeared, so terminate the connection
                                System.err.println("problem with rx ...");
                                System.err.println("... terminating connection.\n");
                                s = new String(quit);
                            } else if (r > 0) { // something came back form the server
                                s = new String(buffer);
                                System.out.print("Received " + r + " bytes --> " + s);
                            }
                        } catch (SocketTimeoutException e) {
                            // no incoming data - just ignore
                        } catch (IOException e) {
                            System.err.println("IO Exception: " + e.getMessage());
                        }

                        try {
                            Thread.sleep(sleepTime_); // pause before trying again ...
                        } catch (InterruptedException e) {
                            System.err.println("Interrupted Exception: " + e.getMessage());
                        }
                    } // while (r == 0)
                }
            }

            System.out.print("\nClosing connection ... ");
            connection.close();
            System.out.println("... closed.");
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
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
            connection.setSoTimeout(soTimeout_);

            System.out.println("--* Connecting to " + hostname + ":" + port + " -> " + connection);
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }

        return connection;
    }
}
