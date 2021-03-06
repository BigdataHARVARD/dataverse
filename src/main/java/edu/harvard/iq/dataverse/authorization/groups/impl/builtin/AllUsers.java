package edu.harvard.iq.dataverse.authorization.groups.impl.builtin;

import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.RoleAssigneeDisplayInfo;
import edu.harvard.iq.dataverse.authorization.groups.Group;
import edu.harvard.iq.dataverse.authorization.groups.GroupProvider;
import edu.harvard.iq.dataverse.authorization.users.User;

/**
 * A group containing all the users in the system - including the guest user.
 * So, basically, everyone.
 * 
 * <b>NOTE</b> this group is a singleton, as there's no point in having more than one. Get the instance
 * using {@link #get()}.
 * 
 * @author michael
 */
public final class AllUsers implements Group {

    public static final AllUsers instance = new AllUsers();
       
    private final String identifier = ":AllUsers";
    
    public static final AllUsers get() { return instance; }
    
    /**
     * Prevent instance creation
     */
    private AllUsers() {}
        
    @Override
    public boolean contains(RoleAssignee ra) {
        return (ra instanceof User);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public GroupProvider getGroupProvider() {
        return BuiltInGroupsProvider.get();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public RoleAssigneeDisplayInfo getDisplayInfo() {
        return new RoleAssigneeDisplayInfo("Everyone (including guests)", null);
    }

    @Override
    public String getAlias() {
        return getGroupProvider().getGroupProviderAlias() + Group.PATH_SEPARATOR + "all-users";
    }

    @Override
    public String getDisplayName() {
        return "All Users";
    }

    @Override
    public String getDescription() {
        return "All users, including guests";
    }

}
