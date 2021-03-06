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
package org.bonitasoft.studio.actors.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.studio.actors.i18n.Messages;
import org.bonitasoft.studio.actors.model.organization.Organization;
import org.bonitasoft.studio.actors.preference.ActorsPreferenceConstants;
import org.bonitasoft.studio.actors.ui.wizard.page.DefaultUserOrganizationWizardPage;
import org.bonitasoft.studio.actors.ui.wizard.page.SynchronizeOrganizationWizardPage;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.preferences.BonitaPreferenceConstants;
import org.bonitasoft.studio.preferences.BonitaStudioPreferencesPlugin;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * @author Romain Bioteau
 *
 */
public class SynchronizeOrganizationWizard extends Wizard {

    protected IRepositoryFileStore file;
    private SynchronizeOrganizationWizardPage page;
    private DefaultUserOrganizationWizardPage userPage;

    public SynchronizeOrganizationWizard(){
    	setWindowTitle(Messages.synchronizeOrganizationTitle);
        setDefaultPageImageDescriptor(Pics.getWizban()) ;
        setForcePreviousAndNextButtons(false) ;
        setNeedsProgressMonitor(true) ;
    }

    @Override
    public void addPages() {
        page = new SynchronizeOrganizationWizardPage() ;
        userPage = new DefaultUserOrganizationWizardPage() ;
        IPreferenceStore prefStore = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore() ;
        userPage.setUser(prefStore.getString(BonitaPreferenceConstants.USER_NAME));
        userPage.setPassword(prefStore.getString(BonitaPreferenceConstants.USER_PASSWORD));
        addPage(page) ;
        addPage(userPage) ;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if(page instanceof SynchronizeOrganizationWizardPage){
            userPage.setOrganization((Organization) ((SynchronizeOrganizationWizardPage)page).getFileStore().getContent()) ;
            return userPage ;
        }else{
            return super.getNextPage(page);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        try {
            getContainer().run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor maonitor) throws InvocationTargetException,InterruptedException {
                    maonitor.beginTask(Messages.synchronizingOrganization, IProgressMonitor.UNKNOWN) ;

                    IPreferenceStore prefStore = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore() ;
                    prefStore.setValue(BonitaPreferenceConstants.USER_NAME,userPage.getUser());
                    prefStore.setValue(BonitaPreferenceConstants.USER_PASSWORD,userPage.getPassword());

                    IRepositoryFileStore artifact = getFileStore() ;
                    ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class) ;
                    Command cmd = service.getCommand("org.bonitasoft.studio.engine.installOrganization") ;
                    Map<String, Object> parameters = new HashMap<String, Object>() ;
                    parameters.put("artifact", artifact.getName()) ;

                    ExecutionEvent ee = new ExecutionEvent(cmd, parameters, null, null) ;

                    try {
                        cmd.executeWithChecks(ee) ;
                        prefStore.setValue(ActorsPreferenceConstants.DEFAULT_ORGANIZATION, artifact.getDisplayName()) ;
                    } catch (Exception e) {
                        BonitaStudioLog.error(e) ;
                        return  ;
                    }
                    final String organizationName = artifact.getDisplayName();
                    Display.getDefault().syncExec( new Runnable() {
						public void run() {
							 MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.synchronizeInformationTitle,Messages.bind(Messages.synchronizeOrganizationSuccessMsg, organizationName));
						}
					});
                
                }
            }) ;
        } catch (Exception e) {
            BonitaStudioLog.error(e) ;
            return false ;
        }
        return true;
    }

    public IRepositoryFileStore getFileStore() {
        return page.getFileStore();
    }


}
