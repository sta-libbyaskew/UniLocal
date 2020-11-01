import java.io.*;
import java.net.Socket;

public class ConnectionHandler {

    private Socket conn; // socket representing TCP/IP connection to Client
    private InputStream is; // get data from client on this input stream
    private OutputStream os; // can send data back to the client on this output stream
    BufferedReader br; // use buffered reader to read client data

    public ConnectionHandler(Socket conn) {
        this.conn = conn;
        try {
            is = conn.getInputStream(); // get data from client on this input stream
            os = conn.getOutputStream(); // to send data back to the client on this stream
            br = new BufferedReader(new InputStreamReader(is)); // use buffered reader to read data
        } catch (IOException ioe) {
            System.out.println("ConnectionHandler: " + ioe.getMessage());
        }
    }

    public void handleClientRequest() {
        System.out.println("new ConnectionHandler constructed .... ");
        try {
            printClientData();
        } catch (Exception e) { // exit cleanly for any Exception
            System.out.println("ConnectionHandler.handleClientRequest: " + e.getMessage());
            cleanup(); // cleanup and exit
        }
    }

    private void printClientData() throws DisconnectedException, IOException {
        while (true) {
            String line = br.readLine(); // get data from client over socket
            // if readLine fails we can deduce here that the connection to the client is broken
            // and shut down the connection on this side cleanly by throwing a DisconnectedException
            // which will be passed up the call stack to the nearest handler (catch block)
            // in the run method
            if (line == null || line.equals("null") || line.equals(Configuration.exitString)) {
                throw new DisconnectedException(" ... client has closed the connection ... "); //disconnected exceoption is a custom exception. See DisconntectedException.java
            }
            // assuming no exception, print out line received from client
            System.out.println("ConnectionHandler: " + line);
        }
    }

    private void cleanup() {
        System.out.println("ConnectionHandler: ... cleaning up and exiting ... ");
        try {
            br.close();
            is.close();
            conn.close();
        } catch (IOException ioe) {
            System.out.println("ConnectionHandler:cleanup " + ioe.getMessage());
        }
    }
}
