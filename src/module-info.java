module com.example.projetv4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.projetv4 to javafx.fxml;
    exports com.example.projetv4;
}