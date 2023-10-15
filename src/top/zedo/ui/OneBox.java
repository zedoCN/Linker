package top.zedo.ui;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;
import top.zedo.LinkerLogger;
import top.zedo.data.LinkerCommand;
import top.zedo.data.LinkerEvent;
import top.zedo.data.LinkerGroup;
import top.zedo.data.LinkerUser;
import top.zedo.net.LinkerClient;
import top.zedo.util.ByteFormatter;
import top.zedo.util.TimeFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class OneBox extends VBox {
    public static class Person {
        private final SimpleStringProperty groupName;
        private final SimpleStringProperty creator;
        private final SimpleStringProperty members;
        private final SimpleStringProperty delay;

        private final SimpleStringProperty createTime;
        private final SimpleStringProperty groupUUID;

        public Person(String groupName, String creator, String members, String delay, String createTime, String groupUUID) {
            this.groupName = new SimpleStringProperty(groupName);
            this.creator = new SimpleStringProperty(creator);
            this.members = new SimpleStringProperty(members);
            this.delay = new SimpleStringProperty(delay);
            this.createTime = new SimpleStringProperty(createTime);
            this.groupUUID = new SimpleStringProperty(groupUUID);
        }


        public void setMembers(String members) {
            this.members.set(members);
        }

        public void setDelay(String delay) {
            this.delay.set(delay);
        }

        public void setCreator(String creator) {
            this.creator.set(creator);
        }

        public String getGroupName() {
            return groupName.get();
        }

        public String getCreator() {
            return creator.get();
        }

        public String getMembers() {
            return members.get();
        }

        public String getDelay() {
            return delay.get();
        }

        public String getCreateTime() {
            return createTime.get();
        }

        public String getGroupUUID() {
            return groupUUID.get();
        }
    }

    ObservableList<Person> data = FXCollections.observableArrayList();
    HashMap<UUID, Person> groupMap = new HashMap<>();
    TableView<Person> tableView = new TableView<>();
    TableColumn<Person, String> groupNameCol = new TableColumn<>("组名");
    TableColumn<Person, String> creatorCol = new TableColumn<>("创建者");
    TableColumn<Person, String> membersCol = new TableColumn<>("成员");
    TableColumn<Person, String> delayCol = new TableColumn<>("延迟");
    TableColumn<Person, String> createTimeCol = new TableColumn<>("创建日期");
    TableColumn<Person, String> groupUUIDCol = new TableColumn<>("组UUID");
    LinkerStage stage;

    {
        // 使用 PropertyValueFactory 来设置列的数据提取方式
        groupNameCol.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        creatorCol.setCellValueFactory(new PropertyValueFactory<>("creator"));
        membersCol.setCellValueFactory(new PropertyValueFactory<>("members"));
        delayCol.setCellValueFactory(new PropertyValueFactory<>("delay"));
        createTimeCol.setCellValueFactory(new PropertyValueFactory<>("createTime"));

        delayCol.setPrefWidth(60);
        createTimeCol.setPrefWidth(100);

        tableView.getColumns().addAll(groupNameCol, creatorCol, membersCol, delayCol, createTimeCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);


        // 设置单元格内容居中对齐
        Callback<TableColumn<Person, String>, TableCell<Person, String>> cellFactory = col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    setAlignment(javafx.geometry.Pos.CENTER);
                } else {
                    setText(null);
                }
            }
        };


        groupNameCol.setCellFactory(cellFactory);
        creatorCol.setCellFactory(cellFactory);
        membersCol.setCellFactory(cellFactory);
        delayCol.setCellFactory(cellFactory);
        createTimeCol.setCellFactory(cellFactory);


        tableView.setItems(data);
    }

    Button createGroupButton = new Button("创建组");
    Button joinGroupButton = new Button("加入组");

    LabelTextField createGroupNameField = new LabelTextField("创建组名:");
    LabelTextField createGroupPortField = new LabelTextField("游戏端口:");
    LabelTextField joinGroupPortField = new LabelTextField("开放端口:");
    LabelTextField joinGroupUUIDField = new LabelTextField("选择组(UUID):");

    {
        createGroupButton.setMaxWidth(Double.MAX_VALUE);
        joinGroupButton.setMaxWidth(Double.MAX_VALUE);
    }

    VBox leftBox = new VBox(createGroupNameField, createGroupPortField, createGroupButton);
    VBox rightBox = new VBox(joinGroupUUIDField, joinGroupPortField, joinGroupButton);

    {
        leftBox.setSpacing(8);
        //leftBox.setPadding(new Insets(8));
        leftBox.setAlignment(Pos.BOTTOM_CENTER);

        rightBox.setSpacing(8);
        //rightBox.setPadding(new Insets(8));
        rightBox.setAlignment(Pos.BOTTOM_CENTER);

        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
    }


    HBox bottom = new HBox(leftBox, rightBox);


    {
        setSpacing(8);
        bottom.setSpacing(8);
        setPadding(new Insets(8));

        tableView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tableView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(tableView, bottom);
        //bottom.setPadding(new Insets(8));
    }

    public void rejoinGroup() {
        String joinGroupName = stage.properties.getProperty("joinGroupName", "");
        String mode = stage.properties.getProperty("previousMode", "");

        if ("".equals(mode)) {
            stage.setLinkerTitle("重连组 未配置");
            return;
        }
        if ("User".equals(mode)) {
            stage.setLinkerTitle("重连组 等待重连 " + joinGroupName);
        }
        if ("Host".equals(mode)) {
            stage.setLinkerTitle("重连组 准备创建组 " + createGroupNameField.textField.getText());
            stage.linkerClient.createGroup(createGroupNameField.textField.getText());
            stage.setLinkerTitle("重连组 创建成功");
            return;
        }
        for (Person c : groupMap.values()) {
            if (joinGroupName.equals(c.getGroupName())) {
               if ("User".equals(mode)) {
                    joinGroupUUIDField.textField.setText(c.getGroupUUID());
                    stage.linkerClient.joinGroup(c.getGroupUUID());
                    stage.setLinkerTitle("重连组 加入成功");
                }
                return;
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setLinkerTitle("重连组 未找到");

    }

    public OneBox(LinkerStage stage) {
        this.stage = stage;
        createGroupNameField.textField.setText(stage.properties.getProperty("groupName"));
        createGroupNameField.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                stage.properties.put("groupName", createGroupNameField.textField.getText());
                stage.saveProperties();
            }
        });

        createGroupPortField.textField.setText(stage.properties.getProperty("hostPort"));
        createGroupPortField.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                stage.properties.put("hostPort", createGroupPortField.textField.getText());
                stage.saveProperties();
            }
        });

        joinGroupPortField.textField.setText(stage.properties.getProperty("userPort"));
        joinGroupPortField.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                stage.properties.put("userPort", joinGroupPortField.textField.getText());
                stage.saveProperties();
            }
        });


        // 创建一个 Timeline，每秒更新一次时间
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
           /* LocalTime currentTime = LocalTime.now();
            String formattedTime = currentTime.format(timeFormatter);*/
            stage.linkerClient.getGroups();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // 设置为无限循环


        parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                timeline.play(); // 启动计时器
            } else {
                timeline.pause(); //暂停
            }
        });
    }

    {
        createGroupButton.setOnAction(event -> {
            stage.linkerClient.createGroup(createGroupNameField.textField.getText());
            stage.properties.setProperty("previousMode", "Host");
            stage.saveProperties();
        });

        joinGroupButton.setOnAction(event -> {
            stage.linkerClient.joinGroup(joinGroupUUIDField.textField.getText());
            stage.properties.setProperty("previousMode", "User");
            stage.saveProperties();
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                joinGroupUUIDField.textField.setText(newValue.getGroupUUID());
                stage.properties.setProperty("joinGroupName", newValue.getGroupName());
                stage.saveProperties();
            }
        });
    }

    public LinkerClient.LinkerClientEvent handleEvent = (linkerUser, event, object) -> {
        switch (event) {
            case USER_GET_START -> {
                Platform.runLater(() -> {

                });
            }
            case USER_LOGIN -> {

            }
            case USER_GET_GROUPS -> {
                JSONArray groups = object.getJSONArray("groups");


                ArrayList<UUID> removed = new ArrayList<>(groupMap.keySet());
                for (int i = 0; i < groups.size(); i++) {
                    LinkerGroup group = LinkerGroup.build(groups.getJSONObject(i));

                    Person person = groupMap.get(group.getUUID());
                    if (person == null) {

                        person = new Person(group.name, group.host.name, group.members.size() + "人", group.host.delay + "ms", TimeFormatter.formatTimestamp(group.createTime), group.getUUID().toString());
                        groupMap.put(group.getUUID(), person);
                        data.add(person);
                    } else {
                        person.setDelay(group.host.delay + "ms");
                        person.setMembers(group.members.size() + "人");
                        person.setCreator(group.host.name);
                    }
                    removed.remove(group.getUUID());
                }
                tableView.refresh();
                for (UUID removedGroup : removed) {
                    data.remove(groupMap.get(removedGroup));
                    groupMap.remove(removedGroup);
                }
            }
        }
    };
}
