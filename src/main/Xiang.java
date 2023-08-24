package main;

import mode.LinkerClient;

public class Xiang {
    public static void main(String[] args) throws InterruptedException {

        while (true){
            try {
                Thread.sleep(500);
                LinkerClient client1 = new LinkerClient("192.168.1.120", 8432, "", "zedoCN");
                Thread.sleep(500);
                client1.createGroup("yigezu", 2157);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("崩了，自动重启。");
            }
            Thread.sleep(2000);
        }
    }
}
