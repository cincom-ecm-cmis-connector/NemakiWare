package jp.aegif.nemaki.cincom.model;

import java.util.List;

import jp.aegif.nemaki.model.Property;

public class CincomDocumentDTO {
	private String uuid;
	private String name; 
	private String path;
	private String propertyGroup;
	private javax.xml.datatype.XMLGregorianCalendar creationDate;
	private String createdBy; 
	private javax.xml.datatype.XMLGregorianCalendar modificationDate;
	private String modifiedBy;
	private String versionLabel;
	private boolean latestVersion;
	private long majorVersion;
	private long minorVersion;
	private boolean content;
	
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
	public javax.xml.datatype.XMLGregorianCalendar getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(javax.xml.datatype.XMLGregorianCalendar creationDate) {
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
	public boolean isLatestVersion() {
		return latestVersion;
	}
	public void setLatestVersion(boolean latestVersion) {
		this.latestVersion = latestVersion;
	}
	public long getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(long majorVersion) {
		this.majorVersion = majorVersion;
	}
	public long getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(long minorVersion) {
		this.minorVersion = minorVersion;
	}
	public boolean hasContent() {
		return content;
	}
	public void setHasContent(boolean content) {
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
	
	
	
	
	
}
