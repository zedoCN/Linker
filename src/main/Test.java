package main;

import mode.LinkerClient;
import server.LinkerServer;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            LinkerServer server = new LinkerServer(8866);
        }).start();

        Thread.sleep(500);
        LinkerClient client1 = new LinkerClient("192.168.1.120", 8866, "", "zedoCN");
        Thread.sleep(500);
        client1.createGroup("yigezu", 2157);


        Thread.sleep(500);
        LinkerClient client2 = new LinkerClient("192.168.1.120", 8866, "", "zedoCN");
        Thread.sleep(500);
        client2.joinGroup(String.valueOf(client1.linkerGroup.uuid),8888);


    }
}
