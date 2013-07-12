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
package org.bonitasoft.studio.groovy.ui.providers;

import org.bonitasoft.studio.common.jface.databinding.validator.InputLengthValidator;
import org.bonitasoft.studio.expression.editor.provider.IExpressionEditor;
import org.bonitasoft.studio.groovy.GroovyUtil;
import org.bonitasoft.studio.groovy.ui.Messages;
import org.bonitasoft.studio.groovy.ui.viewer.GroovyViewer;
import org.bonitasoft.studio.groovy.ui.wizard.ProcessVariableContentProvider;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * @author Romain Bioteau
 *
 */
public class GroovyScriptFileEditor extends GroovyScriptExpressionEditor implements IExpressionEditor {


    public GroovyScriptFileEditor(){
        super();
    }
    /* (non-Javadoc)
     * @see org.bonitasoft.studio.expression.editor.provider.IExpressionEditor#createExpressionEditor(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createExpressionEditor(Composite parent) {
        createDataChooserArea(parent);
        mainComposite = new Composite(parent,SWT.NONE) ;
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 300).create()) ;
        mainComposite.setLayout(new FillLayout(SWT.VERTICAL)) ;
        createGroovyEditor(parent);
        return mainComposite;
    }


    @Override
    public boolean canFinish() {
        return true;
    }

    @Override
    public void bindExpression(EMFDataBindingContext dataBindingContext,final EObject context, Expression inputExpression,ViewerFilter[] filters) {
        this.inputExpression = inputExpression ;
        this.context = context ;

        IObservableValue contentModelObservable = EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__CONTENT) ;

        groovyViewer.getDocument().set(inputExpression.getContent()) ;
        groovyViewer.setContext(context,filters) ;
        nodes = groovyViewer.getFieldNodes() ;

        if (context == null && (nodes == null || nodes.isEmpty())) {
            dataCombo.add(Messages.noProcessVariableAvailable);
            dataCombo.getTableCombo().setText(Messages.noProcessVariableAvailable);
            dataCombo.getTableCombo().setEnabled(false);
        }else if(nodes != null){
            dataCombo.setInput(nodes);
            dataCombo.setSelection(new StructuredSelection(ProcessVariableContentProvider.SELECT_ENTRY));
        }else{
            dataCombo.setInput(groovyViewer.getFieldNodes());
            dataCombo.setSelection(new StructuredSelection(ProcessVariableContentProvider.SELECT_ENTRY));
        }

        bonitaDataCombo.setInput(GroovyUtil.getBonitaVariables(context,null));
        bonitaDataCombo.setSelection(new StructuredSelection(ProcessVariableContentProvider.SELECT_ENTRY));

        dataBindingContext.bindValue(SWTObservables.observeText(sourceViewer.getTextWidget(),SWT.Modify), contentModelObservable,new UpdateValueStrategy().setAfterGetValidator(new InputLengthValidator("", GroovyViewer.MAX_SCRIPT_LENGTH)), null) ;
        sourceViewer.addTextListener(new ITextListener() {
            @Override
            public void textChanged(TextEvent event) {
                sourceViewer.getTextWidget().notifyListeners(SWT.Modify, new Event()) ;
            }
        }) ;
    }

}