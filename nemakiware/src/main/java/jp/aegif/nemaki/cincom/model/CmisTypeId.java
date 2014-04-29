package jp.aegif.nemaki.cincom.model;

import jp.aegif.nemaki.cincom.model.constant.CincomConst;

public class CmisTypeId {
	private String prefix;
	private String baseId;
	
	public CmisTypeId(){
		
	}
	
	public CmisTypeId(String baseId){
		setBaseId(baseId);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getBaseId() {
		return baseId;
	}

	public void setBaseId(String baseId) {
		this.baseId = baseId;
	}
	
	public static CmisTypeId parse(String cmisTypeId){
		int colonPos = cmisTypeId.indexOf(":");
		String prefix = cmisTypeId.substring(0, colonPos);
		String base = cmisTypeId.substring(colonPos + 1, cmisTypeId.length());
		
		CmisTypeId result = new CmisTypeId();
		result.setPrefix(prefix);
		result.setBaseId(base);
		
		return result;
	}
	
	public static CmisTypeId build(String typename){
		CmisTypeId cmisTypeId = new CmisTypeId(typename);
		cmisTypeId.setPrefix(CincomConst.PREFIX);
		return cmisTypeId;
	}
	
	public static CmisTypeId buildSystem(String typename){
		CmisTypeId cmisTypeId = new CmisTypeId(typename);
		cmisTypeId.setPrefix(CincomConst.PREFIX_SYSTEM);
		return cmisTypeId;
	}
	
	public static CmisTypeId buildRelation(String typename){
		CmisTypeId cmisTypeId = new CmisTypeId(typename);
		cmisTypeId.setPrefix(CincomConst.PREFIX_RELATION);
		return cmisTypeId;
	}

	
	public static String _build(String typename){
		return build(typename).value();
	}
	
	public static String _buildSystem(String typename){
		return buildSystem(typename).value();
	}
	
	public static String _buildRelation(String typename){
		return buildRelation(typename).value();
	}
	
	public String value(){
		return getPrefix() + getBaseId();
	}
	
	public static String extractBaseId(String cmisTypeId){
		return cmisTypeId.replaceFirst(CincomConst.PREFIX, "");
	}
}
