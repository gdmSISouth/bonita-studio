/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.studio.diagram.form.custom.perspective;

import org.bonitasoft.studio.model.process.diagram.form.part.FormDiagramEditor;
import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

public class PerspectiveMigrationFormsFactory extends PerspectiveFormsFactory {

	private static String MIGRATION_FORM_PERSPECTIVE_ID= "org.bonitasoft.studio.common.perspective.form.migration";
	
	@Override
	protected void createLeftViewFolder(IPageLayout layout, String editorArea) {
		IFolderLayout left = layout.createFolder(
				"left",
				IPageLayout.LEFT,
				(float) 0.3,
				editorArea);
		left.addView("org.bonitasoft.studio.migration.view");
		left.addView(PaletteView.ID);	
	}
	
	@Override
	public boolean isRelevantFor(IEditorPart part) {
		if(part instanceof FormDiagramEditor){
			return isOngoingMigration((FormDiagramEditor)part);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.bonitasoft.studio.common.perspectives.AbstractPerspectiveFactory#getID()
	 */
	@Override
	public String getID() {
		return MIGRATION_FORM_PERSPECTIVE_ID;
	}
	
	
}
