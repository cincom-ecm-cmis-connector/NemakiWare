package jp.aegif.nemaki.cincom;

import jp.aegif.nemaki.cincom.service.cmis.impl.CEAuthenticationServiceImpl;
import jp.aegif.nemaki.cincom.service.node.impl.CEContentServiceImpl;
import jp.aegif.nemaki.cincom.util.CincomManager;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CETestRule implements TestRule {
	private  ApplicationContext springContext;
	private  CincomManager mgr;
	private  CEAuthenticationServiceImpl authService;
	private  CEContentServiceImpl contentService;
	private  com.cincom.kmdata.client.api.folder.Folder testFolder;
	
	
	private final String currentUser = "admin";
	private final String password = "admin";
	

	@Override
	public Statement apply(final Statement statement, Description description) {
		return new Statement() {
			public void evaluate() {
				// Here is BEFORE_CODE

				doBefore();

				try {
					statement.evaluate();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					// Here is AFTER_CODE
					doAfter();

				}
			}
		};
	}

	void doBefore() {
		// Prepare the connection to CincomECM
		springContext = new ClassPathXmlApplicationContext(
				"classpath:cincomContext-test.xml");

		// Get spring service bean
		authService = (CEAuthenticationServiceImpl) springContext
				.getBean("authenticationService");
		mgr = (CincomManager) springContext.getBean("CincomManager");
		authService.login(currentUser, password);
		contentService = (CEContentServiceImpl) springContext
				.getBean("contentService");

		// Create a test folder
		String testFolderName = "nemakiTest_" + System.currentTimeMillis();
		testFolder = TestUtil.createFolder(mgr, "",
				testFolderName, "dc_folder");
	}

	void doAfter() {
		TestUtil.deleteFolder(mgr, testFolder.getPath());
		authService.logout();
	}

	public  ApplicationContext getSpringContext() {
		return springContext;
	}

	public  CincomManager getMgr() {
		return mgr;
	}

	public  CEAuthenticationServiceImpl getAuthService() {
		return authService;
	}

	public  CEContentServiceImpl getContentService() {
		return contentService;
	}

	public  com.cincom.kmdata.client.api.folder.Folder getTestFolder() {
		return testFolder;
	}

}