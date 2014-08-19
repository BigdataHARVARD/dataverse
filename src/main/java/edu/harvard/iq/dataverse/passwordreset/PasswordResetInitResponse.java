package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.util.SystemConfig;

public class PasswordResetInitResponse {

    /**
     * @todo Do we really need emailFound? Just check if passwordResetData is
     * null or not instead?
     */
    private boolean emailFound;
    private String resetUrl;
    private PasswordResetData passwordResetData;

    public PasswordResetInitResponse(boolean emailFound) {
        this.emailFound = emailFound;
    }

    public PasswordResetInitResponse(boolean emailFound, PasswordResetData passwordResetData) {
        this.emailFound = emailFound;
        this.passwordResetData = passwordResetData;
        this.resetUrl = "https://" + System.getProperty(SystemConfig.FQDN) + "/passwordreset.xhtml?token=" + passwordResetData.getToken();
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
