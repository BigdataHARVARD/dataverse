package edu.harvard.iq.dataverse.passwordreset;

import edu.harvard.iq.dataverse.DataverseUser;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class PasswordResetData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String token;

    /**
     * @todo Only allow a given user to have one token at a time.
     */
    @OneToOne
    @JoinColumn(nullable = false)
    private DataverseUser dataverseUser;

    @Column(nullable = false)
    private Timestamp created;

    @Column(nullable = false)
    private Timestamp expires;

    /**
     * This is only here because it has to be: "The class should have a no-arg,
     * public or protected constructor." Please use the contructor that takes
     * arguments.
     */
    @Deprecated
    public PasswordResetData() {
    }

    public PasswordResetData(DataverseUser dataverseUser) {
        this.dataverseUser = dataverseUser;
        this.token = UUID.randomUUID().toString();
        long nowInMilliseconds = new Date().getTime();
        this.created = new Timestamp(nowInMilliseconds);
        /**
         * @todo decide when token should expire. 20 minutes from now? Make this
         * configurable?
         */
        int minutesUntilTokenExpires = 20;
        long ONE_MINUTE_IN_MILLISECONDS = 60000;
        long futureInMilliseconds = nowInMilliseconds + (minutesUntilTokenExpires * ONE_MINUTE_IN_MILLISECONDS);
        this.expires = new Timestamp(new Date(futureInMilliseconds).getTime());

    }

    public String getToken() {
        return token;
    }

    public DataverseUser getDataverseUser() {
        return dataverseUser;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getExpires() {
        return expires;
    }

}
