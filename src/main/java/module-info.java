module sn.ouleymatou.hotelmanagement {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Persistence
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // iText PDF (automatic modules)
    requires kernel;
    requires layout;
    requires io;


    // Pour manipuler des images (SwingFXUtils)
    requires javafx.swing;

    // Ouverture des packages pour JavaFX et Hibernate
    opens sn.ouleymatou.hotelmanagement to javafx.fxml;
    opens sn.ouleymatou.hotelmanagement.controllers to javafx.fxml;
    opens sn.ouleymatou.hotelmanagement.entities to javafx.base, org.hibernate.orm.core;

    exports sn.ouleymatou.hotelmanagement;
}
