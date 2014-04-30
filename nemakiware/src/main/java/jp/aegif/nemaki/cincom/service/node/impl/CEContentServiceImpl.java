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
 * You should have received a copy of the GNU General Public License along with NemakiWare.
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     linzhixing(https://github.com/linzhixing) - initial API and implementation
 ******************************************************************************/
package jp.aegif.nemaki.cincom.service.node.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.XMLGregorianCalendar;

import jp.aegif.nemaki.cincom.model.CmisId;
import jp.aegif.nemaki.cincom.model.CmisPropertyId;
import jp.aegif.nemaki.cincom.model.CmisTypeId;
import jp.aegif.nemaki.cincom.model.Document;
import jp.aegif.nemaki.cincom.model.NodeValueObject;
import jp.aegif.nemaki.cincom.model.constant.CincomConst;
import jp.aegif.nemaki.cincom.model.constant.CincomPropertyKey;
import jp.aegif.nemaki.cincom.model.constant.Permission;
import jp.aegif.nemaki.cincom.shared.CincomManager;
import jp.aegif.nemaki.model.Ace;
import jp.aegif.nemaki.model.Acl;
import jp.aegif.nemaki.model.Archive;
import jp.aegif.nemaki.model.Aspect;
import jp.aegif.nemaki.model.AttachmentNode;
import jp.aegif.nemaki.model.Change;
import jp.aegif.nemaki.model.Content;
import jp.aegif.nemaki.model.Folder;
import jp.aegif.nemaki.model.Item;
import jp.aegif.nemaki.model.Policy;
import jp.aegif.nemaki.model.Property;
import jp.aegif.nemaki.model.Relationship;
import jp.aegif.nemaki.model.Rendition;
import jp.aegif.nemaki.model.VersionSeries;
import jp.aegif.nemaki.util.constant.NemakiConstant;
import jp.aegif.nemaki.util.constant.NodeType;
import jp.aegif.nemaki.util.constant.RenditionKind;
import jp.aegif.nemaki.repository.type.TypeManager;
import jp.aegif.nemaki.service.node.ContentService;
import jp.aegif.nemaki.util.DataUtil;
import jp.aegif.nemaki.util.NemakiPropertyManager;
import net.java.dev.jaxb.array.StringArray;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cincom.kmdata.client.api.document.DocumentWS;
import com.cincom.kmdata.client.api.document.IOException_Exception;
import com.cincom.kmdata.client.api.document.LockInfo;
import com.cincom.kmdata.client.api.document.Version;
import com.cincom.kmdata.client.api.folder.AccessDeniedException_Exception;
import com.cincom.kmdata.client.api.folder.FolderWS;
import com.cincom.kmdata.client.api.folder.InformationException_Exception;
import com.cincom.kmdata.client.api.folder.ItemExistsException_Exception;
import com.cincom.kmdata.client.api.folder.ItemNotFoundException_Exception;
import com.cincom.kmdata.client.api.folder.LockException_Exception;
import com.cincom.kmdata.client.api.folder.RepositoryException_Exception;
import com.cincom.kmdata.client.api.folder.Result;
import com.cincom.kmdata.client.api.folder.ResultSet;
import com.cincom.kmdata.client.api.folder.VersionException_Exception;
import com.cincom.kmdata.client.api.folder.WorkflowException_Exception;
import com.cincom.kmdata.client.api.property.PropertyGroup;
import com.cincom.kmdata.client.api.property.PropertyWS;
import com.cincom.kmdata.client.api.relation.Relation;
import com.cincom.kmdata.client.api.relation.RelationWS;
import com.cincom.kmdata.client.api.search.Parameters;
import com.cincom.kmdata.client.api.search.SearchWS;
import com.cincom.kmdata.client.api.search.Parameters.QueryValues;
import com.cincom.kmdata.client.api.server.ServerWS;
import com.cincom.kmdata.client.api.statistic.DocumentHistory;
import com.cincom.kmdata.client.api.statistic.StatisticWS;
import com.cincom.kmdata.client.api.user.KeyValuePair;
import com.cincom.kmdata.client.api.user.UserWS;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * Node Service implementation
 * 
 * @author linzhixing
 * 
 */
public class CEContentServiceImpl implements ContentService {

	private static final Log log = LogFactory
			.getLog(CEContentServiceImpl.class);

	private static final String TOKEN_ATTACHMENT_NODE = "attachmentNode";
	private static final String TOKEN_PATH = "path";
	private static final String TOKEN_CREATION_DATE = "creationDate";
	private static final String TOKEN_MODIFICATION_DATE = "modificationDate";

	private final String PATH_SEPARATOR = "/";
	private final String ROOT_CINCOM_PATH = "/";
	private final String ROOT_CMIS_PATH = "/";

	private String rootObjectId;
	private String mappedTypetoCmisDocument = "";
	private String mappedTypetoCmisFolder = "";
	private String mappedPGtoCmisDocument = "";
	private String mappedPGtoCmisFolder = "";

	private CincomManager cincomManager;
	private ServerWS serverWS;
	private FolderWS folderWS;
	private DocumentWS documentWS;
	private StatisticWS statisticWS;
	private UserWS userWS;
	private RelationWS relationWS;
	private PropertyWS propertyWS;
	private SearchWS searchWS;

	private TypeManager typeManager;

	private NemakiPropertyManager propertyManager;

	public void init() {
		// Set WebService end points
		serverWS = cincomManager.getServerWS();
		folderWS = cincomManager.getFolderWS();
		documentWS = cincomManager.getDocumentWS();
		statisticWS = cincomManager.getStatisticWS();
		userWS = cincomManager.getUserWS();
		relationWS = cincomManager.getRelationWS();
		propertyWS = cincomManager.getPropertyWS();
		searchWS = cincomManager.getSearchWS();

		// Check mapping to cmis:document
		mappedTypetoCmisDocument = propertyManager
				.readValue(CincomPropertyKey.TYPE_MAPPING_TO_CMIS_DOCUMENT);
		if (StringUtils.isNotBlank(mappedTypetoCmisDocument)) {
			mappedPGtoCmisDocument = CmisTypeId.parse(mappedTypetoCmisDocument)
					.getBaseId();
		}
		// Check mapping to cmis:folder
		mappedTypetoCmisFolder = propertyManager
				.readValue(CincomPropertyKey.TYPE_MAPPING_TO_CMIS_FOLDER);
		if (StringUtils.isNotBlank(mappedTypetoCmisFolder)) {
			mappedPGtoCmisFolder = CmisTypeId.parse(mappedTypetoCmisFolder)
					.getBaseId();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// Convert from CincomECM to NemakiWare
	// /////////////////////////////////////////////////////////////////

	/**
	 * Return context token for authentication.
	 * 
	 * @return
	 */
	private String ctxt() {
		return cincomManager.getContext();
	}

	/**
	 * Convert CMIS objectId to CincomECM path
	 * 
	 * @param cmisId
	 * @return
	 */
	public String getPathFromCmisId(CmisId cmisId) {
		if (rootObjectId.equals(cmisId.getBaseId())) {
			return ROOT_CINCOM_PATH;
		} else {
			return getPathFromUuid(cmisId.getBaseId());
		}
	}

	/**
	 * Convert CMIS objectId to CincomECM path
	 * 
	 * @param uuid
	 * @return
	 */
	private String getPathFromUuid(String uuid) {
		try {

			String path = serverWS.getObjectPathFromUUID(ctxt(), uuid);

			String rootFolderUuid = CmisId.parse(rootObjectId).getBaseId();

			if (!rootFolderUuid.equals(uuid) && StringUtils.isBlank(path)) {
				log.error("[uuid=" + uuid
						+ "]Failed to convert UUID in CincomECM server",
						new Throwable());
			}

			return path;
		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * Convert CincomECM path to CincomECM UUID
	 * 
	 * @param cincomPath
	 * @return
	 */
	private String getUuidFormPath(String cincomPath) {
		// TODO null check

		try {
			return serverWS.getUUIDFromObjectPath(ctxt(), cincomPath);
		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		}
		return null;
	}

	/**
	 * Get CMIS objectId of the parent object from CincomECM path
	 * 
	 * @param cincomPath
	 * @return
	 */
	private String getParentCmisId(String cincomPath) {
		String[] splitted = StringUtils.split(cincomPath, PATH_SEPARATOR);
		if (splitted.length <= 1) {
			return rootObjectId;
		} else {
			List<String> _splitted = new ArrayList<String>(
					Arrays.asList(splitted));
			int size = _splitted.size();
			_splitted.remove(size - 1);
			String parentPath = ROOT_CINCOM_PATH
					+ StringUtils.join(_splitted, PATH_SEPARATOR);
			try {
				String uuid = serverWS
						.getUUIDFromObjectPath(ctxt(), parentPath);
				return CmisId._buildFolder(uuid);
			} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
				log.error("", e);
			}
		}

		return null;
	}

	/**
	 * Check the primary type of CincomECM object
	 * 
	 * @param cincomPath
	 * @return
	 */
	private String getCincomType(String cincomPath) {
		try {
			boolean isFolder = serverWS.isFolder(ctxt(), cincomPath);

			if (isFolder) {
				return CincomConst.TYPE_FOLDER;
			}

			boolean isDocument = serverWS.isDocument(ctxt(), cincomPath);
			if (isDocument) {
				return CincomConst.TYPE_DOCUMENT;
			}
		} catch (com.cincom.kmdata.client.api.server.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * Add some fields of CincomECM objects as properties
	 * 
	 * @param properties
	 * @param node
	 */
	private void addSystemProperites(List<Property> properties,
			NodeValueObject node) {
		// processId
		Property processId = new Property(
				CmisPropertyId._buildSystem(CincomConst.PROP_NAME_PROCESSID),
				node.getProcessId());
		properties.add(processId);

		// processList
		Property processList = new Property(
				CmisPropertyId._buildSystem(CincomConst.PROP_NAME_PROCESSLIST),
				node.getProcessList());
		properties.add(processList);

		// processTaskId
		Property processTaskId = new Property(
				CmisPropertyId
						._buildSystem(CincomConst.PROP_NAME_PROCESSTASKID),
				node.getProcessTaskId());
		properties.add(processTaskId);

		// comments
		Property comments = new Property(
				CmisPropertyId._buildSystem(CincomConst.PROP_NAME_COMMENTS),
				node.getComments());
		properties.add(comments);
	}

	// TODO check PropertyDataType and property value class
	/**
	 * Format property key and value
	 * 
	 * @param properties
	 * @param typeName
	 * @return
	 */
	private List<Property> formatProperties(List<Property> properties,
			String typeName) {
		List<Property> result = new ArrayList<Property>();

		for (Property p : properties) {
			Property _p = new Property();

			// Add prefix of CMIS objectType ID
			_p.setKey(CmisPropertyId._build(typeName, p.getKey()));

			// Cast the class of a value
			Object v = p.getValue();
			if (v instanceof String && StringUtils.isBlank((String) v)) {
				// CincoECM property value "" means null
				_p.setValue(null);
				result.add(_p);
				continue;
			} else if (v instanceof StringArray) {
				StringArray _v = (StringArray) v;
				_p.setValue(_v.getItem());
				result.add(_p);
				continue;
			} else if (v instanceof XMLGregorianCalendarImpl) {
				XMLGregorianCalendarImpl _v = (XMLGregorianCalendarImpl) v;
				_p.setValue(_v.toGregorianCalendar());
			} else {
				_p.setValue(v);
			}

			result.add(_p);

		}

		return result;
	}

	/**
	 * Get CincomECM property group from cmis:objectTypeId
	 * 
	 * @param properties
	 * @return
	 */
	private String getCincomPropertyGroup(Properties properties) {
		@SuppressWarnings("unchecked")
		PropertyData<String> objectTypeId = (PropertyData<String>) properties
				.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
		String propertyGroup = CmisTypeId.extractBaseId(objectTypeId
				.getFirstValue());
		return propertyGroup;
	}

	/**
	 * Get CincomECM property keys of a propertyGroup
	 * 
	 * @param propertyGroup
	 * @return
	 */
	private List<String> getCincomProperties(String propertyGroup) {
		List<String> result = new ArrayList<String>();

		try {
			PropertyGroup pg = propertyWS.getPropertyGroup(
					cincomManager.getAdminContext(), propertyGroup, null, null);

			for (com.cincom.kmdata.client.api.property.Property p : pg
					.getProperties()) {
				result.add(p.getName());
			}

		} catch (com.cincom.kmdata.client.api.property.RepositoryException_Exception e) {
			log.error("", e);
		}
		return result;
	}

	/**
	 * Convert ACL of a specified object
	 * 
	 * @param path
	 * @return
	 */
	private Acl convertAcl(String path) {
		List<KeyValuePair> ceAcl = new ArrayList<KeyValuePair>();
		try {
			List<KeyValuePair> ceUserAcl = userWS.getGrantedUsersList(ctxt(),
					path);
			List<KeyValuePair> ceGroupAcl = userWS.getGrantedGroupsList(ctxt(),
					path);
			ceAcl.addAll(ceUserAcl);
			ceAcl.addAll(ceGroupAcl);
		} catch (com.cincom.kmdata.client.api.user.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.user.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.user.RepositoryException_Exception e) {
			log.error("", e);
		}

		Acl acl = new Acl();

		List<Ace> aces = new ArrayList<Ace>();
		for (KeyValuePair kvp : ceAcl) {
			Ace ace = new Ace();
			ace.setPrincipalId((String) kvp.getKey());

			List<String> permissions = new ArrayList<String>();
			byte p = (Byte) kvp.getValue();
			if (p == (p | Permission.READ)) {
				permissions.add(Permission.PERMISSION_READ);
			}
			if (p == (p | Permission.WRITE)) {
				permissions.add(Permission.PERMISSION_WRITE);
			}
			if (p == (p | Permission.DELETE)) {
				permissions.add(Permission.PERMISSION_DELETE);
			}
			if (p == (p | Permission.CREATE)) {
				permissions.add(Permission.PERMISSION_CREATE);
			}
			if (p == (p | Permission.ADMINISTRATOR)) {
				permissions.add(Permission.PERMISSION_ADMINISTRATOR);
			}

			ace.setPermissions(permissions);
			// FIXME hard-coded
			ace.setDirect(false);

			aces.add(ace);
		}

		acl.setLocalAces(aces);

		return acl;
	}

	/**
	 * Convert to (CincomECM-customized) NemakiWare document model
	 * 
	 * @param node
	 * @return
	 */
	public Document convertDocument(NodeValueObject node) {
		Document document = new Document();

		String versionlabel = buildVersionLabel(node.getMajorVersionId(),
				node.getMinorVersionId());

		// objectType
		String typeId = CmisTypeId._build(node.getPropertyGroup());

		// Type mapping to cmis:documnt
		if (StringUtils.isNotBlank(mappedTypetoCmisDocument)
				&& mappedTypetoCmisDocument.equals(typeId)) {
			typeId = NodeType.CMIS_DOCUMENT.value();
		}

		document.setObjectType(typeId);

		// objectId
		document.setId(CmisId._buildDocument(node.getUuid(), versionlabel, null));

		// name
		document.setName(node.getName());

		// Signature
		if (node.getCreationDate() == null) {
			// TODO error logging
		} else {
			document.setCreated(node.getCreationDate().toGregorianCalendar());
		}
		document.setCreator(node.getCreatedBy());

		if (node.getModificationDate() == null) {
			document.setModified(document.getCreated());
		} else {
			document.setModified(node.getModificationDate()
					.toGregorianCalendar());
		}
		document.setModifier(node.getModifiedBy());

		// parentId
		document.setParentId(getParentCmisId(node.getPath()));

		// versioning
		// TODO mockup
		String versionSeriesId = CmisId._buildVersionSeries(node.getUuid(),
				null, null);
		document.setVersionSeriesId(versionSeriesId);
		document.setVersionLabel(versionlabel);
		document.setMajorVersion(node.getMinorVersionId() == 0);
		document.setLatestVersion(node.isLatestVersion());
		document.setLatestMajorVersion(document.isLatestVersion()
				&& document.isMajorVersion());
		document.setCheckinComment("");
		document.setPrivateWorkingCopy(false);

		// Add versionSeries
		VersionSeries vs = buildVersionSeries(node.getPath(), node.getUuid(),
				null, null, node.getMajorVersionId(), node.getMinorVersionId());
		document.setVersionSeries(vs);

		// acl
		// TODO mockup
		document.setAclInherited(false);
		document.setAcl(convertAcl(node.getPath()));

		// changeToken
		if (document.getModified() != null) {
			document.setChangeToken(String.valueOf(document.getModified()
					.getTimeInMillis()));
		}

		// immutable
		document.setImmutable(false);

		// attachment
		if (node.hasContent()) {
			String attachmentNodeId = CmisId._buildContent(node.getUuid(),
					versionlabel, null);
			document.setAttachmentNodeId(attachmentNodeId);
		}

		// rendition
		List<String> renditionIds = new ArrayList<String>();
		String renditionId = CmisId._buildThumbNail(node.getUuid(),
				versionlabel);
		renditionIds.add(renditionId);
		document.setRenditionIds(renditionIds);

		// aspects
		document.setAspects(new ArrayList<Aspect>());

		// subType properties
		List<Property> properties = node.getProperties();
		document.setSubTypeProperties(formatProperties(properties,
				node.getPropertyGroup()));

		// TODO System properties
		addSystemProperites(document.getSubTypeProperties(), node);

		
		//Configure version specific date time
		Map<String, XMLGregorianCalendar>versionSpecificDate = getVersionSpecificDate(node.getPath(), versionlabel);
		XMLGregorianCalendar creationDate = versionSpecificDate
				.get(TOKEN_CREATION_DATE);
		XMLGregorianCalendar modificationDate = versionSpecificDate
				.get(TOKEN_MODIFICATION_DATE);
		if (creationDate != null) {
			document.setCreated(creationDate.toGregorianCalendar());
		}
		if (modificationDate != null) {
			document.setModified(modificationDate.toGregorianCalendar());
		}
		
		return document;
	}

	private Map<String, Object> getAttachmentInternal(CmisId cmisId) {
		String type = cmisId.getType();

		if (CincomConst.TYPE_CONTENT.equals(type)) {
			try {

				String path = serverWS.getObjectPathFromUUID(ctxt(),
						cmisId.getBaseId());

				AttachmentNode an = new AttachmentNode();
				an.setId(cmisId.value());

				if (StringUtils.isBlank(cmisId.getAttachment())) {
					com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
							.getPropertiesByVersion(ctxt(), path,
									cmisId.getVersion());
					// cmis:contentStreamFileName is output as the same as
					// cmis:name
					an.setName(ceDocument.getName());
					an.setLength(ceDocument.getSize());
					an.setMimeType(ceDocument.getMimeType());
				} else {
					com.cincom.kmdata.client.api.relation.Attachment ceAttachment = relationWS
							.getAttachmentByVersion(ctxt(), path,
									cmisId.getVersion(), cmisId.getAttachment());
					an.setName(ceAttachment.getFileName());
					an.setLength(ceAttachment.getSize());
					an.setMimeType(ceAttachment.getMimeType());
				}

				Map<String, Object> map = new HashMap<String, Object>();
				map.put(TOKEN_ATTACHMENT_NODE, an);
				map.put(TOKEN_PATH, path);
				return map;
			} catch (IOException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.relation.AccessDeniedException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.relation.IOException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.relation.ItemNotFoundException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.relation.RepositoryException_Exception e) {
				log.error("", e);
			}
		}

		return null;
	}

	/**
	 * Convert CincomECM attachment documents to (CincomECM-customized)
	 * NemakiWare document
	 * 
	 * @param attachment
	 * @param mainDocument
	 * @return
	 */
	// FIXME ?
	private Document convertAttachedDocument(
			com.cincom.kmdata.client.api.relation.Attachment attachment,
			com.cincom.kmdata.client.api.document.Document mainDocument) {
		String baseId = mainDocument.getUuid();
		String versionId = mainDocument.getActualVersion().getId();

		Document document = new Document();

		// objectType
		// FIXME hard-coded
		String attachmentTypeId = CincomConst.DOCTYPE_ID_ATTACHMENT;
		document.setObjectType(attachmentTypeId);

		// objectId
		String docId = CmisId._buildDocument(baseId, versionId,
				attachment.getId());
		document.setId(docId);

		// name
		document.setName(attachment.getFileName());

		// Signature
		if (attachment.getCreationDate() != null) {
			document.setCreated(attachment.getCreationDate()
					.toGregorianCalendar());
			document.setModified(attachment.getCreationDate()
					.toGregorianCalendar());
		}

		// FIXME hard-coded
		document.setCreator(NemakiConstant.PRINCIPAL_SYSTEM);
		document.setModifier(NemakiConstant.PRINCIPAL_SYSTEM);

		// parentId
		document.setParentId("");

		// versioning
		// TODO mockup
		String versionSeriesId = CmisId._buildVersionSeries(baseId, versionId,
				attachment.getId());
		document.setVersionSeriesId(versionSeriesId);
		document.setVersionLabel("1.0");
		// FIXME overwrite in CompileObjectService ?
		document.setMajorVersion(true);
		document.setLatestMajorVersion(true);
		document.setLatestVersion(true);
		document.setCheckinComment(null);
		document.setPrivateWorkingCopy(false);

		// acl
		document.setAclInherited(false);
		Acl acl = convertAcl(mainDocument.getPath());
		document.setAcl(acl);

		// changeToken
		// TODO mockup
		document.setChangeToken("0");

		// immutable
		document.setImmutable(false);

		// attachment
		// TODO multiple attachments

		document.setAttachmentNodeId(CmisId._buildContent(baseId, versionId,
				attachment.getId()));

		return document;
	}

	/**
	 * Build CMIS VersionSeries object
	 * 
	 * @param path
	 * @param uuid
	 * @param versionId
	 * @param attachmentId
	 * @param latestMajorVersionId
	 * @param latestMinorVersionId
	 * @return
	 */
	private VersionSeries buildVersionSeries(String path, String uuid,
			String versionId, String attachmentId, long latestMajorVersionId,
			long latestMinorVersionId) {

		LockInfo lockInfo = null;
		try {
			lockInfo = documentWS.getLockInfo(ctxt(), path);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

		VersionSeries vs = new VersionSeries();

		// TODO why versionId is needed ?
		String id = CmisId._buildVersionSeries(uuid, versionId, attachmentId);
		vs.setId(id);

		if (lockInfo == null) {
			vs.setVersionSeriesCheckedOut(false);
		} else {
			vs.setVersionSeriesCheckedOut(true);
			vs.setVersionSeriesCheckedOutBy(lockInfo.getOwner());
			String latestVersionlabel = buildVersionLabel(latestMajorVersionId,
					latestMinorVersionId);
			String pwcId = CmisId._buildPwc(uuid, latestVersionlabel);
			vs.setVersionSeriesCheckedOutId(pwcId);
		}

		return vs;

	}

	/**
	 * Generate version label
	 * 
	 * @param major
	 * @param minor
	 * @return
	 */
	private String buildVersionLabel(long major, long minor) {
		return String.valueOf(major) + "." + String.valueOf(minor);
	}

	/**
	 * Set document poperties for Create/Update
	 * 
	 * @param ceDocument
	 * @param properties
	 * @param typeDefinition
	 */
	private void setDocumentProperties(
			com.cincom.kmdata.client.api.document.Document ceDocument,
			Properties properties, TypeDefinition typeDefinition) {

		// ////////////////////////////
		// Prepare contextual values
		// ////////////////////////////

		// Source properties: Target document will be overwritten by this
		Map<String, PropertyData<?>> sources = properties.getProperties();

		// If no sources, do nothing
		if (MapUtils.isEmpty(sources))
			return;

		// Cincom propertyGroup
		String propertyGroup = ceDocument.getPropertyGroup();
		if (StringUtils.isBlank(propertyGroup)) {
			propertyGroup = getCincomPropertyGroup(properties);
		}

		// Cincom property names
		List<String> cePropertyIds = getCincomProperties(propertyGroup);

		// Build the target document's properties as map
		Map<String, com.cincom.kmdata.client.api.document.Document.Properties.Entry> targetsMap = new HashMap<String, com.cincom.kmdata.client.api.document.Document.Properties.Entry>();
		if (ceDocument.getProperties() != null
				&& CollectionUtils.isNotEmpty(ceDocument.getProperties()
						.getEntry())) {
			for (com.cincom.kmdata.client.api.document.Document.Properties.Entry t : ceDocument
					.getProperties().getEntry()) {
				String propertyId = CmisPropertyId._build(
						CmisTypeId.extractBaseId(typeDefinition.getId()),
						t.getKey());
				targetsMap.put(propertyId, t);
			}
		}

		// ////////////////////////////
		// Edit values to be updated
		// ////////////////////////////
		Map<String, PropertyDefinition<?>> propertyDefinitions = typeDefinition
				.getPropertyDefinitions();
		List<com.cincom.kmdata.client.api.document.Document.Properties.Entry> result = new ArrayList<com.cincom.kmdata.client.api.document.Document.Properties.Entry>();
		for (Entry<String, PropertyDefinition<?>> _pdf : propertyDefinitions
				.entrySet()) {
			String propId = _pdf.getKey();
			PropertyData<?> source = sources.get(propId);

			// //// System properties //////

			// cmis:name
			if (propId.equals(PropertyIds.NAME) && source != null) {
				ceDocument.setName(DataUtil.getStringProperty(properties,
						PropertyIds.NAME));
				continue;
			}

			// cmis:objectTypeId
			if (propId.equals(PropertyIds.OBJECT_TYPE_ID) && source != null) {
				ceDocument.setPropertyGroup(propertyGroup);
			}

			// //// Variable properties //////

			// Corresponding target property
			com.cincom.kmdata.client.api.document.Document.Properties.Entry target = targetsMap
					.get(propId);

			// Check source
			if (source == null) {
				if (target == null) {
					continue;
				} else {
					// TODO cannot deep copy?
					result.add(target);
					continue;
				}
			} else {
				com.cincom.kmdata.client.api.document.Document.Properties.Entry e = new com.cincom.kmdata.client.api.document.Document.Properties.Entry();

				// Extract property key without prefix
				String propKey = CmisPropertyId.extractBaseId(source.getId(),
						CmisTypeId.extractBaseId(typeDefinition.getId()));

				// Check the property key exists in CincomECM
				if (cePropertyIds.contains(propKey)) {
					e.setKey(CmisPropertyId.extractBaseId(source.getId(),
							CmisTypeId.extractBaseId(typeDefinition.getId())));
					// Cardinality
					PropertyDefinition<?> pdf = _pdf.getValue();
					if (pdf.getCardinality() == Cardinality.SINGLE) {
						e.setValue(source.getFirstValue());
					} else {
						e.setValue(source.getValues());
					}
					result.add(e);
				} else {
					continue;
				}

			}

		}

		// ////////////////////////////
		// Overwrite
		// ////////////////////////////
		com.cincom.kmdata.client.api.document.Document.Properties newProps = new com.cincom.kmdata.client.api.document.Document.Properties();
		for (com.cincom.kmdata.client.api.document.Document.Properties.Entry e : result) {
			newProps.getEntry().add(e);
		}
		ceDocument.setProperties(newProps);
	}

	/**
	 * Convert to NemakiWare folder
	 * 
	 * @param node
	 * @return
	 */
	public Folder convertFolder(NodeValueObject node) {
		Folder folder = new Folder();

		// objectType
		String typeId = CmisTypeId._build(node.getPropertyGroup());

		// Type Mapping to cmis:folder
		if (StringUtils.isNotBlank(mappedTypetoCmisFolder)
				&& mappedTypetoCmisFolder.equals(typeId)) {
			typeId = NodeType.CMIS_FOLDER.value();
		}

		folder.setObjectType(typeId);

		// objectId
		folder.setId(CmisId._buildFolder(node.getUuid()));

		// name
		folder.setName(node.getName());

		// description
		// TODO mock up
		folder.setDescription(null);

		// Signature
		// TODO if creationDate is null, logging & set default value
		if (node.getCreationDate() == null) {
			// TODO logging or put default value
		} else {
			folder.setCreated(node.getCreationDate().toGregorianCalendar());
		}
		folder.setCreator(node.getCreatedBy());

		if (node.getModificationDate() == null) {
			folder.setModified(folder.getCreated());
		} else {
			folder.setModified(node.getModificationDate().toGregorianCalendar());
		}
		folder.setModifier(node.getModifiedBy());

		// parentId
		folder.setParentId(getParentCmisId(node.getPath()));

		// allowedChildTypeIds
		List<String> allowedChildTypeIds = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(node.getPropertyGroups())) {

			for (String pg : node.getPropertyGroups()) {
				String allowed = CmisTypeId._build(pg);

				// Filtering by mapping to cmis:document
				if (mappedPGtoCmisDocument.equals(pg)) {
					allowedChildTypeIds.add(BaseTypeId.CMIS_DOCUMENT.value());
					continue;
				}
				// Filtering by mapping to cmis:folder
				if (mappedPGtoCmisFolder.equals(pg)) {
					allowedChildTypeIds.add(BaseTypeId.CMIS_FOLDER.value());
					continue;
				}

				allowedChildTypeIds.add(allowed);
			}
		}
		folder.setAllowedChildTypeIds(allowedChildTypeIds);

		// acl
		folder.setAclInherited(false);
		folder.setAcl(convertAcl(node.getPath()));

		// changeToken
		// TODO null check
		if (folder.getModified() != null) {
			String changeToken = String.valueOf(folder.getModified()
					.getTimeInMillis());
			folder.setChangeToken(changeToken);
		}

		// aspects
		// TODO mockup
		folder.setAspects(new ArrayList<Aspect>());

		// subType properties
		List<Property> properties = node.getProperties();
		folder.setSubTypeProperties(formatProperties(properties,
				node.getPropertyGroup()));

		// System properties
		addSystemProperites(folder.getSubTypeProperties(), node);

		// Specific to Root Folder
		// TODO mock up
		if (ROOT_CINCOM_PATH.equals(node.getPath())
				|| "".equals(node.getPath())) {
			configureRootFolder(folder);
		}

		return folder;
	}

	/**
	 * Set some value to the root folder
	 * 
	 * @param folder
	 */
	private void configureRootFolder(Folder folder) {
		folder.setId(rootObjectId);
		folder.setName("Root"); // CincomECM root has no name
		folder.setParentId(null);

		// Set allowedChildTypeIds mapping to cmis:document/folder
		List<String> allowedChiledTypeIds = new ArrayList<String>();
		List<String> _allowedChiledTypeIds = propertyManager
				.readValues(CincomPropertyKey.TYPE_ROOT_CHILD_ALLOWED);
		if (CollectionUtils.isNotEmpty(_allowedChiledTypeIds)) {
			for (String c : _allowedChiledTypeIds) {
				if (c.equals(mappedTypetoCmisDocument)) {
					allowedChiledTypeIds.add(NodeType.CMIS_DOCUMENT.value());
				} else if (c.equals(mappedTypetoCmisFolder)) {
					allowedChiledTypeIds.add(NodeType.CMIS_FOLDER.value());
				} else {
					allowedChiledTypeIds.add(c);
				}
			}
		}
		folder.setAllowedChildTypeIds(allowedChiledTypeIds);
	}

	/**
	 * Set folder properties for Create/Update
	 * 
	 * @param ceFolder
	 * @param properties
	 * @param typeDefinition
	 */
	private void setFolderProperties(
			com.cincom.kmdata.client.api.folder.Folder ceFolder,
			Properties properties, TypeDefinition typeDefinition) {

		// ////////////////////////////
		// Prepare contextual values
		// ////////////////////////////

		// Source properties: Target folder will be overwritten by this
		Map<String, PropertyData<?>> sources = properties.getProperties();

		// If no sources, do nothing
		if (MapUtils.isEmpty(sources)) {
			return;
		}

		// CincomECM propertyGroup
		String propertyGroup = ceFolder.getPropertyGroup();
		if (StringUtils.isBlank(propertyGroup)) {
			propertyGroup = getCincomPropertyGroup(properties);
		}

		// CincomECM property names
		List<String> cePropertyIds = getCincomProperties(propertyGroup);

		// Build the target document's properties as map
		Map<String, com.cincom.kmdata.client.api.folder.Folder.Properties.Entry> targetsMap = new HashMap<String, com.cincom.kmdata.client.api.folder.Folder.Properties.Entry>();
		if (ceFolder.getProperties() != null
				&& CollectionUtils.isNotEmpty(ceFolder.getProperties()
						.getEntry())) {
			for (com.cincom.kmdata.client.api.folder.Folder.Properties.Entry t : ceFolder
					.getProperties().getEntry()) {
				String propertyId = CmisPropertyId._build(
						CmisTypeId.extractBaseId(typeDefinition.getId()),
						t.getKey());
				targetsMap.put(propertyId, t);
			}
		}

		// ////////////////////////////
		// Edit values to be updated
		// ////////////////////////////
		Map<String, PropertyDefinition<?>> propertyDefinitions = typeDefinition
				.getPropertyDefinitions();
		List<com.cincom.kmdata.client.api.folder.Folder.Properties.Entry> result = new ArrayList<com.cincom.kmdata.client.api.folder.Folder.Properties.Entry>();
		for (Entry<String, PropertyDefinition<?>> _pdf : propertyDefinitions
				.entrySet()) {

			String propId = _pdf.getKey();

			PropertyData<?> source = sources.get(propId);

			// //// System properties //////

			// cmis:name
			if (propId.equals(PropertyIds.NAME) && source != null) {
				ceFolder.setName(DataUtil.getStringProperty(properties,
						PropertyIds.NAME));
				continue;
			}

			// cmis:objectTypeId
			if (propId.equals(PropertyIds.OBJECT_TYPE_ID) && source != null) {
				ceFolder.setPropertyGroup(propertyGroup);
			}

			// //// Variable properties //////

			// Corresponding target property
			com.cincom.kmdata.client.api.folder.Folder.Properties.Entry target = targetsMap
					.get(propId);

			// Check source
			if (source == null) {
				if (target == null) {
					continue;
				} else {
					// TODO cannot deep copy?
					result.add(target);
					continue;
				}
			} else {
				com.cincom.kmdata.client.api.folder.Folder.Properties.Entry e = new com.cincom.kmdata.client.api.folder.Folder.Properties.Entry();

				// Extract property key without prefix
				String propKey = CmisPropertyId.extractBaseId(source.getId(),
						CmisTypeId.extractBaseId(typeDefinition.getId()));

				// Check the property key exists in CincomECM
				if (cePropertyIds.contains(propKey)) {
					e.setKey(CmisPropertyId.extractBaseId(source.getId(),
							CmisTypeId.extractBaseId(typeDefinition.getId())));
					// Cardinality
					PropertyDefinition<?> pdf = _pdf.getValue();
					if (pdf.getCardinality() == Cardinality.SINGLE) {
						e.setValue(source.getFirstValue());
					} else {
						e.setValue(source.getValues());
					}
					result.add(e);
				} else {
					continue;
				}

			}
		}

		// ////////////////////////////
		// Overwrite
		// ////////////////////////////
		com.cincom.kmdata.client.api.folder.Folder.Properties newProps = new com.cincom.kmdata.client.api.folder.Folder.Properties();
		for (com.cincom.kmdata.client.api.folder.Folder.Properties.Entry e : result) {
			newProps.getEntry().add(e);
		}
		ceFolder.setProperties(newProps);
	}

	/**
	 * Build CMIS relationships of an object specified by CincomECM path
	 * 
	 * @param path
	 * @return
	 */
	private List<Relationship> buildRelationships(String path) {
		List<Relationship> result = new ArrayList<Relationship>();

		Collection<Relation> ceRelations;
		try {
			ceRelations = relationWS.getRelations(ctxt(), path, "");

			if (CollectionUtils.isNotEmpty(ceRelations)) {
				for (Relation ceRelation : ceRelations) {
					Relationship rel = convertRelationship(ceRelation);
					result.add(rel);
				}
			}

		} catch (com.cincom.kmdata.client.api.relation.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.RepositoryException_Exception e) {
			log.error("", e);
		}

		return result;
	}

	/**
	 * Convert CincomECM Relation to CMIS Relationship
	 * 
	 * @param ceRelation
	 * @return
	 */
	private Relationship convertRelationship(Relation ceRelation) {
		Relationship rel = new Relationship();
		// TODO difference of name and label
		String typeId = CmisTypeId._buildRelation(ceRelation.getName());
		rel.setObjectType(typeId);
		rel.setName(ceRelation.getName());

		// Source
		String sourcePath = ceRelation.getSourcePath();
		String sourceVersionId = ceRelation.getSourceVersion();
		String sourceId = getUuidFormPath(sourcePath);
		String sourceType = getCincomType(sourcePath);
		CmisId sourceCmisId = null;

		if (CincomConst.TYPE_DOCUMENT.equals(sourceType)) {
			sourceCmisId = CmisId
					.buildDocument(sourceId, sourceVersionId, null);
		} else if (CincomConst.TYPE_FOLDER.equals(sourceType)) {
			sourceCmisId = CmisId.buildFolder(sourceId);
		}
		rel.setSourceId(sourceCmisId.value());

		// Target
		String targetPath = ceRelation.getDestinationPath();
		String targetVersionId = ceRelation.getDestinationVersion();
		String targtType = getCincomType(targetPath);
		String targetId = getUuidFormPath(targetPath);
		CmisId targetCmisId = null;

		if (CincomConst.TYPE_DOCUMENT.equals(targtType)) {
			targetCmisId = CmisId
					.buildDocument(targetId, targetVersionId, null);
		} else if (CincomConst.TYPE_FOLDER.equals(targtType)) {
			targetCmisId = CmisId.buildFolder(targetId);
		}
		rel.setTargetId(targetCmisId.value());

		// Set reciprocal
		List<Property> subProps = new ArrayList<Property>();
		// FIXME hard-coding
		// TODO WHEN false ?

		Property reciprocal = new Property(CincomConst.PROP_ID_RECIPROCAL, true);
		subProps.add(reciprocal);
		rel.setSubTypeProperties(subProps);

		// TODO set by constructor
		String relId = CmisId._buildRelationship(sourceId, targetId,
				CincomConst.PREFIX_RELATION + ceRelation.getName());
		rel.setId(relId);

		return rel;
	}

	// TODO javadoc
	/**
	 * Build CMIS relationships
	 * 
	 * @param cmisId
	 * @param path
	 * @return
	 */
	private List<Relationship> buildHasAtachmentRelationshipsForMain(
			CmisId cmisId, String path) {
		List<Relationship> result = new ArrayList<Relationship>();

		try {
			List<com.cincom.kmdata.client.api.relation.Attachment> attachments = relationWS
					.getAttachmentsByVersion(ctxt(), path,
							cmisId.getVersionWithoutPwc());

			if (CollectionUtils.isNotEmpty(attachments)) {
				for (com.cincom.kmdata.client.api.relation.Attachment attachment : attachments) {
					// AttachedDocument's id
					CmisId attachmentCmisId = CmisId.buildDocument(
							cmisId.getBaseId(), cmisId.getVersion(),
							attachment.getId());

					// Build "hasAttachment" relationship
					Relationship rel = new Relationship();
					rel.setObjectType(CincomConst.RELTYPE_ID_HAS_ATTACHMENT);
					rel.setName(CincomConst.RELTYPE_NAME_HAS_ATTACHMENT);
					rel.setSourceId(cmisId.value());
					rel.setTargetId(attachmentCmisId.value());

					String relId = CmisId._buildRelationship(rel,
							CincomConst.RELTYPE_ID_HAS_ATTACHMENT);
					rel.setId(relId);

					result.add(rel);
				}

			}

		} catch (com.cincom.kmdata.client.api.relation.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.RepositoryException_Exception e) {
			log.error("", e);
		}

		return result;
	}

	private Relationship buildHasAttachmentRelationshipForAttached(CmisId cmisId) {
		// Main document id
		String mainDocId = CmisId._buildDocument(cmisId.getBaseId(),
				cmisId.getVersion(), null);

		// Build "hasAttachment" relationship
		Relationship rel = new Relationship();
		rel.setObjectType(CincomConst.RELTYPE_ID_HAS_ATTACHMENT);
		rel.setName(CincomConst.RELTYPE_NAME_HAS_ATTACHMENT);
		rel.setSourceId(mainDocId);
		rel.setTargetId(cmisId.value());
		String relId = CmisId._buildRelationship(rel,
				CincomConst.RELTYPE_ID_HAS_ATTACHMENT);
		rel.setId(relId);

		return rel;
	}
	
	private Change convertChange(DocumentHistory  documentHistory){
		Change change = new Change();
		
		change.setId(documentHistory.getUuid());
		change.setName(documentHistory.getUri()); // FIXME Uri is not name
		
		GregorianCalendar cal = documentHistory.getDate().toGregorianCalendar();
		change.setTime(cal);
		change.setChangeToken(String.valueOf(cal.getTimeInMillis()));
		
		String uuid = documentHistory.getUuid();
		if (documentHistory.isDocument()) {
			change.setBaseType(BaseTypeId.CMIS_DOCUMENT.value());
			String objectId = CmisId._buildDocument(uuid, documentHistory.getVersionId(), null);
			change.setObjectId(objectId);
		} else if (documentHistory.isFolder()) {
			change.setBaseType(BaseTypeId.CMIS_FOLDER.value());
			String objectId = CmisId._buildFolder(uuid);
			change.setObjectId(objectId);
		}

		// TODO type mapping
		String objectType = CmisTypeId._build(documentHistory.getPropertyGroup());
		change.setObjectType(objectType);

		String actionId = documentHistory.getActionId();
		if (CincomConst.ACTION_CREATE.equals(actionId)) {
			change.setChangeType(ChangeType.CREATED);
		} else if (CincomConst.ACTION_UPATE.equals(actionId)) {
			change.setChangeType(ChangeType.UPDATED);
		} else if (CincomConst.ACTION_DELETE.equals(actionId)) {
			change.setChangeType(ChangeType.DELETED);
		} else {
			change.setChangeType(null);
		}
		
		return change;
	}
	
	private XMLGregorianCalendar convertMilliSecondsToCalendar (String token){
		long _token = 0;
		if(!StringUtils.isBlank(token)){
			_token = Long.valueOf(token);
		}
		
		Date date = new Date(_token);
		Calendar cal= GregorianCalendar.getInstance();
		cal.setTime(date);
		XMLGregorianCalendar xcal = new XMLGregorianCalendarImpl((GregorianCalendar)cal);
	
		return xcal;
	}

	// /////////////////////////////////////////////////////////////////
	// Content Service
	// /////////////////////////////////////////////////////////////////
	@Override
	public boolean isRoot(Folder folder) {
		return rootObjectId.equals(folder.getId());
	}

	@Override
	public boolean existContent(String objectTypeId) {

		// TODO Auto-generated method stub
		// FIXME mockup
		return true;
	}

	@Override
	public Content getContent(String objectId) {

		CmisId cmisId = CmisId.parse(objectId);
		String type = cmisId.getType();

		// FIXME hard-coding
		if (rootObjectId.equals(objectId)) {
			return getFolder(objectId);
		}

		if (CincomConst.TYPE_DOCUMENT.equals(type)) {
			return getDocument(objectId);
		} else if (CincomConst.TYPE_FOLDER.equals(type)) {
			return getFolder(objectId);
		} else if (CincomConst.TYPE_RELATION.equals(type)) {
			return getRelationship(objectId);
		} else {
			return null;
		}
	}

	@Override
	public Content getContentByPath(String path) {
		try {
			if (serverWS.isFolder(ctxt(), path)) {
				return getFolderByPath(path);
			} else if (serverWS.isDocument(ctxt(), path)) {
				return getDocumentByPath(path, null, false, null);
			}
		} catch (com.cincom.kmdata.client.api.server.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public Folder getParent(String objectId) {
		CmisId cmisId = CmisId.parse(objectId);
		String cincomPath = getPathFromCmisId(cmisId);
		String parentId = getParentCmisId(cincomPath);
		Folder parent = getFolder(parentId);
		return parent;
	}

	@Override
	public List<Content> getChildren(String folderId) {

		try {
			CmisId cmisId = CmisId.parse(folderId);
			List<Content> list = new ArrayList<Content>();
			String path = getPathFromCmisId(cmisId);

			ResultSet resultSet = folderWS.getChildren(ctxt(), path, null);
			List<Result> results = resultSet.getResults();
			for (Result result : results) {
				Object obj = result.getObject();

				if (obj instanceof com.cincom.kmdata.client.api.folder.Folder) {
					com.cincom.kmdata.client.api.folder.Folder ceFolder = (com.cincom.kmdata.client.api.folder.Folder) obj;
					Folder folder = convertFolder(new NodeValueObject(ceFolder));
					list.add(folder);

				} else if (obj instanceof com.cincom.kmdata.client.api.folder.Document) {
					Document document = null;

					com.cincom.kmdata.client.api.folder.Document ceDocument = (com.cincom.kmdata.client.api.folder.Document) obj;

					// if it's WorkingCopy, return FrozenCopy as a
					// child(WorkingCopy should be linked as PWC)
					if (ceDocument.getLockInfo() != null
							&& ceDocument.getLockInfo().isUserOwner()) {
						com.cincom.kmdata.client.api.document.Document frozenCopy = getFrozenCopy(
								ceDocument.getPath(),
								ceDocument.getMajorVersionId(),
								ceDocument.getMinorVersionId());
						document = convertDocument(new NodeValueObject(
								frozenCopy));
					} else {
						document = convertDocument(new NodeValueObject(
								ceDocument));
					}
					
					//Add to the list
					list.add(document);
				}
			}
			return list;
		} catch (InformationException_Exception e) {
			log.error("", e);
		} catch (ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public Document getDocument(String objectId) {
		CmisId cmisId = CmisId.parse(objectId);
		String versionId = null;

		boolean isPwc = cmisId.isPwc();
		if (isPwc) {
			versionId = cmisId.getVersionWithoutPwc();
			cmisId.setVersion(versionId);
		} else {
			versionId = cmisId.getVersion();
		}

		String attachmentId = cmisId.getAttachment();
		String path = getPathFromCmisId(cmisId);

		return getDocumentByPath(path, versionId, isPwc, attachmentId);
	}

	private Map<String, XMLGregorianCalendar> getVersionSpecificDate(
			String path, String versionId) {
		Map<String, XMLGregorianCalendar> result = new HashMap<String, XMLGregorianCalendar>();

		List<DocumentHistory> histories = new ArrayList<DocumentHistory>();
		try {
			histories = statisticWS
					.getHistoryLog(ctxt(), path, 0, 0, "", false);
		} catch (com.cincom.kmdata.client.api.statistic.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.statistic.RepositoryException_Exception e) {
			log.error("", e);
		}

		if (CollectionUtils.isNotEmpty(histories)) {
			List<DocumentHistory> historiesOfTheVersion = new ArrayList<DocumentHistory>();
			for (DocumentHistory history : histories) {
				if (versionId.equals(history.getVersionId())) {
					historiesOfTheVersion.add(history);
				}
			}
			if (CollectionUtils.isNotEmpty(historiesOfTheVersion)) {
				XMLGregorianCalendar creationDate = historiesOfTheVersion.get(
						historiesOfTheVersion.size() - 1).getDate();
				XMLGregorianCalendar modificationDate = historiesOfTheVersion
						.get(0).getDate();

				result.put(TOKEN_CREATION_DATE, creationDate);
				result.put(TOKEN_MODIFICATION_DATE, modificationDate);
			}
		}

		return result;
	}

	private Document getDocumentByPath(String path, String versionId,
			boolean isPwc, String attachmentId) {
		com.cincom.kmdata.client.api.document.Document ceDocument = null;

		try {
			// Get a document
			ceDocument = documentWS.getPropertiesByVersion(ctxt(), path,
					versionId);
			if (!isPwc && ceDocument.getActualVersion() != null
					& ceDocument.isCheckedOut()) {
				// Overwrite in the case of frozen copy
				com.cincom.kmdata.client.api.document.Document frozenCopy = getFrozenCopy(
						path, ceDocument.getMajorVersionId(),
						ceDocument.getMinorVersionId());

				ceDocument = frozenCopy;
			}

			// Convert
			if (StringUtils.isBlank(attachmentId)) {
				Document document = convertDocument(new NodeValueObject(
						ceDocument));
				if (isPwc) {
					configurePwc(document);
					return document;
				} else {
					return document;
				}
			} else {
				com.cincom.kmdata.client.api.relation.Attachment attachment = relationWS
						.getAttachmentByVersion(ctxt(), path, versionId,
								attachmentId);
				return convertAttachedDocument(attachment, ceDocument);
			}
		} catch (Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * Set some fields to a private working copy
	 * 
	 * @param toBePwc
	 */
	private void configurePwc(Document toBePwc) {
		// Prepare CmisId with PWC suffix
		CmisId pwcCmisId = CmisId.parse(toBePwc.getId());
		pwcCmisId.setVersion(pwcCmisId.getVersionWitPwc());

		// Overwrite Document properties
		toBePwc.setId(pwcCmisId.value());
		toBePwc.setLatestVersion(false);
		toBePwc.setLatestMajorVersion(false);
		toBePwc.setVersionLabel("");
		toBePwc.setPrivateWorkingCopy(true);
	}

	/**
	 * Get CincomECM "frozen" copy
	 * 
	 * @param path
	 * @param majorVersionId
	 * @param minorVersionId
	 * @return
	 */
	private com.cincom.kmdata.client.api.document.Document getFrozenCopy(
			String path, long majorVersionId, long minorVersionId) {
		String versionId = majorVersionId + "." + minorVersionId;
		try {
			com.cincom.kmdata.client.api.document.Document frozenCopy = documentWS
					.getPropertiesByVersion(ctxt(), path, versionId);
			configureFrozenCopy(frozenCopy);
			return frozenCopy;
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * Set some fields to a frozen copy(the latest document but not PWC)
	 * 
	 * @param ceDocument
	 */
	private void configureFrozenCopy(
			com.cincom.kmdata.client.api.document.Document ceDocument) {
		Version version = new Version(); // dummy
		ceDocument.setActualVersion(version);
	}

	@Override
	public Document getDocumentOfLatestVersion(String versionSeriesId) {
		CmisId cmisId = CmisId.parse(versionSeriesId);
		if (StringUtils.isNotBlank(cmisId.getAttachment())) {
			return getAttachedDocumentOfLatestVersion(cmisId);
		}

		try {

			String path = getPathFromCmisId(cmisId);
			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getDocument(ctxt(), path, null);
			// TODO integrate ID-convention logic with getDocumentByPath

			return convertDocument(new NodeValueObject(ceDocument));
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * AttachedDocument is always its latest and only version
	 * 
	 * @param versionSeriesCmisId
	 * @return
	 */
	private Document getAttachedDocumentOfLatestVersion(
			CmisId versionSeriesCmisId) {
		String docId = CmisId._buildDocument(versionSeriesCmisId.getBaseId(),
				versionSeriesCmisId.getVersion(),
				versionSeriesCmisId.getAttachment());
		Document doc = getDocument(docId);
		return doc;
	}

	@Override
	public Document getDocumentOfLatestMajorVersion(String versionSeriesId) {
		CmisId cmisId = CmisId.parse(versionSeriesId);

		// AttachedDocument
		if (StringUtils.isNotBlank(cmisId.getAttachment())) {
			return getAttachedDocumentOfLatestVersion(cmisId);
		}

		// NormalDocument
		String path = getPathFromCmisId(cmisId);
		try {
			com.cincom.kmdata.client.api.document.Document latest = documentWS
					.getDocument(ctxt(), path, "");
			String versionLabel = buildVersionLabel(latest.getMajorVersionId(),
					0);
			com.cincom.kmdata.client.api.document.Document latestMajor = documentWS
					.getPropertiesByVersion(ctxt(), path, versionLabel);
			return convertDocument(new NodeValueObject(latestMajor));
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<jp.aegif.nemaki.model.Document> getAllVersions(
			CallContext callContext, String versionSeriesId) {

		List<jp.aegif.nemaki.model.Document> result = new ArrayList<jp.aegif.nemaki.model.Document>();

		List<Document> _result = getAllVersionsInternal(callContext,
				versionSeriesId);

		if (CollectionUtils.isNotEmpty(_result)) {
			result = new ArrayList<jp.aegif.nemaki.model.Document>(_result);
		}
		return result;
	}

	private List<Document> getAllVersionsInternal(CallContext callContext,
			String versionSeriesId) {
		CmisId cmisId = CmisId.parse(versionSeriesId);

		// AttacedDocument
		if (StringUtils.isNotBlank(cmisId.getAttachment())) {
			getAttachedDocumentAllVersions(versionSeriesId);
		}

		// NormalDocument
		try {
			List<Document> l = new ArrayList<Document>();

			String path = getPathFromCmisId(cmisId);
			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getDocument(ctxt(), path, null);

			// Add PWC when checked out and the user is the owner.
			// PWC must be on the top according to the specs.
			if (ceDocument.isCheckedOut()) {
				if (ceDocument.getLockInfo() != null
						&& ceDocument.getLockInfo().isUserOwner()) {
					Document pwc = convertDocument(new NodeValueObject(
							ceDocument));
					configurePwc(pwc);
					l.add(pwc);
				}
			}

			// Add versions
			// TODO sort in descending oder
			List<com.cincom.kmdata.client.api.document.Version> versions = documentWS
					.getVersions(ctxt(), ceDocument.getPath());
			for (com.cincom.kmdata.client.api.document.Version version : versions) {
				com.cincom.kmdata.client.api.document.Document versionDocument = documentWS
						.getPropertiesByVersion(ctxt(), ceDocument.getPath(),
								version.getId());
				l.add(convertDocument(new NodeValueObject(versionDocument)));
			}

			// FIXME WORKAROUND: sort by versionLabel
			//Collections.sort(l, new VersionComparator());
			
			
			return l;
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	private class VersionComparator implements Comparator<Document> {
		// PWC has blank versionLabel and should be the first in the list

		public VersionComparator() {
		}

		@Override
		public int compare(Document o1, Document o2) {
			if (StringUtils.isBlank(o1.getVersionLabel())
					&& StringUtils.isBlank(o2.getVersionLabel())) {
				// TODO warning
				return 0;
			} else if (StringUtils.isBlank(o1.getVersionLabel())
					&& !StringUtils.isBlank(o2.getVersionLabel())) {
				return -1;
			} else if (!StringUtils.isBlank(o1.getVersionLabel())
					&& StringUtils.isBlank(o2.getVersionLabel())) {
				return 1;
			} else {
				return o2.getVersionLabel().compareTo(o1.getVersionLabel());
			}
		}

	}

	/**
	 * AttachedDocument is always its latest and only version
	 * 
	 * @param versionSeriesId
	 * @return
	 */
	private List<Document> getAttachedDocumentAllVersions(String versionSeriesId) {
		CmisId cmisId = CmisId.parse(versionSeriesId);
		Document doc = getAttachedDocumentOfLatestVersion(cmisId);

		List<Document> result = new ArrayList<Document>();
		result.add(doc);
		return result;
	}

	@Override
	public List<jp.aegif.nemaki.model.Document> getCheckedOutDocs(
			String folderId, String orderBy, ExtensionsData extension) {
		List<jp.aegif.nemaki.model.Document> checkedOutDocs = new ArrayList<jp.aegif.nemaki.model.Document>();

		List<Document> _checkedOutDocs = new ArrayList<Document>();

		//Use SearchWS (@isCheckedOut='true')
		QueryValues queryValues = new QueryValues();
		com.cincom.kmdata.client.api.search.Parameters.QueryValues.Entry entry = new com.cincom.kmdata.client.api.search.Parameters.QueryValues.Entry();
		entry.setKey("."); 
		String query = "@isCheckedOut='true'";
		entry.setValue(query);
		queryValues.getEntry().add(entry);

		Parameters parameters = new Parameters();
		parameters.setFilter(1); // no filter
		parameters.setQueryValues(queryValues); 
		try {
			String cincomFolderPath = "";
			if(StringUtils.isNotBlank(folderId)){
				cincomFolderPath = getPathFromCmisId(CmisId.parse(folderId));
			}
			
			com.cincom.kmdata.client.api.search.ResultSet resultSet = searchWS.sendQuery(ctxt(), cincomFolderPath, parameters);
			
			
			//Convert results
			Collection<com.cincom.kmdata.client.api.search.Result> collection = resultSet.getResults();
			
			if(CollectionUtils.isEmpty(collection)){
				
			}else{
				for (com.cincom.kmdata.client.api.search.Result result : collection) {
				      Object object = result.getObject();
				      
				      if(object instanceof com.cincom.kmdata.client.api.search.Document){
				    	  NodeValueObject n = new NodeValueObject(object);
				    	  Document d = convertDocument(n);
				    	  configurePwc(d);
				    	  _checkedOutDocs.add(d);
				      }
				}
			}
		} catch (com.cincom.kmdata.client.api.search.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.search.RepositoryException_Exception e) {
			log.error("", e);
		}
		
		//Convert
		if (CollectionUtils.isNotEmpty(_checkedOutDocs)) {
			checkedOutDocs = new ArrayList<jp.aegif.nemaki.model.Document>(_checkedOutDocs);
		}
		return checkedOutDocs;
	}

	@Override
	public VersionSeries getVersionSeries(
			jp.aegif.nemaki.model.Document document) {
		if (document.getClass() == Document.class) {
			// Down casting
			Document d = (Document) document;
			VersionSeries vs = d.getVersionSeries();
			if (vs == null) {
				return getVersionSeries(document.getVersionSeriesId());
			} else {
				return d.getVersionSeries();
			}

		} else {
			return getVersionSeries(document.getVersionSeriesId());
		}
	}

	public VersionSeries getVersionSeriesInternal(Document document) {
		return getVersionSeries(document.getVersionSeriesId());
	}

	// VersionSeriesId is ceDocument's uuid by convention
	@Override
	public VersionSeries getVersionSeries(String versionSeriesId) {
		CmisId cmisId = CmisId.parse(versionSeriesId);

		if (!CincomConst.TYPE_VERSION.equals(cmisId.getType())) {
			return null;
		}

		if (StringUtils.isNotBlank(cmisId.getAttachment())) {
			return buildAttachedDocumentVersionSeries(cmisId);
		}

		// TODO Standard document
		try {
			String path = getPathFromCmisId(cmisId);
			if (StringUtils.isBlank(path)) {
				return null;
			}

			VersionSeries vs = new VersionSeries();
			vs.setId(versionSeriesId);

			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getDocument(ctxt(), path, null);
			LockInfo lockInfo = ceDocument.getLockInfo();

			if (lockInfo == null) {
				vs.setVersionSeriesCheckedOut(false);
			} else {
				String owner = lockInfo.getOwner();
				if (StringUtils.isBlank(owner)) {
					// TODO what is this ?
					vs.setVersionSeriesCheckedOut(false);
				} else {
					vs.setVersionSeriesCheckedOut(true);
					vs.setVersionSeriesCheckedOutBy(owner);

					CmisId pwcCmisId = CmisId.buildPwc(ceDocument.getUuid(),
							ceDocument.getActualVersion().getId());
					vs.setVersionSeriesCheckedOutId(pwcCmisId.value());
				}
			}

			return vs;
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		}

		// TODO Auto-generated method stub
		return null;
	}

	private VersionSeries buildAttachedDocumentVersionSeries(
			CmisId versionSeriesId) {
		VersionSeries vs = new VersionSeries();
		vs.setId(versionSeriesId.value());
		vs.setVersionSeriesCheckedOut(false);
		vs.setVersionSeriesCheckedOutBy(null);
		vs.setVersionSeriesCheckedOutId(null);
		return vs;
	}

	@Override
	public Folder getFolder(String objectId) {
		CmisId cmisId = CmisId.parse(objectId);
		String path = getPathFromCmisId(cmisId);

		if (path == null) {
			return null;
		} else {
			return getFolderByPath(path);
		}
	}

	private Folder getFolderByPath(String path) {
		try {
			if (serverWS.isFolder(ctxt(), path)) {
				com.cincom.kmdata.client.api.folder.Folder ceFolder = folderWS
						.getFolder(ctxt(), path, null);
				Folder folder = convertFolder(new NodeValueObject(ceFolder));
				return folder;
			}
		} catch (com.cincom.kmdata.client.api.server.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		} catch (InformationException_Exception e) {
			log.error("", e);
		} catch (ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public String calculatePath(Content content) {
		String path = "";
		if (rootObjectId.equals(content.getId())) {
			path = ROOT_CMIS_PATH;
		} else {
			CmisId cmisId = CmisId.parse(content.getId());
			path = getPathFromCmisId(cmisId);
		}
		return path;
	}

	// TODO implement
	@Override
	public Relationship getRelationship(String objectId) {
		CmisId cmisId = CmisId.parse(objectId);
		String sourcePath = getPathFromUuid(cmisId.getSourceId());

		try {
			List<Relation> ceRelations = relationWS.getRelations(ctxt(),
					sourcePath, "");
			for (Relation ceRelation : ceRelations) {
				String targetPath = ceRelation.getDestinationPath();
				String targetId = getUuidFormPath(targetPath);
				if (cmisId.getDetinationId().equals(targetId)) {

					// Build CMIS Relationship
					Relationship relationship = new Relationship();
					String typeId = CmisTypeId._buildRelation(ceRelation
							.getName());
					relationship.setObjectType(typeId);
					relationship.setId(objectId);
					relationship.setName(ceRelation.getLabel());
					relationship.setSourceId(cmisId.getSourceId());
					relationship.setTargetId(targetId);

					return relationship;
				}
			}

		} catch (com.cincom.kmdata.client.api.relation.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public List<Relationship> getRelationsipsOfObject(String objectId,
			RelationshipDirection relationshipDirection) {
		List<Relationship> result = new ArrayList<Relationship>();

		// TODO Auto-generated method stub
		CmisId cmisId = CmisId.parse(objectId);
		switch (relationshipDirection) {
		case SOURCE:
			RelationshipDirection.SOURCE.value();
			break;
		case TARGET:
			RelationshipDirection.TARGET.value();
			break;
		case EITHER:
			RelationshipDirection.EITHER.value();
			break;
		default:
			RelationshipDirection.SOURCE.value();
			break;
		}

		String path = getPathFromCmisId(cmisId);

		// System default relationships
		if (CincomConst.TYPE_DOCUMENT.equals(cmisId.getType())) {
			if (StringUtils.isEmpty(cmisId.getAttachment())) {
				result.addAll(buildHasAtachmentRelationshipsForMain(cmisId,
						path));
			} else {
				result.add(buildHasAttachmentRelationshipForAttached(cmisId));
			}
		}

		// Standard relationships
		result.addAll(buildRelationships(path));

		return result;
	}

	@Override
	public Policy getPolicy(String objectId) {
		throw new CmisNotSupportedException("getPolicy is not supported",
				BigInteger.valueOf(405));
	}

	@Override
	public Item getItem(String objectId) {
		throw new CmisNotSupportedException("getItem is not supported",
				BigInteger.valueOf(405));
	}

	@Override
	public Document createDocument(CallContext callContext,
			Properties properties, Folder parentFolder,
			ContentStream contentStream, VersioningState versioningState,
			String versionSeriesId) {

		com.cincom.kmdata.client.api.document.Document ceDocument = new com.cincom.kmdata.client.api.document.Document();

		// ObjectType information
		String objectTypeId = DataUtil.getObjectTypeId(properties);
		TypeDefinition typeDefinition = typeManager
				.getTypeDefinition(objectTypeId);

		// Properties
		Properties filtered = filterProperties(properties, typeDefinition, true);
		setDocumentProperties(ceDocument, filtered, typeDefinition);
		// path
		ceDocument.setPath(buildPathOnCreation(parentFolder,
				ceDocument.getName()));
		// version
		if (VersioningState.MAJOR == versioningState
				|| VersioningState.CHECKEDOUT == versioningState
				|| VersioningState.NONE == versioningState) {
			ceDocument.setMajorVersionId(1);
			ceDocument.setMinorVersionId(0);
		} else if (VersioningState.MINOR == versioningState) {
			ceDocument.setMajorVersionId(0);
			ceDocument.setMinorVersionId(1);
		}

		// Content
		try {
			byte[] content;
			content = readAll(contentStream.getStream());
			ceDocument.setContent(content);

			ceDocument.setSize(contentStream.getLength());
			ceDocument.setMimeType(contentStream.getMimeType());
			ceDocument.setFileName(contentStream.getFileName());
		} catch (IOException e) {
			log.error("", e);
		}

		// Mapping from cmis:document(if enabled)
		if (StringUtils.isNotBlank(mappedTypetoCmisDocument)
				&& NodeType.CMIS_DOCUMENT.value().equals(objectTypeId)) {
			ceDocument.setPropertyGroup(CmisTypeId.parse(
					mappedTypetoCmisDocument).getBaseId());
		}

		// Create
		try {
			com.cincom.kmdata.client.api.document.Document _result = documentWS
					.create(ctxt(), ceDocument);
			Document result = convertDocument(new NodeValueObject(_result));

			if (VersioningState.CHECKEDOUT == versioningState) {
				result = checkOut(callContext, result.getId(), null);
			}

			return result;
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemExistsException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.VersionException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.WorkflowException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public Document createDocumentFromSource(CallContext callContext,
			Properties properties, Folder target,
			jp.aegif.nemaki.model.Document original,
			VersioningState versioningState, List<String> policies,
			org.apache.chemistry.opencmis.commons.data.Acl addAces,
			org.apache.chemistry.opencmis.commons.data.Acl removeAces) {
		Document result = createDocumentFromSourceInternal(callContext,
				properties, target, original, versioningState, policies,
				addAces, removeAces);
		return result;
	}

	// TODO what if this method is called on old version?
	/**
	 * createDocumentFromSource method with CincomECM-customized document
	 * 
	 * @param callContext
	 * @param properties
	 * @param target
	 * @param original
	 * @param versioningState
	 * @param policies
	 * @param addAces
	 * @param removeAces
	 * @return
	 */
	private Document createDocumentFromSourceInternal(CallContext callContext,
			Properties properties, Folder target,
			jp.aegif.nemaki.model.Document original,
			VersioningState versioningState, List<String> policies,
			org.apache.chemistry.opencmis.commons.data.Acl addAces,
			org.apache.chemistry.opencmis.commons.data.Acl removeAces) {

		String sourcePath = calculatePath(original);
		String targetPath = calculatePath(target);
		try {
			com.cincom.kmdata.client.api.document.Document _result = documentWS
					.copy(ctxt(), sourcePath, targetPath);
			Document result = getDocument(_result.getUuid());
			return result;
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemExistsException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.WorkflowException_Exception e) {
			log.error("", e);
		}

		// TODO if properties is provided, modify copied document later on

		return null;
	}

	@Override
	public jp.aegif.nemaki.model.Document createDocumentWithNewStream(
			CallContext callContext, jp.aegif.nemaki.model.Document original,
			ContentStream contentStream) {
		return createDocumentWithNewStreamInternal(callContext, original,
				contentStream);
	}

	/**
	 * createDocumentWithNewStream method with CincomECM-customized document
	 * 
	 * @param callContext
	 * @param original
	 * @param contentStream
	 * @return
	 */
	public Document createDocumentWithNewStreamInternal(
			CallContext callContext, jp.aegif.nemaki.model.Document original,
			ContentStream contentStream) {
		CmisId cmisId = CmisId.parse(original.getId());
		String path = getPathFromCmisId(cmisId);

		try {
			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getDocument(ctxt(), path, cmisId.getVersion());

			if (contentStream != null) {
				byte[] bytes = readAll(contentStream.getStream());
				ceDocument.setContent(bytes);
			}

			com.cincom.kmdata.client.api.document.Document _result = documentWS
					.update(ctxt(), ceDocument);
			return convertDocument(new NodeValueObject(_result));

		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.VersionException_Exception e) {
			log.error("", e);
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document checkOut(CallContext callContext, String objectId,
			ExtensionsData extension) {
		CmisId cmisId = CmisId.parse(objectId);

		try {
			String path = getPathFromCmisId(cmisId);

			// CheckOut
			documentWS.checkout(ctxt(), path);

			// Return PWC
			com.cincom.kmdata.client.api.document.Document workingCopy = documentWS
					.getDocument(ctxt(), path, null);
			Document pwc = convertDocument(new NodeValueObject(workingCopy));
			configurePwc(pwc);
			return pwc;

		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * Read InputStream to put it into a byte array
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
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

	@Override
	public void cancelCheckOut(CallContext callContext, String objectId,
			ExtensionsData extension) {
		CmisId cmisId = CmisId.parse(objectId);
		String path = getPathFromCmisId(cmisId);

		try {
			// TODO need a test !
			documentWS.undoCheckout(ctxt(), path);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

	}

	@Override
	public Document checkIn(CallContext callContext, Holder<String> objectId,
			Boolean major, Properties properties, ContentStream contentStream,
			String checkinComment, List<String> policies,
			org.apache.chemistry.opencmis.commons.data.Acl addAces,
			org.apache.chemistry.opencmis.commons.data.Acl removeAces,
			ExtensionsData extension) {

		CmisId cmisId = CmisId.parse(objectId.getValue());
		String path = getPathFromCmisId(cmisId);
		boolean _major = (major == null) ? true : major;

		int versionType = _major ? CincomConst.VERSION_TYPE_MAJOR
				: CincomConst.VERSION_TYPE_MINOR;

		// TODO Where does checkInComment go ?
		try {
			Document pwc = getDocument(objectId.getValue());

			// Update properties
			TypeDefinition tdf = typeManager.getTypeDefinition(pwc);
			updateDocumentProperties(callContext, properties, path, tdf);

			// Update contentStream
			createDocumentWithNewStream(callContext, pwc, contentStream);

			// CheckIn
			String _checkInComment = StringUtils.isBlank(checkinComment) ? ""
					: checkinComment; // avoid nullPointerException
			documentWS.checkin(ctxt(), path, _checkInComment, versionType);

			// Return checkedIn document
			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getDocument(ctxt(), path, null);
			Document checkedIn = convertDocument(new NodeValueObject(ceDocument));
			objectId.setValue(checkedIn.getId());
			return checkedIn;

		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.VersionException_Exception e) {
			log.error("", e);
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public Folder createFolder(CallContext callContext, Properties properties,
			Folder parentFolder) {

		com.cincom.kmdata.client.api.folder.Folder ceFolder = new com.cincom.kmdata.client.api.folder.Folder();

		// ObjectType information
		String objectTypeId = DataUtil.getObjectTypeId(properties);
		TypeDefinition typeDefinition = typeManager
				.getTypeDefinition(objectTypeId);

		// Properties
		Properties filtered = filterProperties(properties, typeDefinition, true);
		setFolderProperties(ceFolder, filtered, typeDefinition);
		// Set CMIS properties from other than source properties
		ceFolder.setPath(buildPathOnCreation(parentFolder, ceFolder.getName()));

		// Mapping objecType from cmis:folder(if enabled)
		if (StringUtils.isNotBlank(mappedTypetoCmisFolder)
				&& NodeType.CMIS_FOLDER.value().equals(objectTypeId)) {
			ceFolder.setPropertyGroup(CmisTypeId.parse(mappedTypetoCmisFolder)
					.getBaseId());
		}

		// Create
		try {
			com.cincom.kmdata.client.api.folder.Folder _result = folderWS
					.create(ctxt(), ceFolder);
			return convertFolder(new NodeValueObject(_result));
		} catch (InformationException_Exception e) {
			log.error("", e);
		} catch (ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (RepositoryException_Exception e) {
			log.error("", e);
		} catch (AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (ItemExistsException_Exception e) {
			log.error("", e);
		} catch (WorkflowException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * 
	 * @param parentFolder
	 * @param name
	 * @return
	 */
	private String buildPathOnCreation(Content parentFolder, String name) {
		if (rootObjectId.equals(parentFolder.getId())) {
			return PATH_SEPARATOR + name;
		} else {
			return calculatePath(parentFolder) + PATH_SEPARATOR + name;
		}
	}

	@Override
	public Relationship createRelationship(CallContext callContext,
			Properties properties, List<String> policies,
			org.apache.chemistry.opencmis.commons.data.Acl addAces,
			org.apache.chemistry.opencmis.commons.data.Acl removeAces,
			ExtensionsData extension) {

		// Prepare
		String sourceId = DataUtil.getIdProperty(properties,
				PropertyIds.SOURCE_ID);
		CmisId _sourceId = CmisId.parse(sourceId);
		String sourcePath = getPathFromCmisId(_sourceId);
		String sourceVersion = _sourceId.getVersionWithoutPwc();

		String targetId = DataUtil.getIdProperty(properties,
				PropertyIds.TARGET_ID);
		CmisId _targetId = CmisId.parse(targetId);
		String tagetPath = getPathFromCmisId(_targetId);
		String targetVersion = _targetId.getVersionWithoutPwc();

		String relType = DataUtil.getIdProperty(properties,
				PropertyIds.OBJECT_TYPE_ID);
		CmisTypeId _relType = CmisTypeId.parse(relType);

		// Create
		Relation ceRelaion = new Relation();
		ceRelaion.setName(_relType.getBaseId());
		ceRelaion.setSourcePath(sourcePath);
		ceRelaion.setSourceVersion(sourceVersion);
		ceRelaion.setDestinationPath(tagetPath);
		ceRelaion.setDestinationVersion(targetVersion);

		Relation ceResult = null;
		try {
			ceResult = relationWS.addRelation(ctxt(), ceRelaion);
		} catch (com.cincom.kmdata.client.api.relation.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.RepositoryException_Exception e) {
			log.error("", e);
		}

		// Build the result
		Relationship result = convertRelationship(ceResult);
		return result;
	}

	@Override
	public Policy createPolicy(CallContext callContext, Properties properties,
			List<String> policies,
			org.apache.chemistry.opencmis.commons.data.Acl addAces,
			org.apache.chemistry.opencmis.commons.data.Acl removeAces,
			ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item createItem(CallContext callContext, Properties properties,
			String folderId, List<String> policies,
			org.apache.chemistry.opencmis.commons.data.Acl addAces,
			org.apache.chemistry.opencmis.commons.data.Acl removeAces,
			ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content update(Content content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content updateProperties(CallContext callContext,
			Properties properties, Content content) {

		String objectTypeId = content.getObjectType();
		TypeDefinition typeDefinition = typeManager
				.getTypeDefinition(objectTypeId);

		String path = calculatePath(content);
		if (NodeType.CMIS_FOLDER.value().equals(content.getType())) {
			// Folder case
			updateFolderProperties(callContext, properties, path,
					typeDefinition);
		} else if (NodeType.CMIS_DOCUMENT.value().equals(content.getType())) {
			// Document case
			updateDocumentProperties(callContext, properties, path,
					typeDefinition);

		}

		// TODO Auto-generated method stub
		return null;
	}

	private Folder updateFolderProperties(CallContext callContext,
			Properties properties, String path, TypeDefinition typeDefinition) {

		Properties filtered = filterProperties(properties, typeDefinition,
				false);

		try {
			com.cincom.kmdata.client.api.folder.Folder ceFolder = folderWS
					.getFolder(ctxt(), path, null);
			// Set values
			setFolderProperties(ceFolder, filtered, typeDefinition);

			// Execute
			com.cincom.kmdata.client.api.folder.Folder _result = folderWS
					.update(ctxt(), ceFolder);
			// This way to get result is OK?
			Folder result = convertFolder(new NodeValueObject(_result));
			return result;
		} catch (InformationException_Exception e) {
			log.error("", e);
		} catch (ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (RepositoryException_Exception e) {
			log.error("", e);
		} catch (AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (LockException_Exception e) {
			log.error("", e);
		} catch (VersionException_Exception e) {
			log.error("", e);
		}
		return null;
	}

	private Document updateDocumentProperties(CallContext callContext,
			Properties properties, String path, TypeDefinition typeDefinition) {
		Properties filtered = filterProperties(properties, typeDefinition,
				false);

		try {
			// TODO what if it is WorkingCopy ?
			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getDocument(ctxt(), path, null);
			// Set values
			setDocumentProperties(ceDocument, filtered, typeDefinition);

			// Execute
			com.cincom.kmdata.client.api.document.Document _result = documentWS
					.update(ctxt(), ceDocument);
			Document result = convertDocument(new NodeValueObject(_result));
			return result;
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.VersionException_Exception e) {
			log.error("", e);
		}
		return null;
	}

	private Properties filterProperties(Properties properties,
			TypeDefinition typeDefinition, boolean onCreate) {
		PropertiesImpl result = new PropertiesImpl();
		Map<String, PropertyDefinition<?>> definitions = typeDefinition
				.getPropertyDefinitions();

		for (Entry<String, PropertyData<?>> e : properties.getProperties()
				.entrySet()) {
			PropertyDefinition<?> definition = definitions.get(e.getKey());
			if (definition != null) {
				Updatability updatability = definition.getUpdatability();
				if (updatability == Updatability.READWRITE
						|| (onCreate && updatability == Updatability.ONCREATE)) {
					result.addProperty(e.getValue());
				}
			}
		}

		return result;
	}

	@Override
	public void move(Content content, Folder target) {
		try {
			if (content.isFolder()) {

				folderWS.move(ctxt(), calculatePath(content),
						calculatePath(target));
			} else if (content.isDocument()) {

				documentWS.move(ctxt(), calculatePath(content),
						calculatePath(target));
			}
		} catch (AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (InformationException_Exception e) {
			log.error("", e);
		} catch (ItemExistsException_Exception e) {
			log.error("", e);
		} catch (ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (RepositoryException_Exception e) {
			log.error("", e);
		} catch (WorkflowException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemExistsException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.WorkflowException_Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void applyPolicy(CallContext callContext, String policyId,
			String objectId, ExtensionsData extension) {
		throw new CmisNotSupportedException("applyPolicy is not supported",
				BigInteger.valueOf(405));

	}

	@Override
	public void removePolicy(CallContext callContext, String policyId,
			String objectId, ExtensionsData extension) {
		throw new CmisNotSupportedException("removePolicy is not supported",
				BigInteger.valueOf(405));
	}

	@Override
	public List<Policy> getAppliedPolicies(String objectId,
			ExtensionsData extension) {
		return new ArrayList<Policy>();
	}

	// TODO check if a folder contains any objects. User deleteTree.
	@Override
	public void delete(CallContext callContext, String objectId,
			Boolean deletedWithParent) {
		try {
			String uuid = CmisId.parse(objectId).getBaseId();
			String path = serverWS.getObjectPathFromUUID(ctxt(), uuid);

			if (serverWS.isFolder(ctxt(), path)) {
				folderWS.delete(ctxt(), path);
			} else if (serverWS.isDocument(ctxt(), path)) {

			}

		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.server.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (InformationException_Exception e) {
			log.error("", e);
		} catch (ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (LockException_Exception e) {
			log.error("", e);
		} catch (RepositoryException_Exception e) {
			log.error("", e);
		}

	}

	@Override
	public void deleteDocument(CallContext callContext, String objectId,
			Boolean allVersions, Boolean deleteWithParent) {
		// TODO redundant
		try {
			String uuid = CmisId.parse(objectId).getBaseId();
			String path = serverWS.getObjectPathFromUUID(ctxt(), uuid);

			if (serverWS.isDocument(ctxt(), path)) {
				documentWS.delete(ctxt(), path);
			}

		} catch (com.cincom.kmdata.client.api.server.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.server.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

	}

	@Override
	public void deleteAttachment(CallContext callContext, String attachmentId) {
		CmisId cmisId = CmisId.parse(attachmentId);
		cmisId.setType(CincomConst.TYPE_DOCUMENT);
		String path = getPathFromCmisId(cmisId);

		try {
			com.cincom.kmdata.client.api.document.Document ceDocument = documentWS
					.getPropertiesByVersion(ctxt(), path, cmisId.getVersion());
			ceDocument.setDeleteMasterFile(true);
			documentWS.update(ctxt(), ceDocument);
			documentWS.checkin(ctxt(), path, "", CincomConst.VERSION_TYPE_NONE);
		} catch (com.cincom.kmdata.client.api.document.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.LockException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.VersionException_Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void deleteContentStream(CallContext callContext,
			Holder<String> objectId) {
		Document document = getDocument(objectId.getValue());
		deleteAttachment(callContext, document.getAttachmentNodeId());
	}

	@Override
	public List<String> deleteTree(CallContext context, String folderId,
			Boolean allVersions, Boolean continueOnFailure,
			Boolean deletedWithParent) {
		delete(context, folderId, null);

		// This method should return failure data, but it's difficult in
		// CincomECM
		return null;
	}

	@Override
	public AttachmentNode getAttachment(String attachmentId) {
		CmisId cmisId = CmisId.parse(attachmentId);
		Map<String, Object> map = getAttachmentInternal(cmisId);
		AttachmentNode an = (AttachmentNode) map.get(TOKEN_ATTACHMENT_NODE);

		try {
			byte[] attachmentByte = null;

			if (StringUtils.isBlank(cmisId.getAttachment())) {
				// NormalDocument
				attachmentByte = documentWS.getContentByVersion(ctxt(),
						(String) map.get(TOKEN_PATH), cmisId.getVersion());
			} else {
				// AttachedDocument
				com.cincom.kmdata.client.api.relation.Attachment ceAttachment = relationWS
						.getAttachmentByVersion(ctxt(),
								(String) map.get(TOKEN_PATH),
								cmisId.getVersion(), cmisId.getAttachment());
				attachmentByte = ceAttachment.getContent();
			}

			ByteArrayInputStream bis = new ByteArrayInputStream(attachmentByte);
			an.setInputStream(bis);
			return an;
		} catch (IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.InformationException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.AccessDeniedException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.IOException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.relation.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public AttachmentNode getAttachmentRef(String attachmentId) {
		CmisId cmisId = CmisId.parse(attachmentId);
		Map<String, Object> map = getAttachmentInternal(cmisId);
		AttachmentNode an = (AttachmentNode) map.get(TOKEN_ATTACHMENT_NODE);
		return an;
	}

	@Override
	public void appendAttachment(CallContext callContext,
			Holder<String> objectId, Holder<String> changeToken,
			ContentStream contentStream, boolean isLastChunk,
			ExtensionsData extension) {
		throw new CmisNotSupportedException(
				"appendContentStream is not supported", BigInteger.valueOf(405));

	}

	@Override
	public Rendition getRendition(String streamId) {
		CmisId cmisId = CmisId.parse(streamId);
		cmisId.setType(CincomConst.TYPE_DOCUMENT);

		try {
			// TODO check in advance isHasThumbNail
			byte[] bytes = documentWS.getThumbnailByVersion(ctxt(),
					getPathFromCmisId(cmisId), cmisId.getVersion());
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

			Rendition rd = new Rendition();

			// TODO null check
			rd.setId(streamId);
			rd.setInputStream(bis);
			rd.setMimetype("image/jpeg");
			rd.setLength(bis.available());
			// FIXME hard-coded
			rd.setKind(RenditionKind.CMIS_THUMBNAIL.value());
			rd.setTitle(streamId);

			return rd;

		} catch (com.cincom.kmdata.client.api.document.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.document.RepositoryException_Exception e) {
			log.error("", e);
		}

		return null;
	}

	@Override
	public List<Rendition> getRenditions(String objectId) {
		// Now a document has only up to one rendition
		CmisId cmisId = CmisId.parse(objectId);
		cmisId.setType(CincomConst.TYPE_THUMBNAIL);
		Rendition rendition = getRendition(cmisId.value());

		List<Rendition> result = new ArrayList<Rendition>();
		if (rendition != null) {
			result.add(rendition);
		}

		return result;
	}

	@Override
	public Acl calculateAcl(Content content) {
		return content.getAcl();
	}

	@Override
	public Change getChangeEvent(String token) {
		
		XMLGregorianCalendar xcal = null;
		try {
			xcal = convertMilliSecondsToCalendar(token);
		} catch (Exception e) {
			log.error("", e);
		}
		
		try {
			List<DocumentHistory> result = statisticWS.getHistoryLogByDateTime(ctxt(), xcal, 0, 1, "", false);
			if(CollectionUtils.isNotEmpty(result)){
				DocumentHistory dh = result.get(0);
				Change ch = convertChange(dh);
				return ch;
			}
		} catch (com.cincom.kmdata.client.api.statistic.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.statistic.RepositoryException_Exception e) {
			log.error("", e);
		}
		
		return null;
	}

	@Override
	public List<Change> getLatestChanges(CallContext context,
			Holder<String> changeLogToken, Boolean includeProperties,
			String filter, Boolean includePolicyIds, Boolean includeAcl,
			BigInteger maxItems, ExtensionsData extension) {

		List<Change> result = new ArrayList<Change>();
		
		String _changeToken = changeLogToken.getValue();
		List<DocumentHistory> histories = new ArrayList<DocumentHistory>();
		
		if(StringUtils.isBlank(_changeToken)){
			try {
				histories = 
						statisticWS.getHistoryLog(ctxt(), "", 0, maxItems.intValue(), "", true);
			} catch (com.cincom.kmdata.client.api.statistic.ItemNotFoundException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.statistic.RepositoryException_Exception e) {
				log.error("", e);
			}
		}else{
			XMLGregorianCalendar xcal = convertMilliSecondsToCalendar(_changeToken);
			try {
				histories = 
						statisticWS.getHistoryLogByDateTime(ctxt(), xcal, 0, maxItems.intValue(), "", true);
				
			} catch (com.cincom.kmdata.client.api.statistic.ItemNotFoundException_Exception e) {
				log.error("", e);
			} catch (com.cincom.kmdata.client.api.statistic.RepositoryException_Exception e) {
				log.error("", e);
			}
		}
		
		for (DocumentHistory dh : histories) {
			Change ch = convertChange(dh);
			if(ch.getChangeType() == null){
				continue;
			}
			result.add(ch);
		}

		return result;
	}

	@Override
	public String getLatestChangeToken() {
		String latestChangeToken = null;

		try {
			List<DocumentHistory> latest = statisticWS.getHistoryLog(ctxt(),
					null, 0, 1, "", false);
			if (CollectionUtils.isNotEmpty(latest)) {
				DocumentHistory dh = latest.get(0);
				latestChangeToken = String.valueOf(dh.getDate()
						.toGregorianCalendar().getTimeInMillis());
			}
		} catch (com.cincom.kmdata.client.api.statistic.ItemNotFoundException_Exception e) {
			log.error("", e);
		} catch (com.cincom.kmdata.client.api.statistic.RepositoryException_Exception e) {
			log.error("", e);
		}

		return latestChangeToken;
	}

	@Override
	public List<Archive> getAllArchives() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Archive getArchive(String archiveId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Archive getArchiveByOriginalId(String archiveId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Archive createArchive(CallContext callContext, String objectId,
			Boolean deletedWithParent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Archive createAttachmentArchive(CallContext callContext,
			String attachmentId) {
		// TODO Auto-generated method stub
		return null;
	}

	// TODO divide class
	@Override
	public void restoreArchive(String archiveId) {
		// TODO Auto-generated method stub

	}

	public void setCincomManager(CincomManager cincomManager) {
		this.cincomManager = cincomManager;
	}

	public void setTypeManager(TypeManager typeManager) {
		this.typeManager = typeManager;
	}

	public void setRootObjectId(String rootObjectId) {
		this.rootObjectId = rootObjectId;
	}

	public void setPropertyManager(NemakiPropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}
}
