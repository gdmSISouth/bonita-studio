/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.dependencies.ui.dialog;

import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.common.repository.provider.FileStoreLabelProvider;
import org.bonitasoft.studio.dependencies.handler.AddJarsHandler;
import org.bonitasoft.studio.dependencies.i18n.Messages;
import org.bonitasoft.studio.dependencies.repository.DependencyRepositoryStore;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Aurelien Pupier
 *
 */
public class ManageJarDialog extends Dialog {

    protected TableViewer tableViewer;
    private Button removeButton;
    private final IRepositoryStore<?> libStore;

    protected DataBindingContext context;
    private ViewerFilter searchFilter;

    public ManageJarDialog(Shell parentShell) {
        super(parentShell);
        libStore = RepositoryManager.getInstance().getRepositoryStore(DependencyRepositoryStore.class) ;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.manageJarTitle) ;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        context = new DataBindingContext() ;
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(15, 15).create());
        createTree(composite);
        createRightButtonPanel(composite);
        return composite;
    }

    protected void createRightButtonPanel(Composite composite) {
        Composite rightPanel = new Composite(composite, composite.getStyle());
        final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.fill =true;
		rightPanel.setLayout(rowLayout);
        addImportJarButton(rightPanel);
        addRemoveJarButton(rightPanel);
    }

    protected void addRemoveJarButton(Composite rightPanel) {
        removeButton = new Button(rightPanel, SWT.FLAT);
        removeButton.setText(Messages.removeJar);
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                IRunnableWithProgress runnable = new IRunnableWithProgress(){

                    @Override
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        monitor.beginTask(Messages.beginToRemoveJars, selection.size());
                        for(Object selected :selection.toList()){
                            if (monitor.isCanceled()) {
                                return;
                            }
                            if(selected instanceof IRepositoryFileStore){
                                try {
                                    monitor.worked(1);
                                    monitor.setTaskName(Messages.removingJar + " : "+ ((IRepositoryFileStore) selected).getName());
                                    ((IRepositoryFileStore) selected).delete() ;
                                } catch (Exception e1) {
                                    BonitaStudioLog.error(e1);
                                }
                            }
                        }
                    }

                };

                try {
                    new ProgressMonitorDialog(getShell()).run(true, true, runnable);
                } catch (InvocationTargetException e1) {
                    BonitaStudioLog.error(e1);
                } catch (InterruptedException e2) {
                    BonitaStudioLog.error(e2);
                }
                /*Due to some file handle issue and lock on windows we need to warn user if the jars are not correctly deleted*/
                if(Platform.getOS().equals(Platform.OS_WIN32)){
                    boolean shouldRestart = false;
                    for(Object selected :selection.toList()){
                        if(selected instanceof IRepositoryFileStore){
                            if(((IRepositoryFileStore) selected).getResource().exists()){
                                shouldRestart = true;
                                break;
                            }
                        }
                    }

                    if(shouldRestart){
                        MessageDialog.openInformation(getShell(), Messages.informationDeleteJarTitle, Messages.informationDeleteJarMessage);
                    }
                }
                /*Refresh viewer*/
                tableViewer.setInput(libStore.getChildren());
                tableViewer.refresh(true);

            }
        });

    }

    protected void addImportJarButton(Composite rightPanel) {
        Button addButton = new Button(rightPanel, SWT.FLAT);
        addButton.setText(Messages.importJar);
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    new AddJarsHandler().execute(null);
                } catch (ExecutionException e1) {
                    BonitaStudioLog.error(e1);
                }
                /*Refresh viewer*/
                tableViewer.setInput(libStore.getChildren()) ;
            }
        });
    }

    protected void createTree(Composite composite) {
        final Text searchText = new Text(composite,SWT.SEARCH | SWT.ICON_SEARCH | SWT.BORDER | SWT.CANCEL) ;
        searchText.setMessage(Messages.search) ;
        searchText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;

        searchFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer arg0, Object arg1, Object element) {
                if(!searchText.getText().isEmpty()){
                    String searchQuery = searchText.getText().toLowerCase() ;
                    IRepositoryFileStore file = (IRepositoryFileStore) element ;
                    return file.getName().toLowerCase().contains(searchQuery) ;
                }
                return true;
            }
        };

        new Label(composite,SWT.NONE) ; //FILLER

        tableViewer = new TableViewer(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(300, 300).create());
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.addFilter(searchFilter)  ;
        tableViewer.setLabelProvider(new FileStoreLabelProvider());
        tableViewer.setInput(libStore.getChildren());

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                if(removeButton != null){
                    removeButton.setEnabled(!tableViewer.getSelection().isEmpty());
                }
            }
        });

        searchText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                tableViewer.refresh() ;
            }
        }) ;

    }

    @Override
    public boolean close() {
        boolean returnValue = super.close();
        if (returnValue) {
            tableViewer.getTable().dispose();
        }
        if(context != null){
            context.dispose() ;
        }
        return returnValue;

    }


}
