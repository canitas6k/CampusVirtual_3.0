module com.campusvirtual {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;
    requires bcrypt;
    requires de.jensd.fx.glyphs.fontawesome;

    opens com.campusvirtual to javafx.fxml;
    opens com.campusvirtual.controller to javafx.fxml;
    opens com.campusvirtual.controller.student to javafx.fxml;
    opens com.campusvirtual.controller.student.cell to javafx.fxml;
    opens com.campusvirtual.controller.professor to javafx.fxml;
    opens com.campusvirtual.controller.admin to javafx.fxml;

    exports com.campusvirtual;
    exports com.campusvirtual.model;
    exports com.campusvirtual.model.enums;
    exports com.campusvirtual.core;
    exports com.campusvirtual.controller;
    exports com.campusvirtual.controller.student;
    exports com.campusvirtual.controller.student.cell;
    exports com.campusvirtual.controller.professor;
    exports com.campusvirtual.controller.admin;
}
