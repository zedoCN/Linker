package top.zedo.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
    OneBox oneBox;
    TwoBox twoBox;

    {
        try {
            LinkerLogger.info("载入配置");
            properties.load(new FileReader("./Linker.cfg", StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    LabelTextField nameTextField = new LabelTextField("你的昵称:");

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
        Platform.runLater(() -> {
            setTitle("Linker " + title);
        });
    }

    public LinkerStage() {
        oneBox = new OneBox(this);
        twoBox = new TwoBox(this);

        /*setMaxWidth(600);
        setMaxHeight(400);*/
        setMinWidth(800);
        setMinHeight(400);
        setWidth(800);
        setHeight(400);
        setScene(scene);


        setOnCloseRequest(event -> System.exit(0));

        nameTextField.textField.setText(properties.getProperty("name"));
        nameTextField.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                properties.put("name", nameTextField.textField.getText());
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
                case USER_LEAVE_GROUP -> {
                    setLinkerTitle("用户离开组");
                }
                case HOST_DISSOLVE_GROUP -> {
                    setLinkerTitle("主机解散了组");
                }
                case USER_LEAVE -> {
                    reconnect();
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
                                setLinkerTitle("无法加入组:" + object.getString("message"));
                                LinkerLogger.warning("无法加入组:" + object.getString("message"));
                            }
                        }
                        case CREATE_GROUP -> {
                            if (object.getBooleanValue("success")) {
                                changePane(false);
                                linkerClient.proxyNetwork.setMode(true);
                                linkerClient.proxyNetwork.start(Integer.parseInt(properties.getProperty("hostPort")));
                                linkerClient.proxyNetwork.setIp(properties.getProperty("hostIp"));
                                setLinkerTitle("成功创建组");
                            } else {
                                setLinkerTitle("无法创建组:" + object.getString("message"));
                                LinkerLogger.warning("无法创建组:" + object.getString("message"));
                            }
                        }
                    }
                }
            }

        });

        linkerClient.proxyNetwork.setFlushDelay(Integer.parseInt(properties.getProperty("flushDelay", "20")));
        linkerClient.setLinkerServerAddress(new InetSocketAddress(properties.getProperty("linkerServerIp"), Integer.parseInt(properties.getProperty("linkerServerPort"))));

        new Thread(() -> {
            setLinkerTitle("正在连接Linker服务器");
            if (linkerClient.connect()) {
                setLinkerTitle("连接成功");
                return;
            }
            reconnect();
        }).start();

        changePane(true);
    }

    private void reconnect() {
        changePane(true);
        new Thread(() -> {
            while (true) {
                for (int i = 5; i > 0; i--) {
                    setLinkerTitle("掉线重连  " + i + "s 后尝试重连");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                setLinkerTitle("正在尝试连接...");
                if (linkerClient.connect()) {
                    setLinkerTitle("重连成功");
                    return;
                }
                setLinkerTitle("重连失败");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }

        }).start();
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
