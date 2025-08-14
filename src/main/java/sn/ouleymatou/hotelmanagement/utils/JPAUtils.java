package sn.ouleymatou.hotelmanagement.utils;



import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtils {
    private static final String PERSISTENCE_UNIT_NAME = "PERSISTENCE_POSTGRES";
    private static EntityManagerFactory factory;

    public static EntityManagerFactory getEntityManagerFactory() {
        if(factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

    // ✅ Ajoute cette méthode :
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
}
}
