package main;

import mode.LinkerClient;

public class RunHostClient {
    public static void main(String[] args) throws InterruptedException {
        LinkerClient client1 = new LinkerClient(args[1], Integer.parseInt(args[2]), args[4], args[3]);
        Thread.sleep(1000);
        client1.createGroup(args[6], Integer.parseInt(args[5]));//-> mc服务器
    }
}
