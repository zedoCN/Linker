package main;

import mode.LinkerClient;
import server.LinkerServer;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            LinkerServer server = new LinkerServer(5432);
        }).start();





    }
}
