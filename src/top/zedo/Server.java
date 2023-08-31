package top.zedo;

import com.alibaba.fastjson2.JSONObject;
import top.zedo.net.LinkerServer;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Server {
    public static Properties properties = new Properties();

    public static void main(String[] args) {
        try {
            LinkerLogger.info("载入配置");
            properties.load(new FileReader("./Linker.cfg", StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LinkerServer linkerServer = new LinkerServer();
        linkerServer.setServerPort(Integer.parseInt(properties.getProperty("linkerServerPort")));
        linkerServer.start();
    }
}
