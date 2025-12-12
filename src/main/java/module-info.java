module com.example.ecommerse_college {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.example.ecommerse_college to javafx.fxml;
    exports com.example.ecommerse_college;
}
