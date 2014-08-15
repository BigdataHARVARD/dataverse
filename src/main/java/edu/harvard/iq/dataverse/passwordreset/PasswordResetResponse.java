package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.util.SystemConfig;

public class PasswordResetResponse {

    /**
     * @todo Do we really need emailFound? Just check if passwordResetData is
     * null or not instead?
     */
    boolean emailFound;
    String resetUrl;
    PasswordResetData passwordResetData;

    public PasswordResetResponse(boolean emailFound) {
        this.emailFound = emailFound;
    }

    public PasswordResetResponse(boolean emailFound, PasswordResetData passwordResetData) {
        this.emailFound = emailFound;
        this.passwordResetData = passwordResetData;
        this.resetUrl = "https://" + System.getProperty(SystemConfig.FQDN) + "/passwordreset/" + passwordResetData.getToken();
    }

    public boolean isEmailFound() {
        return emailFound;
    }

    public String getResetUrl() {
        return resetUrl;
    }

    public PasswordResetData getPasswordResetData() {
        return passwordResetData;
    }

}
