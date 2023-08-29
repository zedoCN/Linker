package top.zedo;

import com.alibaba.fastjson2.JSONObject;
import top.zedo.net.LinkerServer;

public class Server {
    public static void main(String[] args) {
        LinkerServer linkerServer = new LinkerServer();
        linkerServer.setServerPort(5432);
        linkerServer.start();
    }
}
