package jp.aegif.nemaki.cincom.model;

import java.util.ArrayList;
import java.util.List;

import jp.aegif.nemaki.cincom.util.constant.CincomConst;
import jp.aegif.nemaki.model.Relationship;

import org.apache.commons.lang.StringUtils;

public class CmisId {
	private String baseId;
	private String type;
	private String version;
	private String attachment;
	private String sourceId;
	private String detinationId;
	private String relationType;

	private final String SEPARATOR = "_";

	public CmisId() {

	}

	private CmisId(String cmisId) {
		// Only Root folder has "/"
		// FIXME hard-coded
		if ("/".equals(cmisId)) {
			setType(CincomConst.TYPE_FOLDER);
			setBaseId("/");
			return;
		}

		String[] splitted = StringUtils.split(cmisId, SEPARATOR);
		setType(splitted[0]);
		if (CincomConst.TYPE_DOCUMENT.equals(type)
				|| CincomConst.TYPE_FOLDER.equals(type)
				|| CincomConst.TYPE_CONTENT.equals(type)
				|| CincomConst.TYPE_THUMBNAIL.equals(type)
				|| CincomConst.TYPE_VERSION.equals(type)) {

			if (splitted.length > 1) {
				setBaseId(splitted[1]);
			} else {
				// TODO logging
			}

			if (splitted.length > 2) {
				setVersion(splitted[2]);
			}

			if (splitted.length > 3) {
				setAttachment(splitted[3]);
			}

		} else if (CincomConst.TYPE_RELATION.equals(type)) {
			if (splitted.length > 1) {
				setSourceId(splitted[1]);
			}

			if (splitted.length > 2) {
				setDetinationId(splitted[2]);
			}

			if (splitted.length > 3) {
				setRelationType(splitted[3]);
			}
		}
	}

	public CmisId(String type, String baseId, String versionId,
			String attachmentSuffix) {
		setType(type);
		setBaseId(baseId);
		setVersion(versionId);
		setAttachment(attachmentSuffix);
	}

	// TODO add _buildDocument method to return String directly
	public static CmisId buildDocument(String uuid, String versionId,
			String attachmentId) {
		return new CmisId(CincomConst.TYPE_DOCUMENT, uuid, versionId,
				attachmentId);
	}

	public static String _buildDocument(String uuid, String versionId,
			String attachmentId) {
		return buildDocument(uuid, versionId, attachmentId).value();
	}

	public static CmisId buildPwc(String uuid, String versionIdWithoutPwc) {
		CmisId cmisId = new CmisId(CincomConst.TYPE_DOCUMENT, uuid,
				versionIdWithoutPwc + ".pwc", null);
		return cmisId;
	}
	
	public static String _buildPwc(String uuid, String versionIdWithoutPwc) {
		return buildPwc(uuid, versionIdWithoutPwc).value();
	}

	public static CmisId buildVersionSeries(String uuid, String versionId,
			String attachmentId) {
		return new CmisId(CincomConst.TYPE_VERSION, uuid, versionId,
				attachmentId);
	}

	public static String _buildVersionSeries(String uuid, String versionId,
			String attachmentId) {
		return buildVersionSeries(uuid, versionId, attachmentId).value();
	}

	public static CmisId buildContent(String uuid, String versionId,
			String attachmentId) {
		return new CmisId(CincomConst.TYPE_CONTENT, uuid, versionId,
				attachmentId);
	}

	public static String _buildContent(String uuid, String versionId,
			String attachmentId) {
		return buildContent(uuid, versionId, attachmentId).value();
	}

	public static CmisId buildThumbNail(String uuid, String versionId) {
		return new CmisId(CincomConst.TYPE_THUMBNAIL, uuid, versionId, null);
	}

	public static String _buildThumbNail(String uuid, String versionId) {
		return buildThumbNail(uuid, versionId).value();
	}

	public static CmisId buildFolder(String uuid) {
		return new CmisId(CincomConst.TYPE_FOLDER, uuid, null, null);
	}

	public static String _buildFolder(String uuid) {
		return buildFolder(uuid).value();
	}

	public static CmisId buildChange(String objectId) {
		CmisId cmisId = CmisId.parse(objectId);
		CmisId change = new CmisId(CincomConst.TYPE_CHANGE, cmisId.getBaseId(),
				cmisId.getVersion(), cmisId.getAttachment());
		return change;
	}

	public static String _buildChange(String objectId) {
		return buildChange(objectId).value();
	}

	public static CmisId buildRelationship(String sourceId,
			String destinationId, String relType) {
		CmisId cmisId = new CmisId();

		cmisId.setType(CincomConst.TYPE_RELATION);
		cmisId.setSourceId(sourceId);
		cmisId.setDetinationId(destinationId);
		cmisId.setRelationType(relType);

		return cmisId;
	}

	public static CmisId buildRelationship(Relationship relationship,
			String relType) {
		return buildRelationship(relationship.getSourceId(),
				relationship.getTargetId(), relType);
	}

	public static String _buildRelationship(String sourceId,
			String destinationId, String relType) {
		return buildRelationship(sourceId, destinationId, relType).value();
	}

	public static String _buildRelationship(Relationship relationship,
			String relType) {
		return _buildRelationship(relationship.getSourceId(),
				relationship.getTargetId(), relType);
	}

	public static CmisId parse(String objectId) {
		return new CmisId(objectId);
	}

	public String getBaseId() {
		return baseId;
	}

	public void setBaseId(String baseId) {
		this.baseId = baseId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getDetinationId() {
		return detinationId;
	}

	public void setDetinationId(String detinationId) {
		this.detinationId = detinationId;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public boolean isPwc() {
		return StringUtils.isNotBlank(version) && version.endsWith(".pwc");
	}

	public String getVersionWithoutPwc() {
		if (isPwc()) {
			return version.substring(0, version.length() - ".pwc".length());
		} else {
			return getVersion();
		}
	}

	public String getVersionWitPwc() {
		return version + ".pwc";
	}

	// TODO error logging
	public String value() {
		List<String> l = new ArrayList<String>();

		String type = getType();
		if (StringUtils.isNotBlank(getType())) {
			l.add(type);
		}

		if (CincomConst.TYPE_DOCUMENT.equals(type)
				|| CincomConst.TYPE_FOLDER.equals(type)
				|| CincomConst.TYPE_CONTENT.equals(type)
				|| CincomConst.TYPE_THUMBNAIL.equals(type)
				|| CincomConst.TYPE_VERSION.equals(type)
				|| CincomConst.TYPE_CHANGE.equals(type)) {

			if (StringUtils.isNotBlank(getBaseId())) {
				l.add(getBaseId());
			}
			if (StringUtils.isNotBlank(getVersion())) {
				l.add(getVersion());
			}
			if (StringUtils.isNotBlank(getAttachment())) {
				l.add(getAttachment());
			}

		} else if (CincomConst.TYPE_RELATION.equals(type)) {
			if (StringUtils.isNotBlank(getSourceId())) {
				l.add(getSourceId());
			}
			if (StringUtils.isNotBlank(getDetinationId())) {
				l.add(getDetinationId());
			}
			if (StringUtils.isNotBlank(getRelationType())) {
				l.add(getRelationType());
			}
		}

		return StringUtils.join(l, SEPARATOR);
	}

}
