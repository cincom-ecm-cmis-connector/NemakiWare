package jp.aegif.nemaki.cincom;

import com.cincom.kmdata.client.api.folder.AccessDeniedException_Exception;
import com.cincom.kmdata.client.api.folder.InformationException_Exception;
import com.cincom.kmdata.client.api.folder.ItemExistsException_Exception;
import com.cincom.kmdata.client.api.folder.ItemNotFoundException_Exception;
import com.cincom.kmdata.client.api.folder.LockException_Exception;
import com.cincom.kmdata.client.api.folder.RepositoryException_Exception;
import com.cincom.kmdata.client.api.folder.WorkflowException_Exception;

import jp.aegif.nemaki.cincom.shared.CincomManager;

public class TestUtil {
	public static com.cincom.kmdata.client.api.folder.Folder createFolder(CincomManager manager, String parentPath, String name, String propertyGroup) {
		com.cincom.kmdata.client.api.folder.Folder ceFolder = new com.cincom.kmdata.client.api.folder.Folder();
		ceFolder.setName(name);
		ceFolder.setPath(parentPath + "/" + name);
		ceFolder.setPropertyGroup(propertyGroup);
		
		
		try {
			com.cincom.kmdata.client.api.folder.Folder result = manager.getFolderWS().create(manager.getContext(), ceFolder);
			return result;
		} catch (AccessDeniedException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InformationException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemExistsException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemNotFoundException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WorkflowException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void deleteFolder(CincomManager manager, String path){
		try {
			manager.getFolderWS().delete(manager.getContext(), path);
		} catch (AccessDeniedException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InformationException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemNotFoundException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
