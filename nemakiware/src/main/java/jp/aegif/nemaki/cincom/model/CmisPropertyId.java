package jp.aegif.nemaki.cincom.model;

import java.util.ArrayList;
import java.util.List;

import jp.aegif.nemaki.cincom.util.constant.CincomConst;

import org.apache.commons.lang.StringUtils;

public class CmisPropertyId {
	
	private String prefix;
	private String baseId;
	private String typeId;
	
	public CmisPropertyId(){
		
	}
	
	public CmisPropertyId(String typeId, String baseId){
		setTypeId(typeId);
		setBaseId(baseId);
	}

	public static CmisPropertyId build(String typeId, String baseId){
		CmisPropertyId cmisPropertyId = new CmisPropertyId(typeId, baseId);
		cmisPropertyId.setPrefix(CincomConst.PREFIX);
		return cmisPropertyId;
	}
	
	public static String _build(String typeId, String baseId){
		return build(typeId, baseId).value();
	}

	public static CmisPropertyId buildSystem(String baseId){
		CmisPropertyId cmisPropertyId = new CmisPropertyId(null, baseId);
		cmisPropertyId.setPrefix(CincomConst.PREFIX_SYSTEM);
		return cmisPropertyId;
	}
	
	public static String _buildSystem(String baseId){
		return buildSystem(baseId).valueWithSystem();
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
	
	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String value(){
		List<String> tmp = new ArrayList<String>();
		tmp.add(getPrefix() + getTypeId());
		tmp.add(getBaseId());
		return StringUtils.join(tmp, CincomConst.ID_SEPARATOR);
	}

	public String valueWithSystem(){
		return CincomConst.PREFIX_SYSTEM + getBaseId();
	}
	
	public static String extractBaseId(String cmisId, String typeId){
		return cmisId.replaceFirst(CincomConst.PREFIX + typeId + CincomConst.ID_SEPARATOR, "");
	}
}
