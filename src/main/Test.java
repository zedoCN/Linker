package main;

import mode.LinkerClient;
import server.LinkerServer;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            LinkerServer server = new LinkerServer(5432);
        }).start();


        Thread.sleep(500);
        LinkerClient client2 = new LinkerClient("192.168.1.120", 5432, "", "zedoCN");
        Thread.sleep(500);
        client2.createGroup("dsads",8888);


    }
}
