/*******************************************************************************
 * Copyright (c) 2013 aegif.
 * 
 * This file is part of NemakiWare.
 * 
 * NemakiWare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NemakiWare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public Licensealong with NemakiWare. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     linzhixing(https://github.com/linzhixing) - initial API and implementation
 ******************************************************************************/
package jp.aegif.nemaki.cincom.service.cmis.impl;

import jp.aegif.nemaki.cincom.model.constant.CincomPropertyKey;
import jp.aegif.nemaki.cincom.shared.CincomManager;
import jp.aegif.nemaki.service.cmis.AuthenticationService;
import jp.aegif.nemaki.util.NemakiPropertyManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cincom.kmdata.client.api.folder.Folder;
import com.cincom.kmdata.client.api.folder.InformationException_Exception;
import com.cincom.kmdata.client.api.folder.ItemNotFoundException_Exception;
import com.cincom.kmdata.client.api.folder.RepositoryException_Exception;

/**
 * Authentication Service implementation.
 */
public class CEAuthenticationServiceImpl implements AuthenticationService {

	private CincomManager cincomManager;
	private NemakiPropertyManager propertyManager;

	private static final Log log = LogFactory
			.getLog(CEAuthenticationServiceImpl.class);

	public CEAuthenticationServiceImpl(CincomManager cincomManager) {
		setCincomManager(cincomManager);
	}

	public boolean login(String username, String password) {
		cincomManager.setContext(username, password);
		setRootFolderUuid();

		String adminId = propertyManager
				.readValue(CincomPropertyKey.CINCOM_PRINCIPAL_ADMIN_ID);
		return adminId.equals(username);
	}

	public void logout() {
		cincomManager.logout();
	}

	private void setRootFolderUuid() {
		String rootFolderUuid = cincomManager.getRootFolderUuid();
		if (StringUtils.isBlank(rootFolderUuid)) {
			try {
				Folder root = cincomManager.getFolderWS().getFolder(
						cincomManager.getContext(), "/", null);
				rootFolderUuid = root.getUuid();
			} catch (InformationException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ItemNotFoundException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		cincomManager.setRootFolderUuid(rootFolderUuid);
	}

	public void setCincomManager(CincomManager cincomManager) {
		this.cincomManager = cincomManager;
	}

	public void setPropertyManager(NemakiPropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}
}
