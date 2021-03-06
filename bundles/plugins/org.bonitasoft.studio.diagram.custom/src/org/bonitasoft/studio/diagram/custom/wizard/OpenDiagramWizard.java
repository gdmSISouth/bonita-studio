/**
 * Copyright (C) 2009-2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
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
package org.bonitasoft.studio.diagram.custom.wizard;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.diagram.custom.Messages;
import org.bonitasoft.studio.diagram.custom.repository.DiagramFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mickael Istria
 *
 */
public class OpenDiagramWizard extends Wizard implements IWizard {

    private OpenDiagramWizardPage page;

    /**
     * Default constructor to open process and not example.
     */
    public OpenDiagramWizard(){
        setNeedsProgressMonitor(true) ;
        setWindowTitle(Messages.openProcessWizardPage_title);
    }

    @Override
    public void addPages() {
        page = new OpenDiagramWizardPage();
        addPage(page);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {

        try {
            getContainer().run(false, false, new IRunnableWithProgress(){

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.openingDiagramProgressText, IProgressMonitor.UNKNOWN) ;
                    List<DiagramFileStore> files = page.getDiagrams();
                    List<DiagramFileStore> dirtyFiles = new ArrayList<DiagramFileStore>(0);

                    Map<DiagramFileStore, Boolean> filesToOpen= new HashMap<DiagramFileStore, Boolean>();
                    StringBuilder stringBuilder = new StringBuilder(files.size()==1?"":"\n");

                    // get dirtyEditor and list of processes related to them
                    for (DiagramFileStore file : files) {
                        filesToOpen.put(file, true);
                        for(IEditorPart editor : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getDirtyEditors()){
                            if(editor.getEditorInput().getName().equals(file.getName())){
                                dirtyFiles.add(file);
                                stringBuilder.append(file.getName());
                                stringBuilder.append("\n");
                                break;
                            }
                        }
                    }

                    // case of dirty diagrams
                    if (!dirtyFiles.isEmpty() && !MessageDialog.openQuestion(page.getShell(), Messages.confirmProcessOverrideTitle,
                            NLS.bind(Messages.confirmProcessOverrideMessage,stringBuilder.toString()))) {

                        for(DiagramFileStore file : dirtyFiles){
                            filesToOpen.put(file, false);
                        }
                    }

                    // Open closed, already open, not dirty diagrams, for dirty ones, depending on openQuestion called before
                    for(DiagramFileStore file : files){
                        if(filesToOpen.get(file)){
                            file.open() ;
                        }
                    }

                    monitor.done();
                }

            });
        } catch (Exception e) {
            BonitaStudioLog.error(e) ;
        }


        return true;
    }



}
