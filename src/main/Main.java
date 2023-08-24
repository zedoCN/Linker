package main;

import mode.LinkerClient;
import server.LinkerServer;

import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner=new Scanner(System.in);
        new Thread(() -> {
            LinkerServer server = new LinkerServer(8866);//6666
        }).start();
       /* Thread.sleep(1000);
        LinkerClient client1 = new LinkerClient();
        Thread.sleep(1000);
        client1.createGroup("aaaaa", 25565);//-> mc服务器
        Thread.sleep(1000);
        LinkerClient client2 = new LinkerClient();
        Thread.sleep(1000);
        client2.joinGroup(scanner.nextLine(), 8888);*/

    }
}