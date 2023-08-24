package main;

import mode.LinkerClient;

public class RunHostClient {
    public static void main(String[] args) throws InterruptedException {
        LinkerClient client1 = new LinkerClient("192.168.1.120",8866,"xiang");
        Thread.sleep(1000);
        client1.createGroup("aaaaa", 25565);//-> mc服务器
    }
}
