package top.zedo;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.util.Logging;
import top.zedo.net.LinkerServer;
import top.zedo.ui.LinkerStage;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            runClient();
        } else if (args.length == 1) {
            switch (args[0]) {
                case "-server":
                    runServer();
                case "-client":
                    runClient();
                default:
                    System.out.println("-server 运行服务端\n-client 运行客户端\n");
            }
        }


    }

    /**
     * 运行客户端
     */
    public static void runClient() {
        LinkerLogger.info("初始化图形系统");

        //屏蔽javafx歌姬初始化时的异常
        Logging.getJavaFXLogger().disableLogging();
        PlatformImpl.startup(() -> {
            //再次开启javafx日志
            Logging.getJavaFXLogger().enableLogging();

            //创建软件实例
            LinkerStage linkerStage = new LinkerStage();
            LinkerLogger.info("显示Linker窗口");
            linkerStage.show();

        });
    }

    public static void runServer() {
        Properties properties = new Properties();
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
