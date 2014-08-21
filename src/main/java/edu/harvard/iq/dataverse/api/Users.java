package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.passwordreset.PasswordResetData;
import edu.harvard.iq.dataverse.passwordreset.PasswordResetException;
import edu.harvard.iq.dataverse.passwordreset.PasswordResetExecResponse;
import edu.harvard.iq.dataverse.passwordreset.PasswordResetInitResponse;
import edu.harvard.iq.dataverse.passwordreset.PasswordResetServiceBean;
import static edu.harvard.iq.dataverse.util.json.JsonPrinter.json;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author michael
 */
@Path("users")
public class Users extends AbstractApiBean {

    private static final Logger logger = Logger.getLogger(Users.class.getName());

    @EJB
    PasswordResetServiceBean passwordResetService;

    @GET
    public Response list() {
        JsonArrayBuilder bld = Json.createArrayBuilder();

        for (DataverseUser u : userSvc.findAll()) {
            bld.add(json(u));
        }

        return okResponse(bld);
    }

    @GET
    @Path("{identifier}")
    public Response view(@PathParam("identifier") String identifier) {
        DataverseUser u = findUser(identifier);

        return (u != null)
                ? okResponse(json(u))
                : errorResponse(Status.NOT_FOUND, "Can't find user with identifier '" + identifier + "'");
    }

    @POST
    public Response save(DataverseUser user, @QueryParam("password") String password) {
        try {
            if (password != null) {
                user.setEncryptedPassword(userSvc.encryptPassword(password));
            }
            user = userSvc.save(user);
            return okResponse(json(user));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error saving user", e);
            return errorResponse(Status.INTERNAL_SERVER_ERROR, "Can't save user: " + e.getMessage());
        }
    }

    @GET
    @Path(":guest")
    public Response genarateGuest() {
        return okResponse(json(userSvc.createGuestUser()));

    }

    /**
     * Initiate a password reset request.
     *
     * @param emailAddress
     *
     * @todo Should we disable this method? Probably.
     */
    @GET
    @Path("passwordreset/init/{email}")
    public Response passwordResetRequestInit(@PathParam("email") String emailAddress) {
        try {
            PasswordResetInitResponse passwordResetResponse = passwordResetService.requestReset(emailAddress);
            PasswordResetData passwordResetData = passwordResetResponse.getPasswordResetData();
            if (passwordResetData != null) {
                return okResponsePretty("Password reset email has been sent for " + emailAddress
                        + ". Email found: " + passwordResetResponse.isEmailFound()
                        + ". User: " + passwordResetData.getDataverseUser().getUserName()
                        + ". Token create timestamp: " + passwordResetData.getCreated()
                        + ". Token expiration timestamp: " + passwordResetData.getExpires()
                        + ". Reset URL: " + passwordResetResponse.getResetUrl());
            } else {
                // don't let spammers know if we have the email address on file or not.
                return okResponsePretty("Password reset email has been sent for " + emailAddress);
            }
        } catch (PasswordResetException | EJBException ex) {
            /**
             * @todo why are we worried about catching EJBException here?
             */
            Throwable cause = ex.getCause();
            return errorResponse(Status.INTERNAL_SERVER_ERROR, ex + " caused by " + cause);
        }
    }

    /**
     * Execute a password reset.
     *
     * @param token
     *
     * @todo Should we disable this method? Probably.
     */
    @GET
    @Path("passwordreset/exec/{token}")
    public Response passwordResetRequestExec(@PathParam("token") String token) {
        PasswordResetExecResponse passwordResetExecResponse = passwordResetService.processToken(token);
        PasswordResetData passwordResetData = passwordResetExecResponse.getPasswordResetData();
        if (passwordResetData != null) {
            DataverseUser user = passwordResetData.getDataverseUser();
            return okResponsePretty("token " + passwordResetExecResponse.getTokenQueried() + " belongs to " + user.getUserName());
        } else {
            return errorResponse(Status.NOT_FOUND, "Token not found: " + token);
        }
    }

    /**
     * Strictly for debugging.
     *
     * @todo disable this method or do a Permission check.
     */
    @GET
    @Path("passwordreset/debug")
    public Response passwordResetDebug() {
        List<PasswordResetData> allPasswordResetData = passwordResetService.findAllPasswordResetData();
        List<String> rows = new ArrayList<>();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (PasswordResetData data : allPasswordResetData) {
            Timestamp expires = data.getExpires();
            boolean expired = data.isExpired();
            jsonArrayBuilder.add(data.getToken() + "|" + expires + "|expired:" + expired + "|" + data.getDataverseUser().getUserName());
        }
        return okResponsePretty(jsonArrayBuilder);
    }

}
