package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.DataverseUserServiceBean;
import edu.harvard.iq.dataverse.MailServiceBean;
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

    @EJB
    MailServiceBean mailService;

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
        deleteAllExpiredTokens();
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
                PasswordResetInitResponse passwordResetInitResponse = new PasswordResetInitResponse(true, passwordResetData);
                /**
                 * @todo is the response the best place to store the reset link?
                 */
                String passwordResetUrl = passwordResetInitResponse.getResetUrl();
                String userMessage = "Someone (hopefully you) requested a password reset for " + user.getDisplayName() + " (" + user.getUserName() + "):\n\n"
                        + passwordResetUrl
                        /**
                         * @todo It would be a nice touch to show the IP from
                         * which the password reset originated.
                         */
                        + "\n\nIf you did not request this password reset, please ignore this email.";
                try {
                    mailService.sendDoNotReplyMail(emailAddress, "Dataverse password reset for " + user.getDisplayName(), userMessage);
                } catch (Exception ex) {
                    /**
                     * @todo get more specific about the exception that's thrown
                     * when `asadmin create-javamail-resource` (or equivalent)
                     * hasn't been run.
                     */
                    throw new PasswordResetException("Problem sending password reset email possibily due to mail server not being configured.");
                }
                logger.info("attempted to send mail to " + emailAddress);
                return passwordResetInitResponse;
            } else {
                throw new PasswordResetException("Internal error. Unable to persist password reset token.");
            }
        } else {
            return new PasswordResetInitResponse(false);
        }
    }

    /**
     * Process the password reset token, allowing the user to reset the password
     * or report on a invalid token.
     *
     * @param tokenQueried
     */
    public PasswordResetExecResponse processToken(String tokenQueried) {
        deleteAllExpiredTokens();
        PasswordResetExecResponse tokenUnusable = new PasswordResetExecResponse(tokenQueried, null);
        PasswordResetData passwordResetData = findSinglePasswordResetDataByToken(tokenQueried);
        if (passwordResetData != null) {
            if (passwordResetData.isExpired()) {
                // shouldn't reach here since tokens are being expired above
                return tokenUnusable;
            } else {
                PasswordResetExecResponse goodTokenCanProceed = new PasswordResetExecResponse(tokenQueried, passwordResetData);
                return goodTokenCanProceed;
            }
        } else {
            return tokenUnusable;
        }
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

    public List<PasswordResetData> findAllPasswordResetData() {
        TypedQuery<PasswordResetData> typedQuery = em.createQuery("SELECT OBJECT(o) FROM PasswordResetData AS o", PasswordResetData.class);
        List<PasswordResetData> passwordResetDatas = typedQuery.getResultList();
        return passwordResetDatas;
    }

    /**
     * @return The number of tokens deleted.
     */
    private long deleteAllExpiredTokens() {
        long numDeleted = 0;
        List<PasswordResetData> allData = findAllPasswordResetData();
        for (PasswordResetData data : allData) {
            if (data.isExpired()) {
                em.remove(data);
                numDeleted++;
            }
        }
        logger.fine("expired tokens deleted: " + numDeleted);
        return numDeleted;
    }
}
