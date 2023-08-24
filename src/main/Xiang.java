package main;

import mode.LinkerClient;

public class Xiang {
    public static void main(String[] args) throws InterruptedException {

        while (true){
            try {
                Thread.sleep(500);
                LinkerClient client1 = new LinkerClient("zedo.top", 5432, "", "xiang");
                Thread.sleep(500);
                client1.createGroup("yigezu", 2157);

                client1.channelFuture.channel().closeFuture().await();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("崩了，自动重启。");
            }
            Thread.sleep(2000);
        }
    }
}
