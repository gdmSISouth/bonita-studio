/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.studio.application.views;

import org.bonitasoft.studio.common.gmf.tools.tree.BonitaFormTreeViewer;
import org.bonitasoft.studio.common.gmf.tools.tree.BonitaProcessTreeViewer;
import org.bonitasoft.studio.common.gmf.tools.tree.BonitaTreeViewer;
import org.bonitasoft.studio.common.preferences.CommonDiagramPreferencesConstants;
import org.bonitasoft.studio.model.process.diagram.form.part.FormDiagramEditor;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.diagram.core.util.ViewType;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.TreeContainerEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.TreeDiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.TreeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIPlugin;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.internal.l10n.DiagramUIPluginImages;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.draw2d.ui.internal.parts.ScrollableThumbnailEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.parts.ThumbnailEx;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Aurelie Zara
 *
 */
public class BonitaTreeOutlinePage extends ContentOutlinePage implements IAdaptable {

	private EditPartViewer viewer;
	private PageBook pageBook;
	private Control outline;
	private boolean overviewInitialized;
	private DisposeListener disposeListener;
	private DiagramEditor diagramEditor;
	private SelectionSynchronizer selectionSynchronizer;

	/**
	 * @param viewer
	 * @param diagramEditor 
	 */
	public BonitaTreeOutlinePage(EditPartViewer viewer, DiagramEditor diagramEditor) {
		super(viewer);
		this.diagramEditor = diagramEditor;
	}

	/**
	 * configures the outline viewer
	 */
	protected void configureOutlineViewer() {
		getViewer().setEditPartFactory(getOutlineViewEditPartFactory());
		getViewer().setKeyHandler(getKeyHandler());
		pageBook.showPage(outline);
	}


	/**
	 * @return
	 */
	private EditPartFactory getOutlineViewEditPartFactory() {
		return new EditPartFactory() {

			public EditPart createEditPart(EditPart context, Object model) {
				if (model instanceof Diagram) {
					return new TreeDiagramEditPart(model);
                } else if (model instanceof View
                    && ViewType.GROUP.equals(((View) model).getType())) {
                    return new TreeContainerEditPart(model);
				} else {
					return new TreeEditPart(model);
				}
			}
		};
	}
	

	/**
	 * @return
	 */
	private KeyHandler getKeyHandler() {
		return new KeyHandler();
	}

	public void createControl(Composite parent) {
		pageBook = new PageBook(parent, SWT.NONE);
		outline = getViewer().createControl(pageBook);
		pageBook.showPage(outline);
		configureOutlineViewer();
		hookOutlineViewer();
		initializeOutlineViewer();
	}

	public void dispose() {
		unhookOutlineViewer();
		this.overviewInitialized = false;
		super.dispose();
	}

	public Object getAdapter(Class type) {
		return null;
	}

	public Control getControl() {
		return pageBook;
	}

	/**
	 * hook the outline viewer
	 */
	protected void hookOutlineViewer() {
		getSelectionSynchronizer().addViewer(getViewer());
	}

	/**
	 * @return
	 */
	private SelectionSynchronizer getSelectionSynchronizer() {
		if(selectionSynchronizer == null){
			selectionSynchronizer = new SelectionSynchronizer();
		}
		return selectionSynchronizer;
	}


	/**
	 * initialize the outline viewer
	 */
	protected void initializeOutlineViewer() {
		try {
			TransactionUtil.getEditingDomain(getDiagram()).runExclusive(
					new Runnable() {

						public void run() {
							getViewer().setContents(getDiagram());
						}
					});
		} catch (InterruptedException e) {
			Trace.catching(DiagramUIPlugin.getInstance(),
					DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
					"initializeOutlineViewer", e); //$NON-NLS-1$
			Log.error(DiagramUIPlugin.getInstance(),
					DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
					"initializeOutlineViewer", e); //$NON-NLS-1$
		}
		((BonitaTreeViewer) getViewer())
		.setDiagramEditPart(getDiagramEditPart());
	}

	/**
	 * @return
	 */
	private DiagramEditPart getDiagramEditPart() {
		return diagramEditor.getDiagramEditPart();
	}

	/**
	 * @return
	 */
	private EObject getDiagram() {
		return diagramEditor.getDiagram();
	}

	@Override
	protected EditPartViewer getViewer() {
		if (viewer == null) {
			if(diagramEditor instanceof ProcessDiagramEditor){
				viewer = new BonitaProcessTreeViewer();
			}else if(diagramEditor instanceof FormDiagramEditor){
				viewer = new BonitaFormTreeViewer();
			}
			
		}
		return viewer;
	}



	/**
	 * unhook the outline viewer
	 */
	protected void unhookOutlineViewer() {
		getSelectionSynchronizer().removeViewer(getViewer());
		if (disposeListener != null && getEditor() != null
				&& !getEditor().isDisposed())
			getEditor().removeDisposeListener(disposeListener);
	}

	/**
	 * getter for the editor conrolo
	 * 
	 * @return <code>Control</code>
	 */
	protected Control getEditor() {
		return getGraphicalViewer().getControl();
	}

	/**
	 * @return
	 */
	private AbstractEditPartViewer getGraphicalViewer() {
		return (AbstractEditPartViewer) diagramEditor.getDiagramGraphicalViewer();
	}

}