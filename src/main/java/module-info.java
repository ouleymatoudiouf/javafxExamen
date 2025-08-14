module sn.ouleymatou.hotelmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Ouvre les packages à la réflexion pour JavaFX (FXML + PropertyValueFactory)
    opens sn.ouleymatou.hotelmanagement to javafx.fxml;
    opens sn.ouleymatou.hotelmanagement.controllers to javafx.fxml;
    opens sn.ouleymatou.hotelmanagement.entities to javafx.base, org.hibernate.orm.core;

    // Exporte le package principal si utilisé dans d'autres modules
    exports sn.ouleymatou.hotelmanagement;
}
