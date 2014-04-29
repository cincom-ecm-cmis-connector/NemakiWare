package jp.aegif.nemaki.cincom.shared;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import jp.aegif.nemaki.cincom.model.constant.CincomPropertyKey;
import jp.aegif.nemaki.util.NemakiPropertyManager;

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cincom.kmdata.client.api.authentication.AccessDeniedException_Exception;
import com.cincom.kmdata.client.api.authentication.AuthenticationWS;
import com.cincom.kmdata.client.api.authentication.CDAuthenticationService;
import com.cincom.kmdata.client.api.authentication.LicenceException_Exception;
import com.cincom.kmdata.client.api.authentication.ProductException_Exception;
import com.cincom.kmdata.client.api.authentication.RepositoryException_Exception;
import com.cincom.kmdata.client.api.document.CDDocumentService;
import com.cincom.kmdata.client.api.document.DocumentWS;
import com.cincom.kmdata.client.api.folder.CDFolderService;
import com.cincom.kmdata.client.api.folder.FolderWS;
import com.cincom.kmdata.client.api.property.CDPropertyService;
import com.cincom.kmdata.client.api.property.PropertyWS;
import com.cincom.kmdata.client.api.relation.CDRelationService;
import com.cincom.kmdata.client.api.relation.RelationWS;
import com.cincom.kmdata.client.api.search.CDSearchService;
import com.cincom.kmdata.client.api.search.SearchWS;
import com.cincom.kmdata.client.api.server.CDServerService;
import com.cincom.kmdata.client.api.server.ServerWS;
import com.cincom.kmdata.client.api.statistic.CDStatisticService;
import com.cincom.kmdata.client.api.statistic.StatisticWS;
import com.cincom.kmdata.client.api.user.CDUserService;
import com.cincom.kmdata.client.api.user.UserWS;

public class CincomManager {
	private NemakiPropertyManager propertyManager;

	private String currentUser;
	private String rootFolderUuid;
	private AuthenticationWS authenticationWS;
	private ServerWS serverWS;
	private FolderWS folderWS;
	private DocumentWS documentWS;
	private StatisticWS statisticWS;
	private PropertyWS propertyWS;
	private SearchWS searchWS;
	private UserWS userWS;
	private RelationWS relationWS;

	private Map<String, String> contexts = new HashMap<String, String>();

	private static final Log log = LogFactory.getLog(CincomManager.class);

	private CincomManager(NemakiPropertyManager propertymanager) {
		setPropertyManager(propertymanager);

		String server = propertyManager
				.readValue(CincomPropertyKey.CINCOM_SERVER_URL);

		try {
			// Set up WebServices
			CDAuthenticationService cdAuthenticationService = new CDAuthenticationService(
					new URL(server + "/AuthenticationWS?wsdl"),
					new QName(
							"http://authentication.api.client.kmdata.cincom.com",
							"CDAuthenticationService"));
			authenticationWS = cdAuthenticationService
					.getAuthenticationWSPort();

			CDServerService cdServerService = new CDServerService(new URL(
					server + "/ServerWS?wsdl"), new QName(
					"http://server.api.client.kmdata.cincom.com",
					"CDServerService"));
			serverWS = cdServerService.getServerWSPort();

			CDFolderService cdFolderService = new CDFolderService(new URL(
					server + "/FolderWS?wsdl"), new QName(
					"http://folder.api.client.kmdata.cincom.com",
					"CDFolderService"));
			folderWS = cdFolderService.getFolderWSPort();

			CDDocumentService cdDocumentService = new CDDocumentService(
					new URL(server + "/DocumentWS?wsdl"), new QName(
							"http://document.api.client.kmdata.cincom.com",
							"CDDocumentService"));
			documentWS = cdDocumentService.getDocumentWSPort();

			CDStatisticService cdStatisticService = new CDStatisticService(
					new URL(server + "/StatisticWS?wsdl"), new QName(
							"http://statistic.api.client.kmdata.cincom.com",
							"CDStatisticService"));
			statisticWS = cdStatisticService.getStatisticWSPort();

			CDPropertyService cdPropertyService = new CDPropertyService(
					new URL(server + "/PropertyWS?wsdl"), new QName(
							"http://property.api.client.kmdata.cincom.com",
							"CDPropertyService"));
			propertyWS = cdPropertyService.getPropertyWSPort();

			CDSearchService cdSearchService = new CDSearchService(new URL(
					server + "/SearchWS?wsdl"), new QName(
					"http://search.api.client.kmdata.cincom.com",
					"CDSearchService"));
			searchWS = cdSearchService.getSearchWSPort();

			CDUserService cdUserService = new CDUserService(new URL(server
					+ "/UserWS?wsdl"),
					new QName("http://user.api.client.kmdata.cincom.com",
							"CDUserService"));
			userWS = cdUserService.getUserWSPort();

			CDRelationService cdRelationService = new CDRelationService(
					new URL(server + "/RelationWS?wsdl"), new QName(
							"http://relation.api.client.kmdata.cincom.com",
							"CDRelationService"));
			relationWS = cdRelationService.getRelationWSPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getCurrentUser() {
		return currentUser;
	}

	private void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public String getRootFolderUuid() {
		return rootFolderUuid;
	}

	public void setRootFolderUuid(String rootFolderUuid) {
		this.rootFolderUuid = rootFolderUuid;
	}

	public String getContext() {
		// TODO logging
		return contexts.get(currentUser);
	}

	public String getAdminContext() {
		// TODO externalize
		String id = propertyManager
				.readValue(CincomPropertyKey.CINCOM_PRINCIPAL_ADMIN_ID);
		String password = propertyManager
				.readValue(CincomPropertyKey.CINCOM_PRINCIPAL_ADMIN_PASSWORD);

		setCurrentUser(id);
		setContext(id, password);
		return contexts.get(id);
	}

	public void setContext(String userName, String password) {

		String context = contexts.get(userName);

		if (StringUtils.isBlank(context)) {
			context = login(userName, password);
		} else {
			try {
				boolean isLive = authenticationWS.isLive(context);
				if (!isLive) {
					context = login(userName, password);
				}
			} catch (RepositoryException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (StringUtils.isBlank(context)) {
			log.error("Failed to connect to Cincom ECM server! USER="
					+ userName);
			// TODO move this exception to Nemaki class
			throw new CmisPermissionDeniedException("Authentication failed");
		} else {
			// TODO refine
			contexts.put(userName, context);
		}
	}

	public String login(String userName, String password) {
		try {
			String context = authenticationWS.login(userName, password,
					"554934");
			setCurrentUser(userName);
			return context;
		} catch (AccessDeniedException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LicenceException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProductException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void logout() {
		try {
			String context = getContext();
			authenticationWS.logout(context);
		} catch (RepositoryException_Exception e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		contexts.remove(currentUser);
		setCurrentUser(null);
	}

	public String getLang() {
		String lang = propertyManager
				.readValue(CincomPropertyKey.CINCOM_LANG);
		if (StringUtils.isBlank(lang)) {
			lang = "en"; // default to English
		}

		return lang;
	}

	public void setPropertyManager(NemakiPropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}

	public AuthenticationWS getAuthenticationWS() {
		return authenticationWS;
	}

	public ServerWS getServerWS() {
		return serverWS;
	}

	public FolderWS getFolderWS() {
		return folderWS;
	}

	public DocumentWS getDocumentWS() {
		return documentWS;
	}

	public StatisticWS getStatisticWS() {
		return statisticWS;
	}

	public PropertyWS getPropertyWS() {
		return propertyWS;
	}

	public SearchWS getSearchWS() {
		return searchWS;
	}

	public UserWS getUserWS() {
		return userWS;
	}

	public RelationWS getRelationWS() {
		return relationWS;
	}
}