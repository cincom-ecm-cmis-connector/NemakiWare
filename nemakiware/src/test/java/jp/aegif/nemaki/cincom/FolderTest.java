package jp.aegif.nemaki.cincom;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.aegif.nemaki.cincom.service.node.impl.CEContentServiceImpl;
import jp.aegif.nemaki.cincom.shared.CincomManager;
import jp.aegif.nemaki.model.Folder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.cincom.kmdata.client.api.folder.AccessDeniedException_Exception;
import com.cincom.kmdata.client.api.folder.InformationException_Exception;
import com.cincom.kmdata.client.api.folder.ItemExistsException_Exception;
import com.cincom.kmdata.client.api.folder.ItemNotFoundException_Exception;
import com.cincom.kmdata.client.api.folder.RepositoryException_Exception;
import com.cincom.kmdata.client.api.folder.WorkflowException_Exception;

public class FolderTest {

	private static CEContentServiceImpl contentService;
	private static com.cincom.kmdata.client.api.folder.Folder testFolder;
	private static com.cincom.kmdata.client.api.folder.Folder rawObject;
	private static Folder convertedFolder;

	
	@ClassRule public static CETestRule ceTestRule = new CETestRule();
	
	@BeforeClass
	public static void doBefore() {
		//TODO logging
		
		CincomManager mgr = ceTestRule.getMgr();
		contentService = ceTestRule.getContentService();
		testFolder = ceTestRule.getTestFolder();
		
		
		// Create a test object
		rawObject = TestUtil.createFolder(mgr, testFolder.getPath(), "test_01", "dc_folder");
		
		
		//Convert
		try {
			Method method = CEContentServiceImpl.class.getDeclaredMethod(
					"convertFolder",
					com.cincom.kmdata.client.api.folder.Folder.class);
			method.setAccessible(true);
			convertedFolder = (Folder) method.invoke(contentService, rawObject);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void folderNameIsConvertedCorrectly() throws Exception {
		assertEquals(convertedFolder.getId(), "folder_" + rawObject.getUuid());
	}
	
	@Test
	public void folderObjectTypeIsConvertedCorrectly() throws Exception {
		//TODO depends on mapping flag
		
		assertEquals(convertedFolder.getObjectType(), "cincom:" + rawObject.getPropertyGroup());
	}
	
	
	@AfterClass
	public static void doAfter() {
		//TODO logging
	}
	
}
