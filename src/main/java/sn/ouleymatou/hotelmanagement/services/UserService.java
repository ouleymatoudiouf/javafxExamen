package sn.ouleymatou.hotelmanagement.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import sn.ouleymatou.hotelmanagement.entities.User;
import sn.ouleymatou.hotelmanagement.utils.JPAUtils;

public class UserService {

    /**
     * Recherche un utilisateur via son email
     */
    public User findByEmail(String email) {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManagerFactory().createEntityManager();
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class
            );
            query.setParameter("email", email.trim());

            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Authentifie un utilisateur avec email et mot de passe
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;

        email = email.trim();
        password = password.trim();

        User user = findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Sauvegarde un nouvel utilisateur
     */
    public User save(User user) {
        EntityManager em = null;
        EntityTransaction transaction = null;

        try {
            em = JPAUtils.getEntityManagerFactory().createEntityManager();
            transaction = em.getTransaction();

            transaction.begin();
            em.persist(user);
            transaction.commit();

            return user;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur : " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Vérifie l'existence d'un utilisateur par email
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    /**
     * Crée un admin par défaut s’il n’existe pas
     */
    public void createDefaultAdminIfNotExists() {
        String adminEmail = "ouley09@gmail.com";
        String adminPassword = "ou123";

        if (!existsByEmail(adminEmail)) {
            User adminUser = new User(adminEmail, adminPassword);
            save(adminUser);
            System.out.println("Utilisateur admin créé avec succès : " + adminEmail);
        } else {
            System.out.println("Utilisateur admin existe déjà : " + adminEmail);
        }
    }
}
