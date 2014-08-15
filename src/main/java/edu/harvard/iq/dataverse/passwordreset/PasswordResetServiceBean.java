package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.DataverseUserServiceBean;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@Named
public class PasswordResetServiceBean {

    @EJB
    DataverseUserServiceBean dataverseUserService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;

    /**
     * Initiate the password reset process.
     *
     * @param emailAddress
     * @return {@link PasswordResetResponse}
     * @throws edu.harvard.iq.dataverse.passwordreset.PasswordResetException
     */
    // inspired by Troy Hunt: Everything you ever wanted to know about building a secure password reset feature - http://www.troyhunt.com/2012/05/everything-you-ever-wanted-to-know.html
    public PasswordResetResponse requestReset(String emailAddress) throws PasswordResetException {
        DataverseUser user = dataverseUserService.findByEmail(emailAddress);
        if (user != null) {
            /**
             * @todo delete any existing tokens for the user
             */
            PasswordResetData passwordResetData = new PasswordResetData(user);
            try {
                em.persist(passwordResetData);
                em.merge(passwordResetData);
            } catch (Exception ex) {
                String msg = "Unable to save token for " + emailAddress;
                throw new PasswordResetException(msg);
            }
            return new PasswordResetResponse(true, passwordResetData);
        } else {
            return new PasswordResetResponse(false);
        }
    }

}
