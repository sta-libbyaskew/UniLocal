import java.io.*;
import java.net.*;

/**
 * Simple, non-blocking TCP server, using asynchronous accept() and asynchronous Socket connection.
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ March 2007
 */
public class TcpServer1c {

    static int port_ = 55501; // You need to change this!
    static ServerSocket server_;
    static int soTimeout_ = 10; // 10 ms
    static int sleepTime_ = 2000; // 3 seconds

    public static void main(String[] args) {
        startServer();

        while (true) // forever
        {
            Socket connection = null;
            OutputStream tx = null;
            InputStream rx = null;
            byte[] buffer;
            String s;
            String quit = new String("quit");

            while (connection == null) {
                try {
                    tx = null;
                    rx = null;
                    connection = null;

                    connection = server_.accept();
                    connection.setSoTimeout(soTimeout_); // make this connection non-blocking
                    tx = connection.getOutputStream();
                    rx = connection.getInputStream();
                } catch (SocketException e) {
                    System.out.println("Socket Exception: " + e.getMessage());
                } catch (SocketTimeoutException e) {
                    // just means no incoming connection requests
                    System.out.println("+ No incoming connection requests");
                } catch (IOException e) {
                    System.err.println("IO Exception: " + e.getMessage());
                }

                if (connection == null) {
                    System.out.println("+ Going to sleep for a while ...");
                    System.out.println("+ ... could be doing something else.\n");
                    try {
                        Thread.sleep(sleepTime_);
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted Exception: " + e.getMessage());
                    }
                }
            }

            System.out.println(
                    "New connection ... "
                            + connection.getInetAddress().getHostName()
                            + ":"
                            + connection.getPort());

            s = null;
            while (!quit.equalsIgnoreCase(s)) {
                int r;
                try {
                    buffer = new byte[128];
                    r = rx.read(buffer, 0, buffer.length);
                    if (r < 0) { // this means there is problem with the rx object
                        // quite possibly becuase the remote end has
                        // dissappeared, so terminate the connection
                        System.err.println("problem with rx ...");
                        System.err.println("... terminating connection.\n");
                        s = new String(quit);
                    } else if (r > 0) { // something has been read
                        s = new String(buffer);
                        System.out.print("Received " + r + " bytes --> " + s);

                        s = s.trim();
                        if (quit.equalsIgnoreCase(s)) {
                            System.out.println("client is finished ...");
                            System.out.println("... terminating connection.\n");
                            connection.close();
                        } else {
                            System.out.println("\nSending " + r + " bytes");
                            tx.write(buffer, 0, r); // send data back to client
                        }
                    }
                } // try
                catch (SocketTimeoutException e) {
                    // no incoming data - just ignore
                } catch (IOException e) {
                    System.err.println("IO Exception: " + e.getMessage());
                }

                try {
                    Thread.sleep(sleepTime_); // pause before trying again ...
                } catch (InterruptedException e) {
                    System.err.println("Interrupted Exception: " + e.getMessage());
                }
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
            server_.setSoTimeout(soTimeout_); // wait soTimeout ms

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
