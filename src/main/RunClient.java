package main;

import mode.LinkerClient;

import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) throws InterruptedException {
        LinkerClient client2 = new LinkerClient(args[1], Integer.parseInt(args[2]), "", args[3]);
        Thread.sleep(1000);
        client2.joinGroup(args[5], Integer.parseInt(args[4]));
    }
}
