package top.zedo.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import top.zedo.LinkerLogger;
import top.zedo.net.LinkerClient;
import top.zedo.util.ByteFormatter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LinkerStage extends Stage {
    Properties properties = new Properties();

    {
        try {
            LinkerLogger.info("载入配置");
            properties.load(new FileReader("./Linker.cfg", StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    TextField nameTextField = new TextField();

    private void saveProperties() {
        try {
            LinkerLogger.info("保存配置");
            properties.store(new FileWriter("./Linker.cfg", StandardCharsets.UTF_8), "Linker");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    {

        nameTextField.setPromptText("你的昵称");
    }

    Button nameButton = new Button("修改");

    HBox header = new HBox(nameTextField, nameButton);

    {
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(8);
        header.setPadding(new Insets(8));
    }

    OneBox oneBox = new OneBox();
    VBox body = new VBox(oneBox);

    {
        VBox.setVgrow(body, Priority.ALWAYS);
    }


    Label totalUpBytes = new Label("总上传:0B");
    Label upTraffic = new Label("上传:0B/s");
    Label totalDownBytes = new Label("总下载:0B");
    Label downTraffic = new Label("下载:0B/s");
    Label delay = new Label("延迟:0ms");
    HBox footer = new HBox(totalUpBytes, totalDownBytes, upTraffic, downTraffic, delay);

    {
        totalUpBytes.setPrefWidth(100);
        upTraffic.setPrefWidth(100);
        totalDownBytes.setPrefWidth(100);
        downTraffic.setPrefWidth(100);
        delay.setPrefWidth(80);
        footer.setAlignment(Pos.CENTER_RIGHT);
    }

    VBox root = new VBox(header, body, footer);
    Scene scene = new Scene(root);
    LinkerClient linkerClient;


    public LinkerStage() {
        /*setMaxWidth(600);
        setMaxHeight(400);*/
        setMinWidth(600);
        setMinHeight(400);
        setWidth(600);
        setHeight(400);
        setScene(scene);


        linkerClient = new LinkerClient((user, event, object) -> {
            switch (event) {
                case USER_GET_START -> {
                    Platform.runLater(() -> {
                        totalUpBytes.setText("总上传:" + ByteFormatter.formatBytes(user.totalUpBytes));
                        totalDownBytes.setText("总下载:" + ByteFormatter.formatBytes(user.totalDownBytes));
                        upTraffic.setText("上传:" + ByteFormatter.formatBytes(user.upTraffic) + "/s");
                        downTraffic.setText("下载:" + ByteFormatter.formatBytes(user.downTraffic) + "/s");
                        delay.setText("延迟:" + user.delay + "ms");
                    });
                }
                case USER_LOGIN -> {
                    linkerClient.changeName(nameTextField.getText());
                }
            }
        });

        linkerClient.setLinkerServerAddress(new InetSocketAddress("", 5432));
        linkerClient.connect();
    }
}
