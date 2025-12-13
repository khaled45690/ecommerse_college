module com.example.ecommerse_college {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires transitive javafx.graphics;
    // Added module requires for third-party libraries used by Database.java
    requires com.zaxxer.hikari;
    requires java.dotenv; // HikariCP

    opens com.example.ecommerse_college to javafx.fxml;
    exports com.example.ecommerse_college;
}
