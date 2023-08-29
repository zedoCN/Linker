package top.zedo.ui;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import top.zedo.LinkerLogger;
import top.zedo.net.LinkerClient;

import java.net.InetSocketAddress;

public class LinkerStage extends Stage {
    LinkerClient linkerClient=new LinkerClient();
    public LinkerStage() {
        setMaxWidth(800);
        setMaxHeight(600);
        setMinWidth(800);
        setMinHeight(600);
        setWidth(800);
        setHeight(600);


        linkerClient.setLinkerServerAddress(new InetSocketAddress("",5432));
        linkerClient.connect();
    }
}
