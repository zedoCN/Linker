package main;

import mode.LinkerClient;

import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner=new Scanner(System.in);
        LinkerClient client2 = new LinkerClient("zedo.top",8866,"zedoCN");
        Thread.sleep(1000);
        client2.joinGroup(scanner.nextLine(), 8888);
    }
}
