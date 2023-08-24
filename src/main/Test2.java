package main;

import mode.LinkerClient;

public class Test2 {
    public static void main(String[] args) {
            try {
                Thread.sleep(500);
                LinkerClient client1 = new LinkerClient("192.168.1.120", 5432, "", "zedoCN");
                Thread.sleep(500);
                client1.joinGroup("6188549d-f8be-438a-be9a-24d578ea4cb9",8888);

                client1.channelFuture.channel().closeFuture().await();
            }catch (Exception e){
                e.printStackTrace();
            }
    }
}
