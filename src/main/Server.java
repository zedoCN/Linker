package main;

import server.LinkerServer;

public class Server {
    public static void main(String[] args) {
        new Thread(() -> {
            LinkerServer server = new LinkerServer(Integer.parseInt(args[1]));
        }).start();
    }
}
