/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.actors.ui.wizard.page;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.actors.ActorsPlugin;
import org.bonitasoft.studio.actors.i18n.Messages;
import org.bonitasoft.studio.actors.model.organization.Group;
import org.bonitasoft.studio.actors.model.organization.Membership;
import org.bonitasoft.studio.actors.model.organization.Organization;
import org.bonitasoft.studio.actors.model.organization.Role;
import org.bonitasoft.studio.actors.model.organization.User;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * @author Romain Bioteau
 *
 */
public class OrganizationUserValidator implements IValidator {



    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
     */
    @Override
    public IStatus validate(Object input) {
        final Organization organization = (Organization) input;
        for(User u : organization.getUsers().getUser()){
            if(u.getUserName() == null || u.getUserName().isEmpty()){
                return ValidationStatus.error(Messages.userNameMissing);
            }
            if(u.getPassword() == null || u.getPassword().getValue() == null || u.getPassword().getValue().isEmpty()){
                return ValidationStatus.error(Messages.bind(Messages.userPasswordMissing,u.getUserName()));
            }
            if(u.getFirstName() == null || u.getFirstName().isEmpty()){
                return ValidationStatus.error(Messages.bind(Messages.userFirstNameMissing,u.getUserName()));
            }
            if(u.getLastName() == null || u.getLastName().isEmpty()){
                return ValidationStatus.error(Messages.bind(Messages.userLastNameMissing,u.getUserName()));
            }

            if(u.getManager() != null && !u.getManager().isEmpty()){
                IStatus status = checkManagerCycles(organization,u);
                if(!status.isOK()){
                    return status;
                }
            }

            boolean membershipFound = false;
            for(Membership membership : organization.getMemberships().getMembership()){
                final String userName = membership.getUserName() ;
                if(userName != null){
                    if(userName.equals(u.getUserName())){
                        membershipFound = true;
                        final String groupName = membership.getGroupName() ;
                        if(groupName == null){
                            return ValidationStatus.error(Messages.bind(Messages.missingGroup,u.getUserName()));
                        }
                        final String parentPath = membership.getGroupParentPath() ;
                        String groupPath = null ;
                        if(parentPath == null){
                            groupPath = GroupContentProvider.GROUP_SEPARATOR + groupName ;
                        }else{
                            groupPath = parentPath + GroupContentProvider.GROUP_SEPARATOR + groupName ;
                        }
                        IStatus groupStatus = validateGroupExists(organization,groupPath,membership) ;
                        if(groupStatus.getSeverity() != IStatus.OK){
                            return groupStatus ;
                        }

                        final String roleName = membership.getRoleName() ;
                        if(roleName == null){
                            return ValidationStatus.error(Messages.bind(Messages.missingRole,u.getUserName()));
                        }
                        IStatus roleStatus = validateRoleExists(organization,roleName,membership) ;
                        if(roleStatus.getSeverity() != IStatus.OK){
                            return roleStatus ;
                        }
                    }
                }
            }
            if(!membershipFound){
                return ValidationStatus.error(Messages.bind(Messages.missingMembershipForUser,u.getUserName()));
            }
        }
        return ValidationStatus.ok() ;
    }

    private IStatus checkManagerCycles(Organization organization,User u) {
        String managerUsername = u.getManager();
        List<String> managers = new ArrayList<String>();
        managers.add(u.getUserName());
        managers.add(managerUsername);
        while (managerUsername != null ) {
            managerUsername = getManagerOf(organization,managerUsername);
            if(managerUsername != null){
                if(!managers.contains(managerUsername)){
                    managers.add(managerUsername);
                }else{
                    managers.add(managerUsername);
                    return new Status(IStatus.ERROR, ActorsPlugin.PLUGIN_ID, Messages.bind(Messages.managerCycleDetected,managers.toString()));
                }
            }
        }

        return ValidationStatus.ok();
    }

    private String getManagerOf(Organization organization,String managerUsername) {
        for(User u : organization.getUsers().getUser()){
            if(managerUsername.equals(u.getUserName())){
                return u.getManager();
            }
        }
        return null;
    }

    private IStatus validateRoleExists(Organization organization,String roleName, Membership membership) {
        for(Role role : organization.getRoles().getRole()){
            if(role.getName().equals(roleName)){
                return ValidationStatus.ok() ;
            }
        }
        return ValidationStatus.error(Messages.bind(Messages.missingRoleInMembership,roleName,membership.getUserName()));
    }

    private IStatus validateGroupExists(Organization organization,String groupPath, Membership membership) {
        for(Group group : organization.getGroups().getGroup()){
            if(GroupContentProvider.getGroupPath(group).equals(groupPath)){
                return ValidationStatus.ok() ;
            }
        }
        return ValidationStatus.error(Messages.bind(Messages.missingGroupInMembership,groupPath,membership.getUserName()));
    }

}
