package old.main;

import old.server.LinkerServer;

public class Server {
    public static void main(String[] args) {
        new Thread(() -> {
            LinkerServer server = new LinkerServer(5432);
        }).start();
    }
}
