package persistence;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

/**
 * Bean encapsulating all operations for a person.
 */
@Stateless
@LocalBean
public class UserBean {
    @PersistenceContext
    private EntityManager em;
    private static final String PERSISTENCE_UNIT_NAME = "cloudhr_server";  
    private static EntityManagerFactory factory;   
    
    public UserBean(){
        factory = Persistence.createEntityManagerFactory(  
                PERSISTENCE_UNIT_NAME);  
        EntityManager em = factory.createEntityManager();  
    }

    /**
     * Get all users from the table.
     */
    public List<User> getAllUsers() {
    	return em.createNamedQuery("AllUsers", User.class).getResultList();
    }

    /**
     * Add a user to the table.
     */
    public void addUser(User person) {
        em.persist(person);
        em.flush();
    }
}
