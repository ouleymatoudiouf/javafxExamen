package sn.ouleymatou.hotelmanagement.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import sn.ouleymatou.hotelmanagement.entities.TypeChambre;
import sn.ouleymatou.hotelmanagement.utils.JPAUtils;

import java.util.List;

public class TypeChambreService {

    /**
     * Récupère tous les types de chambres.
     */
    public List<TypeChambre> getAll() {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<TypeChambre> query = em.createQuery("SELECT t FROM TypeChambre t", TypeChambre.class);
            return query.getResultList();
        }
    }

    /**
     * Sauvegarde ou met à jour un type de chambre.
     */
    public void save(TypeChambre type) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtils.getEntityManagerFactory().createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            if (type.getId() == null) {
                em.persist(type);
            } else {
                em.merge(type);
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors de l'enregistrement du type de chambre : " + ex.getMessage(), ex);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Supprime un type de chambre par son ID.
     */
    public void delete(Long id) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtils.getEntityManagerFactory().createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            TypeChambre type = em.find(TypeChambre.class, id);
            if (type != null) {
                em.remove(type);
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors de la suppression du type de chambre : " + ex.getMessage(), ex);
        } finally {
            if (em != null) em.close();
        }
    }

    /**
     * Récupère un type de chambre par son ID.
     */
    public TypeChambre findById(Long id) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            return em.find(TypeChambre.class, id);
        }
    }

    public TypeChambre[] Lister() {
        return new TypeChambre[0];
    }
}
