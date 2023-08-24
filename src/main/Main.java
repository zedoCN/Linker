package main;

import mode.LinkerClient;
import server.LinkerServer;

import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        switch (args[0]) {
            case "-s" -> {
                Server.main(args);
            }
            case "-uc" -> {
                RunClient.main(args);
            }
            case "-hc" -> {
                RunHostClient.main(args);
            }
            case "-h" -> {
                System.out.println("-s [port] 启动服务器\n-uc [LinkerIP] [LinkerPort] [userName] [serverIP] [serverPort] [groupUUID] 启动用户客户端\n-hc [LinkerIP] [LinkerPort] [userName] [usePort] [groupName] 启动主机客户端\n-h 帮助");
            }
        }
        //Scanner scanner = new Scanner(System.in);

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