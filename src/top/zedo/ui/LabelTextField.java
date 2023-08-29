package top.zedo.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LabelTextField extends HBox {
    public Label label = new Label();
    public TextField textField = new TextField();

    public LabelTextField(String label) {
        this.label.setText(label);
        this.label.setPrefWidth(80);
        setAlignment(Pos.CENTER);
        setSpacing(8);
        getChildren().addAll(this.label, textField);
    }
}
