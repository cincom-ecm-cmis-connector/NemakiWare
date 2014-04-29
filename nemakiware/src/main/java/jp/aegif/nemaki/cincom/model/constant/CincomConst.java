package jp.aegif.nemaki.cincom.model.constant;

public interface CincomConst{
	//For queryName, do no use "."
	public final String PREFIX = "cincom:";
	public final String PREFIX_SYSTEM = "cincom_system:";
	public final String PREFIX_RELATION = "cincom_relation:";
	public final String ID_SEPARATOR = ".";
	
	public final String TYPE_DOCUMENT = "document";
	public final String TYPE_FOLDER = "folder";
	public final String TYPE_RELATION = "relation";
	public final String TYPE_CONTENT = "content";
	public final String TYPE_THUMBNAIL = "thumbnail";
	public final String TYPE_VERSION = "version";
	public final String TYPE_CHANGE = "change";
	
	public final String DOCTYPE_NAME_ATTACHMENT = "attachment";
	public final String DOCTYPE_ID_ATTACHMENT = PREFIX_SYSTEM + DOCTYPE_NAME_ATTACHMENT;
	
	public final String RELTYPE_NAME_RELATIONSHIP = "relationship";
	public final String RELTYPE_ID_RELATIONSHIP = PREFIX_SYSTEM + RELTYPE_NAME_RELATIONSHIP;
	
	
	public final String RELTYPE_NAME_HAS_ATTACHMENT = "hasAttachment";
	public final String RELTYPE_ID_HAS_ATTACHMENT = PREFIX_SYSTEM + RELTYPE_NAME_HAS_ATTACHMENT;
	
	
	public final String PROP_NAME_RECIPROCAL = "reciprocal";
	public final String PROP_ID_RECIPROCAL = PREFIX_SYSTEM + PROP_NAME_RECIPROCAL;
	
	
	public final String PROP_NAME_PROCESSID = "processId";
	public final String PROP_NAME_PROCESSLIST = "processList";
	public final String PROP_NAME_PROCESSTASKID = "processTaskId";
	public final String PROP_NAME_COMMENTS = "comments";
	public final String PROP_NAME_COMMENTS_IDENTIFIER = "identifier";
	public final String PROP_NAME_COMMENTS_TEXT = "text";
	public final String PROP_NAME_COMMENTS_PUBLICATIONDATE = "publicationDate";
	public final String PROP_NAME_COMMENTS_PUBLISHEDBY = "publishedBy";
	
	public final String ACTION_CREATE="Create";
	public final String ACTION_UPATE="Update";
	public final String ACTION_DELETE="Delete";
	
	public final int VERSION_TYPE_MAJOR = 2;
	public final int VERSION_TYPE_MINOR = 1;
	public final int VERSION_TYPE_NONE = 0;
	
	public final String VERSION_FROZEN_COPY = "frozenCopy";
	
	public final String QUERY_TOKEN_QUERY = "query";
	public final String QUERY_TOKEN_ID = "id";
}
