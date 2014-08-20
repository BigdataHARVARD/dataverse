package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.DataverseUserServiceBean;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
@Named
public class PasswordResetServiceBean {

    private static final Logger logger = Logger.getLogger(PasswordResetServiceBean.class.getCanonicalName());

    @EJB
    DataverseUserServiceBean dataverseUserService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;

    /**
     * Initiate the password reset process.
     *
     * @param emailAddress
     * @return {@link PasswordResetInitResponse}
     * @throws edu.harvard.iq.dataverse.passwordreset.PasswordResetException
     */
    // inspired by Troy Hunt: Everything you ever wanted to know about building a secure password reset feature - http://www.troyhunt.com/2012/05/everything-you-ever-wanted-to-know.html
    public PasswordResetInitResponse requestReset(String emailAddress) throws PasswordResetException {
        DataverseUser user = dataverseUserService.findByEmail(emailAddress);
        if (user != null) {
            // delete old tokens for the user
            List<PasswordResetData> oldTokens = findPasswordResetDataByDataverseUser(user);
            for (PasswordResetData oldToken : oldTokens) {
                em.remove(oldToken);
            }
            // create a fresh token for the user
            PasswordResetData passwordResetData = new PasswordResetData(user);
            PasswordResetData savedPasswordResetData = null;
            try {
                em.persist(passwordResetData);
                savedPasswordResetData = em.merge(passwordResetData);
            } catch (Exception ex) {
                String msg = "Unable to save token for " + emailAddress;
                throw new PasswordResetException(msg);
            }
            if (savedPasswordResetData != null) {
                /**
                 * @todo send email
                 */
            }
            return new PasswordResetInitResponse(true, passwordResetData);
        } else {
            return new PasswordResetInitResponse(false);
        }
    }

    /**
     * Process the password reset token, allowing the user to reset the password
     * or report on a invalid token.
     *
     * @param token
     */
    public PasswordResetExecResponse processToken(String token) {
//        TypedQuery<PasswordResetData> typedQuery = em.createQuery("SELECT OBJECT(o) FROM AuthenticatedUser AS o where o.identifier = :username", PasswordResetData.class);
//        TypedQuery<PasswordResetData> typedQuery = em.createQuery("SELECT OBJECT(o) FROM PasswordResetData AS o WHERE o.token = :token", PasswordResetData.class);
//        typedQuery.setParameter("token", token);
//        try {
//            PasswordResetData passwordResetData = typedQuery.getSingleResult();
        PasswordResetData passwordResetData = findSinglePasswordResetDataByToken(token);
        return new PasswordResetExecResponse(token, passwordResetData);
//        } catch (NoResultException nre) {
//            throw new RuntimeException("no result!");
//        } catch (NonUniqueResultException nure) {
//            throw new RuntimeException("non unique result");
//        }
    }

    /**
     * @param token
     * @return Null or a single row of password reset data.
     */
    private PasswordResetData findSinglePasswordResetDataByToken(String token) {
        PasswordResetData passwordResetData = null;
        TypedQuery<PasswordResetData> typedQuery = em.createQuery("SELECT OBJECT(o) FROM PasswordResetData AS o WHERE o.token = :token", PasswordResetData.class);
        typedQuery.setParameter("token", token);
        try {
            passwordResetData = typedQuery.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            logger.info("When looking up " + token + " caught " + ex);
        }
        return passwordResetData;
    }

    public List<PasswordResetData> findPasswordResetDataByDataverseUser(DataverseUser user) {
        TypedQuery<PasswordResetData> typedQuery = em.createQuery("SELECT OBJECT(o) FROM PasswordResetData AS o WHERE o.dataverseUser = :user", PasswordResetData.class);
        typedQuery.setParameter("user", user);
        List<PasswordResetData> passwordResetDatas = typedQuery.getResultList();
        return passwordResetDatas;
    }
}
