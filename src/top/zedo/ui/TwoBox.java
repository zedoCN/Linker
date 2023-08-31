package top.zedo.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import top.zedo.data.LinkerGroup;
import top.zedo.data.LinkerUser;
import top.zedo.net.LinkerClient;
import top.zedo.util.ByteFormatter;
import top.zedo.util.TimeFormatter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TwoBox extends VBox {

    public static class Person {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty delay = new SimpleStringProperty();
        private final SimpleStringProperty ipAddress = new SimpleStringProperty();
        private final SimpleStringProperty loginTime = new SimpleStringProperty();
        private final SimpleStringProperty upTraffic = new SimpleStringProperty();
        private final SimpleStringProperty downTraffic = new SimpleStringProperty();
        private final SimpleStringProperty totalUpBytes = new SimpleStringProperty();
        private final SimpleStringProperty totalDownBytes = new SimpleStringProperty();

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getDelay() {
            return delay.get();
        }

        public SimpleStringProperty delayProperty() {
            return delay;
        }

        public void setDelay(String delay) {
            this.delay.set(delay);
        }

        public String getIpAddress() {
            return ipAddress.get();
        }

        public SimpleStringProperty ipAddressProperty() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress.set(ipAddress);
        }

        public String getLoginTime() {
            return loginTime.get();
        }

        public SimpleStringProperty loginTimeProperty() {
            return loginTime;
        }

        public void setLoginTime(String loginTime) {
            this.loginTime.set(loginTime);
        }

        public String getUpTraffic() {
            return upTraffic.get();
        }

        public SimpleStringProperty upTrafficProperty() {
            return upTraffic;
        }

        public void setUpTraffic(String upTraffic) {
            this.upTraffic.set(upTraffic);
        }

        public String getDownTraffic() {
            return downTraffic.get();
        }

        public SimpleStringProperty downTrafficProperty() {
            return downTraffic;
        }

        public void setDownTraffic(String downTraffic) {
            this.downTraffic.set(downTraffic);
        }

        public String getTotalUpBytes() {
            return totalUpBytes.get();
        }

        public SimpleStringProperty totalUpBytesProperty() {
            return totalUpBytes;
        }

        public void setTotalUpBytes(String totalUpBytes) {
            this.totalUpBytes.set(totalUpBytes);
        }

        public String getTotalDownBytes() {
            return totalDownBytes.get();
        }

        public SimpleStringProperty totalDownBytesProperty() {
            return totalDownBytes;
        }

        public void setTotalDownBytes(String totalDownBytes) {
            this.totalDownBytes.set(totalDownBytes);
        }
    }

    ObservableList<Person> data = FXCollections.observableArrayList();
    HashMap<UUID, Person> userMap = new HashMap<>();
    TableView<Person> tableView = new TableView<>();


    TableColumn<Person, String> nameCol = new TableColumn<>("昵称");
    TableColumn<Person, String> delayCol = new TableColumn<>("延迟");
    TableColumn<Person, String> ipAddressCol = new TableColumn<>("IP");
    TableColumn<Person, String> loginTimeCol = new TableColumn<>("登录时间");
    TableColumn<Person, String> upTrafficCol = new TableColumn<>("上传");
    TableColumn<Person, String> downTrafficCol = new TableColumn<>("下载");
    TableColumn<Person, String> totalUpBytesCol = new TableColumn<>("总上传");
    TableColumn<Person, String> totalDownBytesCol = new TableColumn<>("总下载");


    {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        delayCol.setCellValueFactory(new PropertyValueFactory<>("delay"));
        ipAddressCol.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        loginTimeCol.setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        upTrafficCol.setCellValueFactory(new PropertyValueFactory<>("upTraffic"));
        downTrafficCol.setCellValueFactory(new PropertyValueFactory<>("downTraffic"));
        totalUpBytesCol.setCellValueFactory(new PropertyValueFactory<>("totalUpBytes"));
        totalDownBytesCol.setCellValueFactory(new PropertyValueFactory<>("totalDownBytes"));
        nameCol.setPrefWidth(140);
        delayCol.setPrefWidth(60);
        ipAddressCol.setPrefWidth(120);
        loginTimeCol.setPrefWidth(100);

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
        nameCol.setCellFactory(cellFactory);
        delayCol.setCellFactory(cellFactory);
        ipAddressCol.setCellFactory(cellFactory);
        loginTimeCol.setCellFactory(cellFactory);
        upTrafficCol.setCellFactory(cellFactory);
        downTrafficCol.setCellFactory(cellFactory);
        totalUpBytesCol.setCellFactory(cellFactory);
        totalDownBytesCol.setCellFactory(cellFactory);

        tableView.getColumns().addAll(nameCol, delayCol, ipAddressCol, loginTimeCol, upTrafficCol, downTrafficCol, totalUpBytesCol, totalDownBytesCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);


        tableView.setItems(data);
    }

    Button leaveButton = new Button("离开组");

    {
        setSpacing(8);
        setPadding(new Insets(8));
        VBox.setVgrow(this, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        leaveButton.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(tableView, leaveButton);
    }

    LinkerStage stage;

    public TwoBox(LinkerStage stage) {
        this.stage = stage;

        leaveButton.setOnAction(event -> {
            stage.linkerClient.leaveGroup();
            stage.changePane(true);
            stage.linkerClient.proxyNetwork.close();
        });
    }


    public LinkerClient.LinkerClientEvent handleEvent = (linkerUser, event, object) -> {
        switch (event) {
            case GROUP_UPDATE -> {
                LinkerGroup group = LinkerGroup.build(object);
                ArrayList<UUID> removed = new ArrayList<>(userMap.keySet());

                for (LinkerUser user : group.members.values()) {
                    Person person = userMap.get(user.getUUID());
                    if (person == null) {
                        person = new Person();

                        userMap.put(user.getUUID(), person);
                        data.add(person);
                    }
                    //更新数据
                    person.setDelay(user.delay + "ms");
                    person.setName(user.name + (user.getUUID().equals(linkerUser.getUUID()) ? " (你)" : "") + ((group.host.getUUID().equals(user.getUUID()) ? " (主机)" : "")));
                    person.setUpTraffic(ByteFormatter.formatBytes(user.upTraffic) + "/s");
                    person.setDownTraffic(ByteFormatter.formatBytes(user.downTraffic) + "/s");
                    person.setTotalUpBytes(ByteFormatter.formatBytes(user.totalUpBytes));
                    person.setTotalDownBytes(ByteFormatter.formatBytes(user.totalDownBytes));
                    person.setIpAddress(user.ipAddress);
                    person.setLoginTime(TimeFormatter.formatTimestamp(user.loginTime));

                    removed.remove(user.getUUID());
                }

                tableView.refresh();
                for (UUID removedGroup : removed) {
                    data.remove(userMap.get(removedGroup));
                    userMap.remove(removedGroup);
                }
            }
            case HOST_DISSOLVE_GROUP -> {
                stage.changePane(true);
                stage.linkerClient.proxyNetwork.close();
            }
        }
    };
}
