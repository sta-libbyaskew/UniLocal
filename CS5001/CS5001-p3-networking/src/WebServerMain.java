public class WebServerMain {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[1]);
        String filepath = args[0];
        WebServer s = new WebServer(port, filepath); //need to pass file directoy here and port is passed as an argem
    }
}

