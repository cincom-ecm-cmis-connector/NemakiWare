package jp.aegif.nemaki.cincom;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import jp.aegif.nemaki.cincom.service.node.impl.CEContentServiceImpl;
import jp.aegif.nemaki.cincom.util.CincomManager;
import jp.aegif.nemaki.model.AttachmentNode;
import jp.aegif.nemaki.model.Document;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.cincom.kmdata.client.api.document.IOException_Exception;
import com.cincom.kmdata.client.api.document.VersionException_Exception;
import com.cincom.kmdata.client.api.folder.ResultSet;

public class DocumentTest {

	private static CEContentServiceImpl contentService;
	private static com.cincom.kmdata.client.api.folder.Folder testFolder;
	private static com.cincom.kmdata.client.api.document.Document rawDocument;
	private static Document convertedDocument;
	private static com.cincom.kmdata.client.api.folder.Document rawChild;
	private static Document convertedChild;

	@ClassRule
	public static CETestRule ceTestRule = new CETestRule();

	@BeforeClass
	public static void doBefore() throws Exception {
		CincomManager mgr = ceTestRule.getMgr();
		contentService = ceTestRule.getContentService();
		testFolder = ceTestRule.getTestFolder();

		// Specify a method(private)
		 Method convertDocument = CEContentServiceImpl.class.getDeclaredMethod(
				"convertDocument",
				com.cincom.kmdata.client.api.document.Document.class);
		convertDocument.setAccessible(true);
		
		 Method convertDocumentAsChild = CEContentServiceImpl.class.getDeclaredMethod(
					"convertDocument",
					com.cincom.kmdata.client.api.folder.Document.class);
		 convertDocumentAsChild.setAccessible(true);
		

		// Create a raw test object
		com.cincom.kmdata.client.api.document.Document ceDocument = new com.cincom.kmdata.client.api.document.Document();
		String name = "test_doc_01";
		ceDocument.setName(name);
		ceDocument.setPath(testFolder.getPath() + "/" + name);
		ceDocument.setPropertyGroup("dc_document");
		ceDocument.setMajorVersionId(1);
		ceDocument.setMinorVersionId(0);
		ceDocument.setFileName("file_01.txt");
		ceDocument.setMimeType("text/plain");
		String fileString = "this is a test.";
		ceDocument.setContent(fileString.getBytes(Charset.forName("UTF-8")));

		try {
			rawDocument = mgr.getDocumentWS()
					.create(mgr.getContext(), ceDocument);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.cincom.kmdata.client.api.document.ItemExistsException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VersionException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (com.cincom.kmdata.client.api.document.WorkflowException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		convertedDocument = (Document) convertDocument.invoke(contentService,
				rawDocument);
		
		ResultSet rs = mgr.getFolderWS().getChildren(mgr.getContext(), testFolder.getPath(), null);
		rawChild = (com.cincom.kmdata.client.api.folder.Document)rs.getResults().get(0).getObject();
		convertedChild = (Document) convertDocumentAsChild.invoke(contentService,
				rawChild);
		
	}

	@Test
	public void documentHasCorrectObjectId() throws Exception {
		String expectedObjecId = "document_" + rawDocument.getUuid() + "_"
				+ rawDocument.getMajorVersionId() + "."
				+ rawDocument.getMinorVersionId();

		// cmis:objctId
		assertEquals(expectedObjecId, convertedDocument.getId());
	}
	
	@Test
	public void childHasCorrectObjectId() throws Exception {
		String expectedObjecId = "document_" + rawChild.getUuid() + "_"
				+ rawChild.getMajorVersionId() + "."
				+ rawChild.getMinorVersionId();

		assertEquals(expectedObjecId, convertedDocument.getId());
	}
	
	@Test
	public void documentHasCorrectName() throws Exception {
		assertEquals(rawDocument.getName(), convertedDocument.getName());
	}
	
	@Test
	public void childHasCorrectName() throws Exception {
		assertEquals(rawChild.getName(), convertedChild.getName());
	}
	
	@Test
	public void documentHasCorrectVersion() throws Exception {
		String expected = rawDocument.getMajorVersionId() + "." + rawDocument.getMinorVersionId();
		assertEquals(expected, convertedDocument.getVersionLabel());
	}
	
	@Test
	public void childHasCorrectVersion() throws Exception {
		String expected = rawChild.getMajorVersionId() + "." + rawChild.getMinorVersionId();
		assertEquals(expected, convertedChild.getVersionLabel());
	}

	@Test
	public void documentHasCorrectFileName() throws Exception {
		AttachmentNode an = contentService.getAttachment(convertedDocument
				.getAttachmentNodeId());
		assertEquals(rawDocument.getFileName(), an.getName());
	}
	
	@Test
	public void childHasCorrectFileName() throws Exception {
		AttachmentNode an = contentService.getAttachment(convertedChild
				.getAttachmentNodeId());
		assertEquals(rawChild.getFileName(), an.getName());
	}

	@Test
	public void documentHasCorrectMimeType() throws Exception {
		AttachmentNode an = contentService.getAttachment(convertedDocument
				.getAttachmentNodeId());
		assertEquals(rawDocument.getMimeType(), an.getMimeType());
	}
	
	@Test
	public void childHasCorrectMimeType() throws Exception {
		AttachmentNode an = contentService.getAttachment(convertedChild
				.getAttachmentNodeId());
		assertEquals(rawChild.getMimeType(), an.getMimeType());
	}


	@Test
	public void documentHasCorrectBytes() throws Exception {
		AttachmentNode an = contentService.getAttachment(convertedDocument
				.getAttachmentNodeId());
		byte[] actualBytes = readAll(an.getInputStream());

		
		assertArrayEquals(rawDocument.getContent(), actualBytes);
	}
	
	@Test
	public void childHasCorrectBytes() throws Exception {
		AttachmentNode an = contentService.getAttachment(convertedChild
				.getAttachmentNodeId());
		byte[] actualBytes = readAll(an.getInputStream());

		
		assertArrayEquals(rawChild.getContent(), actualBytes);
	}

	@AfterClass
	public static void doAfter() {

	}

	private byte[] readAll(InputStream inputStream) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (true) {
			int len = inputStream.read(buffer);
			if (len < 0) {
				break;
			}
			bout.write(buffer, 0, len);
		}
		return bout.toByteArray();
	}
}
