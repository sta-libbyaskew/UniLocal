import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private String host;
    private int port;

    private BufferedReader br;
    private PrintWriter pw;
    private FileReader fr;

    private InputStream test_is;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        runClient();
    }

    private void runClient() {
        try {
            this.socket = new Socket(host, port);
            System.out.println("Client connected to " + host + " on port " + port + ".");
            System.out.println("To exit enter a single line containing: " + Configuration.exitString);
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            br = new BufferedReader(new InputStreamReader(System.in));
            pw = new PrintWriter(new OutputStreamWriter(os.getOutputStream()));
            test_is = socket.getInputStream();
            printUserInputToSocket(); // this runs until something goes wrong

        } catch (Exception e) {
            // exit cleanly for any Exception (including IOException, DisconnectedException)
            System.out.println("Ooops on connection to " + host + " on port " + port + ". " + e.getMessage());
            cleanup(); // execute cleanup method to close connections cleanly
        }
    }

    private void printUserInputToSocket() throws IOException, DisconnectedException {
        while (true) {
            String line = br.readLine(); // get user input
            pw.println(line); // print line out on the socket's output stream
            pw.flush(); // flush the output stream so that the server gets message immediately
            if (line.equals(Configuration.exitString)) { // user has entered exit command
                throw new DisconnectedException(" ... user has entered exit command ... ");
            }
        }
    }

    private void cleanup() {
        System.out.println("Client: ... cleaning up and exiting ... ");
        try {
            pw.close();
            br.close();
            socket.close();
        } catch (IOException ioe) {
            System.out.println("Ooops " + ioe.getMessage());
        }
    }
}
