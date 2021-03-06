/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.form.sections.actions.contributions;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.properties.ExtensibleGridPropertySection;
import org.bonitasoft.studio.common.properties.IExtensibleGridPropertySectionContribution;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
import org.bonitasoft.studio.form.properties.i18n.Messages;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.form.FileWidget;
import org.bonitasoft.studio.model.form.FileWidgetInputType;
import org.bonitasoft.studio.model.form.FormPackage;
import org.bonitasoft.studio.model.form.ViewForm;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.bonitasoft.studio.properties.sections.document.SelectDocumentInBonitaStudioRepository;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.databinding.edit.EMFEditObservables;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * 
 * Allow user to choose a File (fileData) for the file widget
 * 
 * @author Baptiste Mesta
 * 
 */
public class FileGridPropertySectionContribution implements IExtensibleGridPropertySectionContribution {

	private FileWidget element;
	private TransactionalEditingDomain editingDomain;
	private Button downloadOnly;
	private EMFDataBindingContext dataBindingContext;
	private Button imagePreview;
	private Section initialValueSection;
	private Button useDocumentButton;
	//private Button useURLButton;
	private Button useResourceButton;
	private TabbedPropertySheetWidgetFactory widgetFactory;
	private ExpressionViewer inputExpressionViewer;
	private Text resourceText;
	private TableViewer resourceTableViewer;
	private boolean multiple;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory,
	 * org.bonitasoft.studio.common.properties.ExtensibleGridPropertySection)
	 */
	public void createControl(Composite mainComposite, TabbedPropertySheetWidgetFactory widgetFactory,
			ExtensibleGridPropertySection extensibleGridPropertySection) {

		this.widgetFactory = widgetFactory ;
		mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		int col = 3 ;
		if(ModelHelper.isAnEntryPageFlowOnAPool(ModelHelper.getParentForm(element))){
			col = 2 ;
		}
		mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());

		downloadOnly = widgetFactory.createButton(mainComposite, Messages.downloadOnly, SWT.CHECK);
		imagePreview = widgetFactory.createButton(mainComposite, Messages.previewAttachment, SWT.CHECK);
		widgetFactory.createLabel(mainComposite, "");

		final Composite radioComposite = widgetFactory.createComposite(mainComposite) ;
		radioComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL,SWT.CENTER).grab(true, false).span(3, 1).create());
		radioComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(col).margins(0, 0).create()) ;

		FileWidgetInputType initialInputType = createUseDocumentButton(
				widgetFactory, radioComposite);

		//createURLButton(widgetFactory, radioComposite);

		createUseResourceButton(radioComposite);

		initialValueSection = widgetFactory.createSection(mainComposite, Section.NO_TITLE | Section.CLIENT_INDENT) ;
		initialValueSection.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).create());
		initialValueSection.setLayoutData(GridDataFactory.fillDefaults().grab(true,true).span(3, 1).create()) ;
		
		if(initialInputType == FileWidgetInputType.DOCUMENT){
			initialValueSection.setClient(createInputExpressionComposite(initialValueSection, widgetFactory));
		/*}else if(initialInputType == FileWidgetInputType.URL){
			initialValueSection.setClient(createInputExpressionComposite(initialValueSection, widgetFactory));*/
		}else if(initialInputType == FileWidgetInputType.RESOURCE){
			if(element.isDuplicate()){
				initialValueSection.setClient(createMultipleResourceComposite(initialValueSection, widgetFactory));
			}else{
				initialValueSection.setClient(createResourceComposite(initialValueSection, widgetFactory));
			}
		}

		if(initialInputType == FileWidgetInputType.DOCUMENT){
			useDocumentButton.setSelection(true);
			/*}else if(initialInputType == FileWidgetInputType.URL){
			useURLButton.setSelection(true);*/
		}else if(initialInputType == FileWidgetInputType.RESOURCE){
			useResourceButton.setSelection(true);
		}


		bindFields();
	}

	private void createUseResourceButton(Composite radioComposite) {
		useResourceButton = widgetFactory.createButton(radioComposite, Messages.useResource, SWT.RADIO) ;
		useResourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(initialValueSection != null && !initialValueSection.isDisposed()){
					if(useResourceButton.getSelection() && (element.getInputType() != FileWidgetInputType.RESOURCE || element.isDuplicate() != multiple || initialValueSection.getClient() == null)){
						editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, FormPackage.Literals.FILE_WIDGET__INPUT_TYPE, FileWidgetInputType.RESOURCE));
						if(initialValueSection.getClient() != null){
							initialValueSection.getClient().dispose() ;
						}
						if(element.isDuplicate()){
							multiple = true ;
							initialValueSection.setClient(createMultipleResourceComposite(initialValueSection, FileGridPropertySectionContribution.this.widgetFactory)) ;
						}else{
							multiple = false ;
							initialValueSection.setClient(createResourceComposite(initialValueSection, FileGridPropertySectionContribution.this.widgetFactory)) ;
						}

						initialValueSection.setExpanded(true) ;
						bindFields();
					}
				}
			}
		});
	}

	private FileWidgetInputType createUseDocumentButton(
			TabbedPropertySheetWidgetFactory widgetFactory,
			final Composite radioComposite) {
		FileWidgetInputType initialInputType = element.getInputType();
		if(!ModelHelper.isAnEntryPageFlowOnAPool(ModelHelper.getParentForm(element))){
			useDocumentButton = widgetFactory.createButton(radioComposite, Messages.useDocument, SWT.RADIO) ;
			useDocumentButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					if(initialValueSection != null && !initialValueSection.isDisposed()){
						if(useDocumentButton.getSelection() && (element.getInputType() != FileWidgetInputType.DOCUMENT || element.isDuplicate() != multiple || initialValueSection.getClient() == null)){
							boolean recreate = false;
							if(initialValueSection.getClient() == null || element.getInputType() == FileWidgetInputType.RESOURCE){
								recreate = true;
							}
							editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, FormPackage.Literals.FILE_WIDGET__INPUT_TYPE, FileWidgetInputType.DOCUMENT));
							if(recreate){
								if(initialValueSection.getClient() != null){
									initialValueSection.getClient().dispose() ;
								}
								multiple = element.isDuplicate() ;
								initialValueSection.setClient(createInputExpressionComposite(initialValueSection, FileGridPropertySectionContribution.this.widgetFactory)) ;


								initialValueSection.setExpanded(true) ;
								bindFields();
							}
						}
					}
				}
			});
		}else{
			initialInputType = FileWidgetInputType.URL;
		}
		return initialInputType;
	}

	private void createURLButton(
			TabbedPropertySheetWidgetFactory widgetFactory,
			final Composite radioComposite) {
//		useURLButton = widgetFactory.createButton(radioComposite, Messages.useUrl, SWT.RADIO) ;
//		useResourceButton = widgetFactory.createButton(radioComposite, Messages.useResource, SWT.RADIO) ;
//
//		useURLButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
//				if(initialValueSection != null && !initialValueSection.isDisposed()){
//					if(useURLButton.getSelection() && (element.getInputType() != FileWidgetInputType.URL || element.isDuplicate() != multiple || initialValueSection.getClient() == null)){
//						boolean recreate = false;
//						if(initialValueSection.getClient() == null || element.getInputType() == FileWidgetInputType.RESOURCE){
//							recreate = true;
//						}
//						editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, FormPackage.Literals.FILE_WIDGET__INPUT_TYPE, FileWidgetInputType.URL));
//						if(recreate){
//							if(initialValueSection.getClient() != null){
//								initialValueSection.getClient().dispose() ;
//							}
//							multiple = element.isDuplicate();
//							initialValueSection.setClient(createInputExpressionComposite(initialValueSection,FileGridPropertySectionContribution.this.widgetFactory)) ;
//							initialValueSection.setExpanded(true) ;
//							bindFields();
//						}
//					}
//				}
//			}
//		});
	}

	protected Control createResourceComposite(Section section, TabbedPropertySheetWidgetFactory widgetFactory) {
		final Composite client  = widgetFactory.createComposite(section);
		client.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		client.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).create());
		resourceText = widgetFactory.createText(client, "");
		resourceText.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create());

		final ControlDecoration cd = new ControlDecoration(resourceText, SWT.LEFT);
		cd.setImage(Pics.getImage(PicsConstants.hint));
		cd.setDescriptionText(Messages.filewidget_resource_hint);

		final Button browseResourceButton = widgetFactory.createButton(client, Messages.Browse, SWT.PUSH);
		browseResourceButton.setLayoutData(GridDataFactory.fillDefaults().create());
		browseResourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final SelectDocumentInBonitaStudioRepository selectDocumentInBonitaStudioRepository = new SelectDocumentInBonitaStudioRepository(Display.getDefault().getActiveShell());
				if(IDialogConstants.OK_ID == selectDocumentInBonitaStudioRepository.open()){
					resourceText.setText(selectDocumentInBonitaStudioRepository.getSelectedDocument().getName());
				}
			}
		});
		return client;
	}

	protected Control createMultipleResourceComposite(Section section, TabbedPropertySheetWidgetFactory widgetFactory) {
		final Composite client  = widgetFactory.createComposite(section);
		client.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		client.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).create());
		resourceTableViewer = new TableViewer(client, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		resourceTableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT,60).create());
		resourceTableViewer.setContentProvider(new ArrayContentProvider());
		resourceTableViewer.setLabelProvider(new LabelProvider());
		final ControlDecoration cd = new ControlDecoration(resourceTableViewer.getControl(), SWT.TOP | SWT.LEFT);
		cd.setImage(Pics.getImage(PicsConstants.hint));
		cd.setDescriptionText(Messages.filewidget_resource_hint_multiple);

		final Composite buttonComposite = widgetFactory.createComposite(client);
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
		buttonComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(0, 3).create());
		final Button addResourceButton = widgetFactory.createButton(buttonComposite, Messages.Add, SWT.PUSH);
		addResourceButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(85, SWT.DEFAULT).create());
		addResourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final SelectDocumentInBonitaStudioRepository selectDocumentInBonitaStudioRepository = new SelectDocumentInBonitaStudioRepository(Display.getDefault().getActiveShell());
				if(IDialogConstants.OK_ID == selectDocumentInBonitaStudioRepository.open()){
					editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, element, FormPackage.Literals.FILE_WIDGET__INTIAL_RESOURCE_LIST, selectDocumentInBonitaStudioRepository.getSelectedDocument().getName()));
					resourceTableViewer.refresh();
				}
			}
		});
		final Button removeResourceButton = widgetFactory.createButton(buttonComposite, Messages.Remove, SWT.PUSH);
		removeResourceButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(85, SWT.DEFAULT).create());
		removeResourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editingDomain.getCommandStack().execute(RemoveCommand.create(editingDomain, element, FormPackage.Literals.FILE_WIDGET__INTIAL_RESOURCE_LIST,((IStructuredSelection) resourceTableViewer.getSelection()).toList()));
				resourceTableViewer.refresh();
			}
		});

		resourceTableViewer.setInput(
				EMFEditProperties.list(
						editingDomain,
						FormPackage.Literals.FILE_WIDGET__INTIAL_RESOURCE_LIST)
						.observe(element));

		return client;
	}

	protected Control createInputExpressionComposite(Section section, TabbedPropertySheetWidgetFactory widgetFactory) {
		final Composite client  = widgetFactory.createComposite(section);
		client.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		client.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).create());

		inputExpressionViewer = new ExpressionViewer(client,SWT.BORDER,widgetFactory,editingDomain, FormPackage.Literals.WIDGET__INPUT_EXPRESSION) ;
		inputExpressionViewer.addFilter(new AvailableExpressionTypeFilter(new String[]{
				ExpressionConstants.VARIABLE_TYPE,
				ExpressionConstants.SCRIPT_TYPE,
				ExpressionConstants.CONSTANT_TYPE,
				ExpressionConstants.DOCUMENT_REF_TYPE,
				ExpressionConstants.PARAMETER_TYPE})) ;

		inputExpressionViewer.setMessage(getInputExpressionHint(),IStatus.INFO);
		inputExpressionViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;
		return client;
	}



	private String getInputExpressionHint() {
		if(element.isDuplicate()){
			return Messages.data_tooltip_url_multiple ;
		}else{
			return Messages.data_tooltip_url ;
		}

	}



	protected void bindFields() {
		if(dataBindingContext != null){
			dataBindingContext.dispose();
		}
		dataBindingContext = new EMFDataBindingContext();
		if(downloadOnly != null){
			if(ModelHelper.getForm(element) instanceof ViewForm){
				downloadOnly.setSelection(true);
				downloadOnly.setEnabled(false);
			}else{
				dataBindingContext.bindValue(
						SWTObservables.observeSelection(downloadOnly),
						EMFEditObservables.observeValue(
								editingDomain,
								element,
								FormPackage.Literals.FILE_WIDGET__DOWNLOAD_ONLY));
			}
		}

		dataBindingContext.bindValue(SWTObservables.observeSelection(imagePreview), EMFEditObservables.observeValue(editingDomain, element,
				FormPackage.Literals.FILE_WIDGET__USE_PREVIEW));

		IObservableValue value = EMFObservables.observeValue(element, FormPackage.Literals.DUPLICABLE__DUPLICATE);

		value.addValueChangeListener(new IValueChangeListener() {

			public void handleValueChange(ValueChangeEvent arg0) {
				if(useDocumentButton != null &&!useDocumentButton.isDisposed() && element.getInputType() == FileWidgetInputType.DOCUMENT){
					useDocumentButton.notifyListeners(SWT.Selection,new Event());
				/*}else if(!useResourceButton.isDisposed() && element.getInputType() == FileWidgetInputType.URL){
					useURLButton.notifyListeners(SWT.Selection,new Event());*/
				}else if(!useResourceButton.isDisposed() && element.getInputType() == FileWidgetInputType.RESOURCE){
					useResourceButton.notifyListeners(SWT.Selection,new Event());
				}
				if(inputExpressionViewer!=null && !getInputExpressionHint().equals(inputExpressionViewer.getMessage(IStatus.INFO))){
					inputExpressionViewer.setMessage(getInputExpressionHint(),IStatus.INFO);
				}

			}
		});


		bindInputExpressionViewer();
		bindResourceText();
	}

	protected void bindResourceText() {
		if(resourceText != null && element.getInputType() == FileWidgetInputType.RESOURCE && !resourceText.isDisposed()){
			dataBindingContext.bindValue(SWTObservables.observeText(resourceText, SWT.Modify), EMFEditObservables.observeValue(editingDomain, element, FormPackage.Literals.FILE_WIDGET__INITIAL_RESOURCE_PATH));
		}
	}


	protected void bindInputExpressionViewer() {
		if(element.getInputType() == FileWidgetInputType.URL || element.getInputType() == FileWidgetInputType.DOCUMENT){
			if(inputExpressionViewer != null  && !inputExpressionViewer.getControl().isDisposed()){
				Expression input = element.getInputExpression() ;
				if(input == null){
					input = ExpressionFactory.eINSTANCE.createExpression() ;
					editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, FormPackage.Literals.WIDGET__INPUT_EXPRESSION, input));
				}

				inputExpressionViewer.setEditingDomain(editingDomain) ;
				inputExpressionViewer.setInput(element) ;
				dataBindingContext.bindValue(
						ViewersObservables.observeSingleSelection(inputExpressionViewer),
						EMFEditObservables.observeValue(editingDomain,element, FormPackage.Literals.WIDGET__INPUT_EXPRESSION));
			
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution#getLabel()
	 */
	public String getLabel() {
		return Messages.Action_InitialValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution
	 * #isRelevantFor(org.eclipse.emf.ecore.EObject)
	 */
	public boolean isRelevantFor(EObject eObject) {
		return eObject instanceof FileWidget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution#refresh()
	 */
	public void refresh() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution
	 * #setEObject(org.eclipse.emf.ecore.EObject)
	 */
	public void setEObject(EObject object) {
		element = (FileWidget) object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution
	 * #setEditingDomain(org.eclipse.emf.transaction.TransactionalEditingDomain)
	 */
	public void setEditingDomain(TransactionalEditingDomain editingDomain) {
		this.editingDomain = editingDomain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution
	 * #setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		// NOTHING
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.bonitasoft.studio.common.properties.
	 * IExtensibleGridPropertySectionContribution#dispose()
	 */
	public void dispose() {
		if (dataBindingContext != null) {
			dataBindingContext.dispose();
		}
	}

}
