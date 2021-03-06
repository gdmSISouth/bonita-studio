/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.sections.lookandfeel;

import org.bonitasoft.studio.model.process.AbstractProcess;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

/**
 * @author Baptiste Mesta
 *
 */
public class LookAndFeelPropertySectionListener {

	/**
	 * @return the process
	 */
	public AbstractProcess getProcess() {
		return process;
	}

	/**
	 * @return the editDomain
	 */
	public TransactionalEditingDomain getEditDomain() {
		return editDomain;
	}

	private AbstractProcess process;
	private TransactionalEditingDomain editDomain;

	/**
	 * 
	 */
	public LookAndFeelPropertySectionListener() {
		super();
	}

	/**
	 * @param abstractProcess
	 */
	public void setProcess(AbstractProcess abstractProcess) {
		this.process = abstractProcess;
	}

	/**
	 * @param editingDomain
	 */
	public void setEditDomain(TransactionalEditingDomain editingDomain) {
		this.editDomain = editingDomain;
	}

}