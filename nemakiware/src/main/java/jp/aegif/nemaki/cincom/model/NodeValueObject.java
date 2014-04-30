package jp.aegif.nemaki.cincom.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import jp.aegif.nemaki.cincom.util.constant.CincomConst;
import jp.aegif.nemaki.model.Property;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

public class NodeValueObject {

	/**
	 * Constructor calling getter methods by reflection
	 * 
	 * @param source
	 */
	public NodeValueObject(Object source) {
		// Uuid
		String uuid = ReflectionUtils.invoke(source, "getUuid");
		setUuid(uuid);

		// Name
		String name = ReflectionUtils.invoke(source, "getName");
		setName(name);

		// Path
		String path = ReflectionUtils.invoke(source, "getPath");
		setPath(path);

		// PropertyGroup
		String propertyGroup = ReflectionUtils.invoke(source,
				"getPropertyGroup");
		setPropertyGroup(propertyGroup);

		// PropertyGroups
		if (isFolder(source)) {
			List<String> propertyGroups = ReflectionUtils.invoke(source,
					"getPropertyGroups");
			setPropertyGroups(propertyGroups);
		}

		// CreationDate
		XMLGregorianCalendar creationDate = ReflectionUtils.invoke(source,
				"getCreationDate");
		setCreationDate(creationDate);

		// CreatedBy
		String createdBy = ReflectionUtils.invoke(source, "getCreatedBy");
		setCreatedBy(createdBy);

		// ModificationDate
		XMLGregorianCalendar modificationDate = ReflectionUtils.invoke(source,
				"getModificationDate");
		setModificationDate(modificationDate);

		// ModifiedBy
		String modifiedBy = ReflectionUtils.invoke(source, "getModifiedBy");
		setModifiedBy(modifiedBy);

		// ProcessId
		Long processId = ReflectionUtils.invoke(source, "getProcessId");
		setProcessId(processId);

		// ProcessList
		List<String> processList = ReflectionUtils.invoke(source,
				"getProcessList");
		setProcessList(processList);

		// ProcessTaskId
		Long processTaskId = ReflectionUtils.invoke(source, "getProcessTaskId");
		setProcessTaskId(processTaskId);

		// Comments
		List<?> comments = ReflectionUtils.invoke(source, "getComments");
		setComments(convertComments(comments));

		// Properties
		Object properties = ReflectionUtils.invoke(source, "getProperties");
		setProperties(convertProperties(properties));

		// Document specific
		if (isDocument(source)) {
			// MajorVersionId
			Long majorVersionId = ReflectionUtils.invoke(source,
					"getMajorVersionId");
			setMajorVersionId(majorVersionId);

			// MinorVersionId
			Long minorVersionId = ReflectionUtils.invoke(source,
					"getMinorVersionId");
			setMinorVersionId(minorVersionId);

			// LatestVersion
			Boolean existAcualVersion = (ReflectionUtils.invoke(source,
					"getActualVersion") != null);
			setLatestVersion(existAcualVersion);

			// HasContent
			Boolean isHasContent = ReflectionUtils.invoke(source,
					"isHasContent");
			setHasContent(isHasContent);
		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// Type check
	// /////////////////////////////////////////////////////////////////////////////////////
	private boolean isFolder(Object object) {
		return (object instanceof com.cincom.kmdata.client.api.search.Folder || object instanceof com.cincom.kmdata.client.api.folder.Folder);
	}

	private boolean isDocument(Object object) {
		return (object instanceof com.cincom.kmdata.client.api.search.Document
				|| object instanceof com.cincom.kmdata.client.api.folder.Document || object instanceof com.cincom.kmdata.client.api.document.Document);
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// Convert Properties
	// /////////////////////////////////////////////////////////////////////////////////////
	private List<Property> convertProperties(Object properties) {
		List<Property> _properties = new ArrayList<Property>();

		if (properties == null) {
			return null;
		}

		List<?> entries = new ArrayList();
		// Type check
		if (properties instanceof com.cincom.kmdata.client.api.search.Folder.Properties) {
			entries = ((com.cincom.kmdata.client.api.search.Folder.Properties) properties)
					.getEntry();
		} else if (properties instanceof com.cincom.kmdata.client.api.folder.Folder.Properties) {
			entries = ((com.cincom.kmdata.client.api.folder.Folder.Properties) properties)
					.getEntry();
		} else if (properties instanceof com.cincom.kmdata.client.api.folder.Document.Properties) {
			entries = ((com.cincom.kmdata.client.api.folder.Document.Properties) properties)
					.getEntry();
		} else if (properties instanceof com.cincom.kmdata.client.api.document.Document.Properties) {
			entries = ((com.cincom.kmdata.client.api.document.Document.Properties) properties)
					.getEntry();
		}

		// Convert
		if (CollectionUtils.isNotEmpty(entries)) {
			for (Object e : entries) {
				Property p = null;

				String key = (String) ReflectionUtils.invoke(e, "getKey");
				Object value = ReflectionUtils.invoke(e, "getValue");
				p = new Property(key, value);
				_properties.add(p);
			}
		}

		return _properties;
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// Convert Comments
	// /////////////////////////////////////////////////////////////////////////////////////
	private List<String> convertComments(List<?> comments) {
		List<String> _comments = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(comments)) {
			for (Object c : comments) {
				// Type check
				if (c instanceof com.cincom.kmdata.client.api.search.Comment
						|| c instanceof com.cincom.kmdata.client.api.folder.Comment) {

					// Convert
					long identifier = (Long)ReflectionUtils.invoke(c, "getIdentifier");
					String text = ReflectionUtils.invoke(c, "getText");
					XMLGregorianCalendar publicationDate = ReflectionUtils
							.invoke(c, "getPublicationDate");
					String publishedBy = ReflectionUtils.invoke(c,
							"getPublishedBy");
					addComment(_comments, identifier, text, publicationDate,
							publishedBy);
				}
			}
		}
		return _comments;
	}

	@SuppressWarnings("unchecked")
	private void addComment(List<String> comments, long identifier,
			String text, XMLGregorianCalendar publicationDate,
			String publishedBy) {
		JSONObject comment = new JSONObject();
		comment.put(CincomConst.PROP_NAME_COMMENTS_IDENTIFIER, identifier);
		comment.put(CincomConst.PROP_NAME_COMMENTS_TEXT, text);
		comment.put(CincomConst.PROP_NAME_COMMENTS_PUBLICATIONDATE,
				publicationDate.toXMLFormat());
		comment.put(CincomConst.PROP_NAME_COMMENTS_PUBLISHEDBY, publishedBy);
		comments.add(comment.toJSONString());
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// Utility
	// /////////////////////////////////////////////////////////////////////////////////////
	private static class ReflectionUtils {

		// TODO logging but pass through errors
		@SuppressWarnings("unchecked")
		public static <T> T invoke(Object target, String methodName,
				Object... args) {

			try {
				Class<?>[] argsClasses = resolveClasses(args);
				Method m = target.getClass().getMethod(methodName, argsClasses);
				return (T) m.invoke(target, args);
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

			return null;

		}

		private static Class<?>[] resolveClasses(Object... args) {
			if (args == null)
				return new Class[0];
			List<Class<?>> argsClassesList = new ArrayList<Class<?>>();
			for (Object arg : args) {
				argsClassesList.add(arg.getClass());
			}
			return argsClassesList.toArray(new Class<?>[0]);
		}

	}
	
	

	// /////////////////////////////////////////////////////////////////////////////////////
	// POJO definition
	// /////////////////////////////////////////////////////////////////////////////////////
	private String uuid;
	private String name;
	private String path;
	private String propertyGroup;
	private List<String> propertyGroups;
	private javax.xml.datatype.XMLGregorianCalendar creationDate;
	private String createdBy;
	private javax.xml.datatype.XMLGregorianCalendar modificationDate;
	private String modifiedBy;
	private String versionLabel;
	private Boolean latestVersion;
	private Long majorVersionId;
	private Long minorVersionId;
	private Boolean content;

	private Long processId;
	private List<String> processList;
	private Long processTaskId;
	private List<String> comments;

	private List<Property> properties;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPropertyGroup() {
		return propertyGroup;
	}

	public void setPropertyGroup(String propertyGroup) {
		this.propertyGroup = propertyGroup;
	}

	public List<String> getPropertyGroups() {
		return propertyGroups;
	}

	public void setPropertyGroups(List<String> propertyGroups) {
		this.propertyGroups = propertyGroups;
	}

	public javax.xml.datatype.XMLGregorianCalendar getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(
			javax.xml.datatype.XMLGregorianCalendar creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public javax.xml.datatype.XMLGregorianCalendar getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(
			javax.xml.datatype.XMLGregorianCalendar modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public String getVersionLabel() {
		return versionLabel;
	}

	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}

	public Boolean isLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(Boolean latestVersion) {
		this.latestVersion = latestVersion;
	}

	public Long getMajorVersionId() {
		return majorVersionId;
	}

	public void setMajorVersionId(Long majorVersionId) {
		this.majorVersionId = majorVersionId;
	}

	public Long getMinorVersionId() {
		return minorVersionId;
	}

	public void setMinorVersionId(Long minorVersionId) {
		this.minorVersionId = minorVersionId;
	}

	public Boolean hasContent() {
		return content;
	}

	public void setHasContent(Boolean content) {
		this.content = content;
	}

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public List<String> getProcessList() {
		return processList;
	}

	public void setProcessList(List<String> processList) {
		this.processList = processList;
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	private static final Log log = LogFactory.getLog(NodeValueObject.class);

}
