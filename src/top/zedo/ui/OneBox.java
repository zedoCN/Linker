package top.zedo.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class OneBox extends VBox {
    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;

        public Person(String firstName, String lastName) {
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public String getLastName() {
            return lastName.get();
        }
    }

    TableView<Person> tableView = new TableView<>();
    TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
    TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");


    {
        // 使用 PropertyValueFactory 来设置列的数据提取方式
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        tableView.getColumns().addAll(firstNameCol, lastNameCol);

        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("John", "Doe"),
                new Person("Jane", "Smith"),
                new Person("Bob", "Johnson")
        );

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
        leftBox.setPadding(new Insets(5));
        leftBox.setAlignment(Pos.BOTTOM_CENTER);

        rightBox.setSpacing(8);
        rightBox.setPadding(new Insets(5));
        rightBox.setAlignment(Pos.BOTTOM_CENTER);

        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
    }

    HBox bottom = new HBox(leftBox, rightBox);


    {
        tableView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tableView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(tableView, bottom);
        bottom.setPadding(new Insets(8));
    }
}
