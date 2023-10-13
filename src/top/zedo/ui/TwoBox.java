package top.zedo.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import top.zedo.data.LinkerGroup;
import top.zedo.data.LinkerUser;
import top.zedo.net.LinkerClient;
import top.zedo.util.ByteFormatter;
import top.zedo.util.TimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
        private final SimpleStringProperty upstreamPackets = new SimpleStringProperty();
        private final SimpleStringProperty downstreamPackets = new SimpleStringProperty();
        private final SimpleStringProperty totalUpstreamPackets = new SimpleStringProperty();
        private final SimpleStringProperty totalDownstreamPackets = new SimpleStringProperty();

        public String getUpstreamPackets() {
            return upstreamPackets.get();
        }

        public SimpleStringProperty upstreamPacketsProperty() {
            return upstreamPackets;
        }

        public void setUpstreamPackets(String upstreamPackets) {
            this.upstreamPackets.set(upstreamPackets);
        }

        public String getDownstreamPackets() {
            return downstreamPackets.get();
        }

        public SimpleStringProperty downstreamPacketsProperty() {
            return downstreamPackets;
        }

        public void setDownstreamPackets(String downstreamPackets) {
            this.downstreamPackets.set(downstreamPackets);
        }

        public String getTotalUpstreamPackets() {
            return totalUpstreamPackets.get();
        }

        public SimpleStringProperty totalUpstreamPacketsProperty() {
            return totalUpstreamPackets;
        }

        public void setTotalUpstreamPackets(String totalUpstreamPackets) {
            this.totalUpstreamPackets.set(totalUpstreamPackets);
        }

        public String getTotalDownstreamPackets() {
            return totalDownstreamPackets.get();
        }

        public SimpleStringProperty totalDownstreamPacketsProperty() {
            return totalDownstreamPackets;
        }

        public void setTotalDownstreamPackets(String totalDownstreamPackets) {
            this.totalDownstreamPackets.set(totalDownstreamPackets);
        }

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


    TableColumn<Person, String> upstreamPacketsCol = new TableColumn<>("上传包");
    TableColumn<Person, String> downstreamPacketsCol = new TableColumn<>("下载包");
    TableColumn<Person, String> totalUpstreamPacketsCol = new TableColumn<>("总上传包");
    TableColumn<Person, String> totalDownstreamPacketsCol = new TableColumn<>("总下载包");

    HBox colBox = new HBox();

    LinkerStage stage;

    private void refreshTable() {
        // 保存列的可见状态
        Map<TableColumn, Boolean> columnVisibilityMap = new HashMap<>();
        tableView.getColumns().forEach(col -> columnVisibilityMap.put(col, col.isVisible()));

// 将所有列设为可见
        tableView.getColumns().forEach(col -> col.setVisible(true));

// 重新计算列宽
        tableView.layout();

// 恢复列的可见状态
        columnVisibilityMap.forEach((col, isVisible) -> col.setVisible(isVisible));

    }

    {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        delayCol.setCellValueFactory(new PropertyValueFactory<>("delay"));
        ipAddressCol.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        loginTimeCol.setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        upTrafficCol.setCellValueFactory(new PropertyValueFactory<>("upTraffic"));
        downTrafficCol.setCellValueFactory(new PropertyValueFactory<>("downTraffic"));
        totalUpBytesCol.setCellValueFactory(new PropertyValueFactory<>("totalUpBytes"));
        totalDownBytesCol.setCellValueFactory(new PropertyValueFactory<>("totalDownBytes"));
        upstreamPacketsCol.setCellValueFactory(new PropertyValueFactory<>("upstreamPackets"));
        downstreamPacketsCol.setCellValueFactory(new PropertyValueFactory<>("downstreamPackets"));
        totalUpstreamPacketsCol.setCellValueFactory(new PropertyValueFactory<>("totalUpstreamPackets"));
        totalDownstreamPacketsCol.setCellValueFactory(new PropertyValueFactory<>("totalDownstreamPackets"));
        nameCol.setPrefWidth(140);
        delayCol.setPrefWidth(60);
        ipAddressCol.setPrefWidth(120);
        loginTimeCol.setPrefWidth(100);
        upTrafficCol.setPrefWidth(100);
        downTrafficCol.setPrefWidth(100);
        totalUpBytesCol.setPrefWidth(100);
        totalDownBytesCol.setPrefWidth(100);
        upstreamPacketsCol.setPrefWidth(100);
        downstreamPacketsCol.setPrefWidth(100);
        totalUpstreamPacketsCol.setPrefWidth(100);
        totalDownstreamPacketsCol.setPrefWidth(100);


        nameCol.setMinWidth(140);
        delayCol.setMinWidth(60);
        ipAddressCol.setMinWidth(120);
        loginTimeCol.setMinWidth(100);
        upTrafficCol.setMinWidth(60);
        downTrafficCol.setMinWidth(60);
        totalUpBytesCol.setMinWidth(60);
        totalDownBytesCol.setMinWidth(60);
        upstreamPacketsCol.setMinWidth(60);
        downstreamPacketsCol.setMinWidth(60);
        totalUpstreamPacketsCol.setMinWidth(60);
        totalDownstreamPacketsCol.setMinWidth(60);


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

        upstreamPacketsCol.setCellFactory(cellFactory);
        downstreamPacketsCol.setCellFactory(cellFactory);
        totalUpstreamPacketsCol.setCellFactory(cellFactory);
        totalDownstreamPacketsCol.setCellFactory(cellFactory);

        tableView.getColumns().addAll(nameCol, delayCol, ipAddressCol, loginTimeCol, upTrafficCol, downTrafficCol, totalUpBytesCol, totalDownBytesCol,
                upstreamPacketsCol, downstreamPacketsCol, totalUpstreamPacketsCol, totalDownstreamPacketsCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);


        tableView.setItems(data);

        colBox.setSpacing(4);
        colBox.setAlignment(Pos.CENTER_RIGHT);


        {
            CheckBox checkBox = new CheckBox("全部");
            checkBox.setOnAction(event -> {
                for (TableColumn<Person, ?> column : tableView.getColumns()) {
                    column.setVisible(checkBox.isSelected());
                    stage.properties.put(column.getText(), checkBox.isSelected() ? "true" : "false");
                }
                stage.saveProperties();
            });

            colBox.getChildren().add(checkBox);
        }


        for (TableColumn<Person, ?> column : tableView.getColumns()) {
            CheckBox checkBox = new CheckBox(column.getText());
            checkBox.setOnAction(new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    stage.properties.put(checkBox.getText(), checkBox.isSelected() ? "true" : "false");
                    stage.saveProperties();
                }
            });
            column.visibleProperty().bindBidirectional(checkBox.selectedProperty());
            colBox.getChildren().add(checkBox);
        }


    }


    Button leaveButton = new Button("离开组");

    {
        setSpacing(8);
        setPadding(new Insets(8));
        VBox.setVgrow(this, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        leaveButton.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(colBox, tableView, leaveButton);
    }


    public TwoBox(LinkerStage stage) {
        this.stage = stage;

        leaveButton.setOnAction(event -> {
            stage.linkerClient.leaveGroup();
            stage.changePane(true);
            stage.linkerClient.proxyNetwork.close();
        });
        for (TableColumn<Person, ?> column : tableView.getColumns()) {
            column.setVisible("true".contains(stage.properties.getProperty(column.getText(), "true")));
        }
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

                    person.setUpstreamPackets(ByteFormatter.formatPackets(user.upstreamPackets) + "/s");
                    person.setDownstreamPackets(ByteFormatter.formatPackets(user.downstreamPackets) + "/s");
                    person.setTotalUpstreamPackets(ByteFormatter.formatPackets(user.totalUpstreamPackets));
                    person.setTotalDownstreamPackets(ByteFormatter.formatPackets(user.totalDownstreamPackets));

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
