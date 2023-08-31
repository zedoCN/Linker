package top.zedo.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import top.zedo.data.LinkerCommand;
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

    LabelTextField nameTextField = new LabelTextField("昵称:");

    protected void saveProperties() {
        try {
            LinkerLogger.info("保存配置");
            properties.store(new FileWriter("./Linker.cfg", StandardCharsets.UTF_8), "Linker");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    HBox header = new HBox(nameTextField);

    {
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(8);
        header.setPadding(new Insets(8));
    }

    OneBox oneBox = new OneBox(this);
    TwoBox twoBox = new TwoBox(this);
    VBox body = new VBox();

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
    public LinkerClient linkerClient;

    public void setLinkerTitle(String title) {
        Platform.runLater(()->{
            setTitle("Linker " + title);
        });
    }

    public LinkerStage() {
        /*setMaxWidth(600);
        setMaxHeight(400);*/
        setMinWidth(800);
        setMinHeight(400);
        setWidth(800);
        setHeight(400);
        setScene(scene);

        nameTextField.textField.setText(properties.getProperty("Name"));
        nameTextField.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                properties.put("Name", nameTextField.textField.getText());
                saveProperties();
                linkerClient.changeName(nameTextField.textField.getText());
            }
        });

        linkerClient = new LinkerClient((user, event, object) -> {
            oneBox.handleEvent.handleEvent(user, event, object);
            twoBox.handleEvent.handleEvent(user, event, object);
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
                    linkerClient.changeName(nameTextField.textField.getText());
                    setLinkerTitle("登录成功");
                }
                case USER_JOIN_GROUP -> {
                    setLinkerTitle("加入到组");
                }
                case HOST_DISSOLVE_GROUP -> {
                    setLinkerTitle("主机解散了组");
                }
                case USER_LEAVE -> {
                    setLinkerTitle("用户离开");
                }
                case COMMAND_SUCCESS -> {
                    switch (LinkerCommand.valueOf(object.getString("command"))) {
                        case JOIN_GROUP -> {
                            if (object.getBooleanValue("success")) {
                                changePane(false);
                                linkerClient.proxyNetwork.setMode(false);
                                linkerClient.proxyNetwork.start(Integer.parseInt(properties.getProperty("userPort")));
                                setLinkerTitle("成功加入组");
                            } else {
                                setLinkerTitle("无法加入组:"+ object.getString("message"));
                                LinkerLogger.warning("无法加入组:" + object.getString("message"));
                            }
                        }
                        case CREATE_GROUP -> {
                            if (object.getBooleanValue("success")) {
                                changePane(false);
                                linkerClient.proxyNetwork.setMode(true);
                                linkerClient.proxyNetwork.start(Integer.parseInt(properties.getProperty("hostPort")));
                                setLinkerTitle("成功创建组");
                            } else {
                                setLinkerTitle("无法创建组:"+ object.getString("message"));
                                LinkerLogger.warning("无法创建组:" + object.getString("message"));
                            }
                        }
                    }
                }
            }
        });


        linkerClient.setLinkerServerAddress(new InetSocketAddress(properties.getProperty("LinkerServerIp"), Integer.parseInt(properties.getProperty("LinkerServerPort"))));
        linkerClient.connect();

        changePane(true);
    }

    /**
     * 变更面板
     *
     * @param isOne 是第一个
     */
    public void changePane(boolean isOne) {
        Platform.runLater(() -> {
            body.getChildren().clear();
            if (isOne) {
                body.getChildren().add(oneBox);
            } else {
                body.getChildren().add(twoBox);
            }
        });
    }
}
