package top.zedo;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.util.Logging;
import top.zedo.ui.LinkerStage;

public class Main {
    public static void main(String[] args) {

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
}
