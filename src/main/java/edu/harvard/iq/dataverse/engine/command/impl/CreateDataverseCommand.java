package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseRole;
import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.engine.Permission;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.IllegalCommandException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.EnumSet;
import javax.ejb.EJBException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

/**
 * TODO make override the date and user more active, so prevent code errors.
 * e.g. another command, with explicit parameters.
 * 
 * @author michael
 */
@RequiredPermissions( Permission.AddDataverse )
public class CreateDataverseCommand extends AbstractCommand<Dataverse> {
	
	private final Dataverse created;

	public CreateDataverseCommand(Dataverse created, DataverseUser aUser) {
		super(aUser, created.getOwner());
		this.created = created;
	}
	
	@Override
	public Dataverse execute(CommandContext ctxt) throws CommandException {
		
		if ( created.getOwner()==null ) {
			if ( ctxt.dataverses().isRootDataverseExists() ) {
				throw new CommandException("Root Dataverse already exists. Cannot create another one", this);
			}
		}
		
		if ( created.getCreateDate() == null )  {
			created.setCreateDate( new Timestamp(new Date().getTime()) );
		}
		
		if ( created.getCreator() == null ) {
			created.setCreator(getUser());
		}
		
		// Save the dataverse
        Dataverse managedDv = null;
            try {
                managedDv = ctxt.dataverses().save(created);
            } catch (EJBException ex) {
                Throwable cause = ex;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                    if (cause instanceof ConstraintViolationException) {
                        final String dataverseTypeProperty = "dataverseType";
                        ConstraintViolationException constraintViolationException = (ConstraintViolationException) cause;
                        for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
                            String errorMessageFromEntity = violation.getMessage();
                            Path propertyPath = violation.getPropertyPath();
                            if (propertyPath != null) {
                                String propertyPathString = propertyPath.toString();
                                if (propertyPathString != null) {
                                    if (propertyPathString.equals(dataverseTypeProperty)) {
                                        throw new IllegalCommandException(errorMessageFromEntity + " '" + dataverseTypeProperty + "' should be one of: " + Dataverse.getAllowedDataverseTypes(), this);
                                    }
                                }
                            }
                        }
                    }
                }
            }
		
		// Create the manager role and assign it to the creator. This can't be done using commands,
		// as no one is allowed to do anything on the newly created dataverse yet.
		// TODO this can be optimized out if the creating user has full permissions
		// on the parent dv, and the created dv is not a permission root.
		DataverseRole manager = new DataverseRole();
		manager.addPermissions( EnumSet.allOf(Permission.class) );
		
		manager.setAlias("manager");
		manager.setName("Dataverse Manager");
		manager.setDescription("Auto-generated role for the creator of this dataverse");
		manager.setOwner(managedDv);
		
		ctxt.roles().save(manager);
		
		ctxt.roles().save(new RoleAssignment(manager, getUser(), managedDv));
		
		ctxt.index().indexDataverse(managedDv);
		
		return managedDv;
	}
	
}
