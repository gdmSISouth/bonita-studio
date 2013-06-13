/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.studio.actors.ui.handler;

import org.bonitasoft.studio.actors.ui.wizard.FilterImplementationWizard;
import org.bonitasoft.studio.actors.ui.wizard.SelectUserFilterImplementationWizard;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.bonitasoft.studio.actors.i18n.Messages;
import org.bonitasoft.studio.common.jface.CustomWizardDialog;

/**
 * @author Romain Bioteau
 *
 */
public class EditFilterImplementationHandler extends AbstractHandler {



    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        SelectUserFilterImplementationWizard selectWizard = new SelectUserFilterImplementationWizard() ;
        WizardDialog selectDialog = new CustomWizardDialog(Display.getCurrent().getActiveShell(),selectWizard,Messages.edit) ;
        if(selectDialog.open() == Dialog.OK){
            FilterImplementationWizard wizard = new FilterImplementationWizard(selectWizard.getConnectorImplementation()) ;
            WizardDialog wd = new WizardDialog(Display.getCurrent().getActiveShell(),wizard) ;
            wd.open();
        }

        return null ;
    }



}