/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.studio.importer.bar.custom.migration.connector.mapper;

import org.bonitasoft.studio.connectors.extension.AbstractConnectorDefinitionMapper;

public class TalendTISJobLauncherConnectorMapper extends AbstractConnectorDefinitionMapper {

	private static final String TALEND_TIS_JOB_LAUNCHER_ID ="talend-job-ws"; 
	private static final String LEGACY_TALEND_TIS_JOB_LAUNCHER_ID="TISJobLauncher";
	@Override
	public String getDefinitionId() {	
		return TALEND_TIS_JOB_LAUNCHER_ID;
	}

	@Override
	public String getLegacyConnectorId() {
		return LEGACY_TALEND_TIS_JOB_LAUNCHER_ID;
	}
		
}
