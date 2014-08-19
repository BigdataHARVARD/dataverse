package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.DataverseUserServiceBean;
import edu.harvard.iq.dataverse.PasswordEncryption;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@ViewScoped
@Named("PasswordResetPage")
public class PasswordResetPage {

    private static final Logger logger = Logger.getLogger(PasswordResetPage.class.getCanonicalName());

    @EJB
    PasswordResetServiceBean passwordResetService;
    @EJB
    DataverseUserServiceBean dataverseUserService;

    /**
     * The unique string used to look up a user and continue the password reset
     * process.
     */
    String token;

    /**
     * The user looked up by the token who will be setting a new password.
     */
    DataverseUser user;

    /**
     * The email address that is entered to initiate the password reset process.
     */
    String emailAddress;

    /**
     * The link that is emailed to the user to reset the password that contains
     * a token.
     */
    String passwordResetUrl;

    /**
     * The new password the user enters.
     */
    String newPassword;

    public void init() {
        if (token != null) {
            PasswordResetExecResponse passwordResetExecResponse = passwordResetService.processToken(token);
            PasswordResetData passwordResetData = passwordResetExecResponse.getPasswordResetData();
            if (passwordResetData != null) {
                user = passwordResetData.getDataverseUser();
            }
        }
    }

    public void sendPasswordResetLink() {
        logger.info("Send link button clicked. Email address provided: " + emailAddress);
        try {
            PasswordResetInitResponse passwordResetInitResponse = passwordResetService.requestReset(emailAddress);
            PasswordResetData passwordResetData = passwordResetInitResponse.getPasswordResetData();
            if (passwordResetData != null) {
                DataverseUser user = passwordResetData.getDataverseUser();
                passwordResetUrl = passwordResetInitResponse.getResetUrl();
                logger.info("Found account using " + emailAddress + ": " + user.getUserName() + " and sending link " + passwordResetUrl);
            } else {
                logger.info("Couldn't find account using " + emailAddress);
            }
        } catch (PasswordResetException ex) {
            /**
             * @todo do we really need a special exception for this??
             */
            logger.info("Error: " + ex);
        }
    }

    public void resetPassword() {
        if (user != null) {
            if (newPassword != null) {
                /**
                 * @todo SANITY CHECKS ON THE PASSWORD! Is is long and complex
                 * enough?
                 */
                user.setEncryptedPassword(PasswordEncryption.getInstance().encrypt(newPassword));
                DataverseUser savedUser = dataverseUserService.save(user);
                if (savedUser != null) {
                    /**
                     * @todo Communicate back to user if the reset was
                     * successful or not.
                     */
                }
            }
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DataverseUser getUser() {
        return user;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPasswordResetUrl() {
        return passwordResetUrl;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

}
