package jp.aegif.nemaki.cincom.service.node.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jp.aegif.nemaki.cincom.model.CmisPropertyId;
import jp.aegif.nemaki.cincom.model.CmisTypeId;
import jp.aegif.nemaki.cincom.model.constant.CincomConst;
import jp.aegif.nemaki.cincom.model.constant.CincomPropertyKey;
import jp.aegif.nemaki.cincom.shared.CincomManager;
import jp.aegif.nemaki.model.Choice;
import jp.aegif.nemaki.model.NemakiPropertyDefinition;
import jp.aegif.nemaki.model.NemakiPropertyDefinitionCore;
import jp.aegif.nemaki.model.NemakiPropertyDefinitionDetail;
import jp.aegif.nemaki.model.NemakiTypeDefinition;
import jp.aegif.nemaki.model.constant.NodeType;
import jp.aegif.nemaki.model.constant.PropertyKey;
import jp.aegif.nemaki.service.node.TypeService;
import jp.aegif.nemaki.util.NemakiPropertyManager;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cincom.kmdata.client.api.property.Property.CheckBoxLabels;
import com.cincom.kmdata.client.api.property.Property.Label;
import com.cincom.kmdata.client.api.property.Property.Label.Entry;
import com.cincom.kmdata.client.api.property.Property.RadioLabels;
import com.cincom.kmdata.client.api.property.PropertyGroup;
import com.cincom.kmdata.client.api.property.PropertyWS;
import com.cincom.kmdata.client.api.relation.RelationWS;
import com.cincom.kmdata.client.api.relation.RepositoryException_Exception;

public class TypeServiceImpl implements TypeService {

	private CincomManager cincomManager;
	private NemakiPropertyManager propertyManager;

	private PropertyWS propertyWS;
	private RelationWS relationWS;
	private List<NemakiTypeDefinition> types = new ArrayList<NemakiTypeDefinition>();
	private Map<String, NemakiPropertyDefinitionCore> cores = new HashMap<String, NemakiPropertyDefinitionCore>();
	private Map<String, NemakiPropertyDefinitionDetail> details = new HashMap<String, NemakiPropertyDefinitionDetail>();


	private final String GROUP_TYPE_DOCUMENT = "document";
	private final String GROUP_TYPE_SHORTCUT = "shortcut";
	private final String GROUP_TYPE_FOLDER = "folder";
	private final String GROUP_TYPE_DIRECTORY = "directory";

	private static final Log logger = LogFactory.getLog(TypeServiceImpl.class);

	public TypeServiceImpl() {
	}

	public TypeServiceImpl(CincomManager cincomManager,
			NemakiPropertyManager propertyManager) {
		setPropertyManager(propertyManager);
		setCincomManager(cincomManager);
		propertyWS = cincomManager.getPropertyWS();
		relationWS = cincomManager.getRelationWS();

		// Build subtypes defined in CincomECM
		build();

		// Build virtual type of CincomECM attachment
		buildAttachment();

		// Build virtual type of CincomECM relation
		buildRelationships();
	}

	/**
	 * Build types existing as entities in CincomECM
	 */
	private void build() {
		// Get PropertyGroup IDs
		List<String> groups = new ArrayList<String>();
		try {
			groups = propertyWS.getGroups(cincomManager.getAdminContext(),
					null, null);
		} catch (com.cincom.kmdata.client.api.property.RepositoryException_Exception e) {
			logger.error(
					"PropertyGroups in CincomECM repository couldn't be retrieved",
					e);
			return;
		}

		// Parse each objectType(=PropertyGroup)
		for (String group : groups) {
			// Skip if mapped to cmis:folder/cmis:document
			if(filterOutMappedToCmisFolder(group) || filterOutMappedToCmisDocument(group)){
				continue;
			}

			// Get PropertyGroup
			PropertyGroup pg = null;
			try {
				pg = propertyWS.getPropertyGroup(
						cincomManager.getAdminContext(), group, null, null);
			} catch (com.cincom.kmdata.client.api.property.RepositoryException_Exception e) {
				logger.error(
						"PropertyGroup " + group + "couldn't be retrieved", e);
				continue;
			}

			// Set type attributes
			NemakiTypeDefinition ntd = setTypeAttributes(pg);

			// Set property attributes
			List<String> _properties = setPropertiesAttributes(pg);
			ntd.setProperties(_properties);

			types.add(ntd);
		}
	}

	private boolean filterOutMappedToCmisFolder(String group) {
		String mappedToCmisFolder = propertyManager
				.readValue(CincomPropertyKey.TYPE_MAPPING_TO_CMIS_FOLDER);

		if (StringUtils.isNotBlank(mappedToCmisFolder)) {
			String _mappedToCmisFolder = CmisTypeId.parse(mappedToCmisFolder)
					.getBaseId();
			return group.equals(_mappedToCmisFolder);
		}

		return false;
	}

	private boolean filterOutMappedToCmisDocument(String group) {
		String mappedToCmisDocument = propertyManager
				.readValue(CincomPropertyKey.TYPE_MAPPING_TO_CMIS_DOCUMENT);

		if (StringUtils.isNotBlank(mappedToCmisDocument)) {
			String _mappedToCmisDocument = CmisTypeId.parse(
					mappedToCmisDocument).getBaseId();
			return group.equals(_mappedToCmisDocument);
		}

		return false;
	}

	/**
	 * Set type attributes with setting properties attributes.
	 * The types are stored in a global variable.
	 * @param pg
	 * @return
	 */
	private NemakiTypeDefinition setTypeAttributes(PropertyGroup pg) {
		NemakiTypeDefinition ntd = new NemakiTypeDefinition();
		String typeId = CmisTypeId._build(pg.getGroup());

		ntd.setId(typeId);
		ntd.setType(NodeType.TYPE_DEFINITION.value());

		// id
		ntd.setTypeId(typeId);

		// localName
		ntd.setLocalName(typeId);

		// localNamespace
		String nameSpace = propertyManager
				.readValue(PropertyKey.CMIS_REPOSITORY_MAIN_NAMESPACE);
		ntd.setLocalNameSpace(nameSpace);

		// queryName
		ntd.setQueryName(escapeQueryName(typeId));

		// displayName
		for (com.cincom.kmdata.client.api.property.PropertyGroup.Label.Entry e : pg
				.getLabel().getEntry()) {
			if (cincomManager.getLang().equals(e.getKey())) {
				ntd.setDisplayName(e.getValue());
			}
		}

		// baseId, parentId, contntStreamAllowed, fulltextIndexed,
		// versionable
		if (pg.getType().equals(GROUP_TYPE_DOCUMENT)
				|| pg.getType().equals(GROUP_TYPE_SHORTCUT)) {
			ntd.setBaseId(BaseTypeId.CMIS_DOCUMENT);
			ntd.setParentId(BaseTypeId.CMIS_DOCUMENT.value());
			ntd.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
			ntd.setFulltextIndexed(true);
			ntd.setVersionable(true);
		} else if (pg.getType().equals(GROUP_TYPE_FOLDER)
				|| pg.getType().equals(GROUP_TYPE_DIRECTORY)) {
			ntd.setBaseId(BaseTypeId.CMIS_FOLDER);
			ntd.setParentId(BaseTypeId.CMIS_FOLDER.value());
			ntd.setFulltextIndexed(false);
			// versionable is not set for a folder in TypeManager
		}

		// description
		ntd.setDescription(typeId);

		// creatable
		ntd.setCreatable(true);

		// fileable
		// TODO need to verify
		ntd.setFilable(true);

		// queryable
		// TODO really?
		// TODO How about mask?
		ntd.setQueryable(true);

		// controllablePolicy
		ntd.setControllablePolicy(false);

		// controllableACL
		ntd.setControllableACL(true);

		// includeInSuperTypeQuery
		ntd.setIncludedInSupertypeQuery(true);

		// typeMutability
		ntd.setTypeMutabilityCreate(false);
		ntd.setTypeMutabilityDelete(false);
		ntd.setTypeMutabilityUpdate(false);

		return ntd;
	}
	
	/**
	 * Set attributes of all the properties for the real CincomECM property groups
	 * @param pg
	 * @return
	 */
	private List<String> setPropertiesAttributes(PropertyGroup pg) {
		List<String> _properties = new ArrayList<String>();

		// Add system properties
		List<String> systemProperties = buildSystemProperties();
		for (String systemProperty : systemProperties) {
			_properties.add(systemProperty);
		}

		// Add specific properties(with building them)
		List<com.cincom.kmdata.client.api.property.Property> properties = pg
				.getProperties();
		for (com.cincom.kmdata.client.api.property.Property _p : properties) {

			// To get correct metadata of a property
			com.cincom.kmdata.client.api.property.Property p = null;
			try {
				p = propertyWS.getProperty(cincomManager.getAdminContext(),
						pg.getGroup(), _p.getName());
			} catch (com.cincom.kmdata.client.api.property.RepositoryException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO null impl
			// TODO RELATION OUT_GOING should be exposed.
			if (p == null) {
				continue;
			}

			String propId = CmisPropertyId._build(pg.getGroup(), p.getName());

			NemakiPropertyDefinition npd = setPropertyAttributes(pg, p);

			if (npd.getPropertyType() == null) {
				continue;
			}

			// Build property
			buildProperty(npd);

			// Add propertyId to type
			_properties.add(propId);
		}

		return _properties;
	}

	/**
	 * Set each property attributes for the real CincomECM property groups
	 * @param pg
	 * @param p
	 * @return
	 */
	private NemakiPropertyDefinition setPropertyAttributes(PropertyGroup pg,
			com.cincom.kmdata.client.api.property.Property p) {
		NemakiPropertyDefinition npd = new NemakiPropertyDefinition();

		// Property Id
		String propId = CmisPropertyId._build(pg.getGroup(), p.getName());
		npd.setPropertyId(propId);

		// displayName
		Label _displayName = p.getLabel();
		String displayName = null;
		if (_displayName != null
				&& CollectionUtils.isNotEmpty(_displayName.getEntry())) {
			for (Entry e : _displayName.getEntry()) {
				if (cincomManager.getLang().equals(e.getKey())) {
					displayName = e.getValue();
				}
			}
		}
		npd.setDisplayName(displayName);

		// propertyType
		// Ignore CincomCM's BINARY type
		// FIXME Is this mapping right ?
		PropertyType propertyType = null;
		switch (p.getType()) {
		case javax.jcr.PropertyType.STRING:
			propertyType = PropertyType.STRING;
			break;
		case javax.jcr.PropertyType.NAME:
			propertyType = PropertyType.STRING;
			break;
		case javax.jcr.PropertyType.PATH:
			propertyType = PropertyType.STRING;
			break;
		case javax.jcr.PropertyType.BOOLEAN:
			propertyType = PropertyType.BOOLEAN;
			break;
		case javax.jcr.PropertyType.DATE:
			propertyType = PropertyType.DATETIME;
			break;
		case javax.jcr.PropertyType.DECIMAL:
			propertyType = PropertyType.DECIMAL;
			break;
		case javax.jcr.PropertyType.DOUBLE:
			propertyType = PropertyType.DECIMAL;
			break;
		case javax.jcr.PropertyType.LONG:
			propertyType = PropertyType.INTEGER;
			break;
		case javax.jcr.PropertyType.REFERENCE:
			propertyType = PropertyType.ID;
			break;
		case javax.jcr.PropertyType.URI:
			propertyType = PropertyType.URI;
			break;
		case javax.jcr.PropertyType.UNDEFINED:
			propertyType = PropertyType.STRING;
			break;
		default:
			break;
		}

		npd.setPropertyType(propertyType);

		// Cardinality
		Cardinality cardinality;
		if (p.isMultiple()) {
			cardinality = Cardinality.MULTI;
		} else {
			cardinality = Cardinality.SINGLE;
		}
		npd.setCardinality(cardinality);

		// updatability
		Updatability updatability;
		// TODO ONCREATE for some property ?
		if (p.isReadOnly()) {
			updatability = Updatability.READONLY;
		} else {
			updatability = Updatability.READWRITE;
		}
		npd.setUpdatability(updatability);

		// required
		boolean required;
		if (p.isMandatory()) {
			required = true;
		} else {
			required = false;
		}
		npd.setRequired(required);

		// queryable
		boolean queryable = p.isQueryable();
		npd.setQueryable(queryable);

		// orderable
		// TODO There is no such one?
		boolean orderable = false;
		npd.setOrderable(orderable);

		// choices ( & its defaultValues)
		// Default:
		List<Choice> choices = new ArrayList<Choice>();
		boolean openChoice = false;
		List<Object> defaults = new ArrayList<Object>();

		// CheckBox
		if (CollectionUtils.isNotEmpty(p.getCheckBoxValues())) {
			npd.setOpenChoice(false);
			List<String> labels = getCheckBoxLabels(p.getCheckBoxLabels()
					.getEntry(), cincomManager.getLang());
			for (int i = 0; i < p.getCheckBoxValues().size(); i++) {
				String s = p.getCheckBoxValues().get(i);
				List<Object> _values = new ArrayList<Object>();
				_values.add(convertClass(s, p.getType()));
				String label = "";
				if (CollectionUtils.isNotEmpty(labels)
						&& labels.size() >= i + 1) {
					label = labels.get(i);
				} else {
					label = s;
				}
				Choice c = new Choice(label, _values, null);
				choices.add(c);
			}

			defaults.add(convertClass(p.getCheckBoxSelectedValue(), p.getType()));

			// RadioValues
		} else if (CollectionUtils.isNotEmpty(p.getRadioValues())) {
			npd.setOpenChoice(false);
			List<String> labels = getRadioBoxLabels(p.getRadioLabels()
					.getEntry(), cincomManager.getLang());
			for (int i = 0; i < p.getRadioValues().size(); i++) {
				String s = p.getRadioValues().get(i);
				List<Object> _values = new ArrayList<Object>();
				_values.add(convertClass(s, p.getType()));
				String label = "";
				if (CollectionUtils.isNotEmpty(labels)
						&& labels.size() >= i + 1) {
					label = labels.get(i);
				} else {
					label = s;
				}
				Choice c = new Choice(label, _values, null);
				choices.add(c);
			}
			npd.setChoices(choices);

			defaults.add(convertClass(p.getRadioSelectedValue(), p.getType()));

			// ComboBox
			// TODO How to handle with
			// ValueList/SipmleList/IndexList
			// TODO displayName does not exist?
		} else if (CollectionUtils.isNotEmpty(p.getComboBoxValues())) {
			if (p.isComboBoxEditable()) {
				npd.setOpenChoice(true);
			}
			for (String s : p.getComboBoxValues()) {
				List<Object> _values = new ArrayList<Object>();
				_values.add(convertClass(s, p.getType()));
				Choice c = new Choice(s, _values, null);
				choices.add(c);
			}
			npd.setChoices(choices);
		}

		// Default values
		// TODO if checkbox/radiovalues has both default values and
		// selected values?
		if (CollectionUtils.isEmpty(npd.getDefaultValue())) {
			Object _val = convertClass(p.getDefaultValue(), p.getType());
			if (_val != null) {
				defaults.add(_val);
			}
		}
		npd.setChoices(choices);
		npd.setOpenChoice(openChoice);
		npd.setDefaultValue(defaults);

		// Attributes specific to Integer
		long maxValue = p.getNumberFieldMaxValue();
		npd.setMaxValue(maxValue);

		long minValue = p.getNumberFieldMinValue();
		npd.setMinValue(minValue);

		// Attributes specific to DateTime
		// TODO implement

		// Attributes specific to Decimal
		// TODO implement
		int _decimalPrecision = p.getNumberFieldDecimalPrecision();

		BigDecimal decimalMaxValue = BigDecimal.valueOf(p
				.getNumberFieldMaxValue());
		npd.setDecimalMaxValue(decimalMaxValue);
		BigDecimal decimalMinValue = BigDecimal.valueOf(p
				.getNumberFieldMinValue());
		npd.setDecimalMinValue(decimalMinValue);

		// Attributes specific to String
		// There is no minLength in CMIS String
		long maxLength = p.getMaxLength();
		npd.setMaxLength(maxLength);

		return npd;
	}

	/**
	 * Set virtual type of CincomECM attachment.
	 * The type is stored in a global variable.
	 */
	private void buildAttachment() {
		String typeId = CincomConst.DOCTYPE_ID_ATTACHMENT;
		NemakiTypeDefinition ntd = new NemakiTypeDefinition();
		ntd.setType(NodeType.TYPE_DEFINITION.value());

		ntd.setId(typeId);

		// id
		ntd.setTypeId(typeId);

		// localName
		ntd.setLocalName(typeId);

		// localNamespace
		String nameSpace = propertyManager
				.readValue(PropertyKey.CMIS_REPOSITORY_MAIN_NAMESPACE);
		ntd.setLocalNameSpace(nameSpace);

		// queryName
		ntd.setQueryName(escapeQueryName(typeId));

		// displayName
		ntd.setDisplayName(typeId);

		// baseId, parentId, contntStreamAllowed, fulltextIndexed
		ntd.setBaseId(BaseTypeId.CMIS_DOCUMENT);
		ntd.setParentId(BaseTypeId.CMIS_DOCUMENT.value());
		ntd.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
		ntd.setFulltextIndexed(false);

		// description
		ntd.setDescription(typeId);

		// creatable
		// TODO mockup
		ntd.setCreatable(false);

		// fileable
		// TODO need to verify
		ntd.setFilable(false);

		// queryable
		// TODO need to verify
		// There seems to be no flag in Category level.
		ntd.setQueryable(false);

		// versionable?
		ntd.setVersionable(false);

		// controllablePolicy
		ntd.setControllablePolicy(false);

		// controllableACL
		ntd.setControllableACL(true);

		// includeInSuperTypeQuery
		ntd.setIncludedInSupertypeQuery(false);

		// typeMutability
		ntd.setTypeMutabilityCreate(false);
		ntd.setTypeMutabilityDelete(false);
		ntd.setTypeMutabilityUpdate(false);

		types.add(ntd);
	}

	/**
	 * Set virtual types of CincomECM relation.
	 * The types are stored in a global variable.
	 */
	private void buildRelationships() {
		String cincomRelationTypeId = buildCincomRelationship();
		buildHasAttachmentRelationship();

		try {
			List<String> list = relationWS.getRelationNameList(cincomManager
					.getContext());

			for (String l : list) {
				String typeId = CmisTypeId._buildRelation(l);
				NemakiTypeDefinition ntd = buildRalationshipBase(typeId,
						cincomRelationTypeId);
				types.add(ntd);
			}

		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String buildCincomRelationship() {
		String cincomRelTypeId = CincomConst.RELTYPE_ID_RELATIONSHIP;
		NemakiTypeDefinition cincomRelationship = buildRalationshipBase(
				cincomRelTypeId, BaseTypeId.CMIS_RELATIONSHIP.value());
		types.add(cincomRelationship);

		return cincomRelTypeId;
	}

	private void buildHasAttachmentRelationship() {
		String cincomRelTypeId = CincomConst.RELTYPE_ID_RELATIONSHIP;
		String hasAttachmentTypeId = CincomConst.RELTYPE_ID_HAS_ATTACHMENT;
		NemakiTypeDefinition hasAttachmentRelationship = buildRalationshipBase(
				hasAttachmentTypeId, cincomRelTypeId);
		types.add(hasAttachmentRelationship);
	}

	private NemakiTypeDefinition buildRalationshipBase(String typeId,
			String parentId) {
		NemakiTypeDefinition ntd = new NemakiTypeDefinition();
		ntd.setType(NodeType.TYPE_DEFINITION.value());

		ntd.setId(typeId);

		// id
		ntd.setTypeId(typeId);

		// localName
		ntd.setLocalName(typeId);

		// localNamespace
		String nameSpace = propertyManager
				.readValue(PropertyKey.CMIS_REPOSITORY_MAIN_NAMESPACE);
		ntd.setLocalNameSpace(nameSpace);

		// queryName
		ntd.setQueryName(escapeQueryName(typeId));

		// displayName
		ntd.setDisplayName(typeId);

		// baseId, parentId, contntStreamAllowed, fulltextIndexed
		ntd.setBaseId(BaseTypeId.CMIS_RELATIONSHIP);
		ntd.setParentId(parentId);
		ntd.setContentStreamAllowed(ContentStreamAllowed.NOTALLOWED);
		ntd.setFulltextIndexed(false);

		// description
		ntd.setDescription(typeId);

		// creatable
		// TODO mockup
		ntd.setCreatable(true);

		// fileable
		// TODO need to verify
		// As long as "Relatinoship" doesn't come in, all types are fileable.
		ntd.setFilable(false);

		// queryable
		// TODO need to verify
		// There seems to be no flag in Category level.
		ntd.setQueryable(false);

		// controllablePolicy
		ntd.setControllablePolicy(false);

		// controllableACL
		ntd.setControllableACL(false);

		// includeInSuperTypeQuery
		ntd.setIncludedInSupertypeQuery(false);

		// typeMutability
		ntd.setTypeMutabilityCreate(false);
		ntd.setTypeMutabilityDelete(false);
		ntd.setTypeMutabilityUpdate(false);

		return ntd;
	}

	
	/**
	 * Store property in a global variable
	 * @param npd
	 */
	private void buildProperty(NemakiPropertyDefinition npd) {
		// core
		NemakiPropertyDefinitionCore core = new NemakiPropertyDefinitionCore();
		core = new NemakiPropertyDefinitionCore(npd);
		cores.put(npd.getPropertyId(), core);

		// detail
		NemakiPropertyDefinitionDetail detail = new NemakiPropertyDefinitionDetail();
		detail = new NemakiPropertyDefinitionDetail(npd, npd.getPropertyId());
		details.put(npd.getPropertyId(), detail);
	}

	private void buildSystemProperty(List<String> result, String propertyName,
			PropertyType propertyType, Cardinality cardinality) {
		String propertyId = CmisPropertyId._buildSystem(propertyName);

		NemakiPropertyDefinition npd = new NemakiPropertyDefinition();
		npd.setPropertyId(propertyId);
		npd.setDisplayName(propertyId);
		npd.setPropertyType(propertyType);
		npd.setCardinality(cardinality);
		npd.setUpdatability(Updatability.READONLY);
		npd.setRequired(false);
		npd.setQueryable(false);
		npd.setOrderable(false);
		npd.setChoices(null);
		npd.setDefaultValue(null);
		npd.setMaxValue(null);
		npd.setMinValue(null);
		npd.setDecimalMaxValue(null);
		npd.setDecimalMinValue(null);
		npd.setMaxLength(null);
		npd.setOpenChoice(false);

		buildProperty(npd);
		result.add(propertyId);
	}

	private List<String> buildSystemProperties() {
		List<String> result = new ArrayList<String>();

		// processId
		buildSystemProperty(result, CincomConst.PROP_NAME_PROCESSID,
				PropertyType.INTEGER, Cardinality.SINGLE);

		// processList
		buildSystemProperty(result, CincomConst.PROP_NAME_PROCESSLIST,
				PropertyType.STRING, Cardinality.MULTI);

		// processTaskId
		buildSystemProperty(result, CincomConst.PROP_NAME_PROCESSTASKID,
				PropertyType.INTEGER, Cardinality.SINGLE);

		// comments
		buildSystemProperty(result, CincomConst.PROP_NAME_COMMENTS,
				PropertyType.STRING, Cardinality.MULTI);

		// TODO HomePage?

		return result;

	}

	private List<String> getCheckBoxLabels(List<CheckBoxLabels.Entry> entries,
			String lang) {
		for (com.cincom.kmdata.client.api.property.Property.CheckBoxLabels.Entry e : entries) {
			if (lang.equals(e.getKey())) {
				return e.getValue().getItem();
			}
		}
		return null;
	}

	private List<String> getRadioBoxLabels(List<RadioLabels.Entry> entries,
			String lang) {
		for (RadioLabels.Entry e : entries) {
			if (lang.equals(e.getKey())) {
				return e.getValue().getItem();
			}
		}
		return null;
	}

	// Ignore binary type
	// See org.apache.chemistry.opencmis.commons.impl.WSConverter
	private Object convertClass(String value, int jcrType) {
		Object result = null;
		switch (jcrType) {
		case javax.jcr.PropertyType.STRING:
			result = value;
			break;
		case javax.jcr.PropertyType.NAME:
			result = value;
			break;
		case javax.jcr.PropertyType.PATH:
			result = value;
			break;
		case javax.jcr.PropertyType.BOOLEAN:
			result = Boolean.valueOf(value);
			break;
		case javax.jcr.PropertyType.DATE:
			try {
				Calendar cal = toCalendar(value);
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTimeInMillis(cal.getTimeInMillis());
				result = gcal;
			} catch (Exception e) {
				// TODO logging
				result = null;
			}
			break;
		case javax.jcr.PropertyType.DECIMAL:
			try {
				result = BigDecimal.valueOf(Long.valueOf(value));
			} catch (Exception e) {
				// TODO logging
				result = null;
			}
			break;
		case javax.jcr.PropertyType.DOUBLE:
			try {
				result = BigDecimal.valueOf(Long.valueOf(value));
			} catch (Exception e) {
				// TODO logging
				result = null;
			}
			break;
		case javax.jcr.PropertyType.LONG:
			try {
				result = BigInteger.valueOf(Long.valueOf(value));
			} catch (Exception e) {
				// TODO logging
				result = null;
			}
			break;
		case javax.jcr.PropertyType.REFERENCE:
			result = value;
			break;
		case javax.jcr.PropertyType.URI:
			result = value;
			break;
		case javax.jcr.PropertyType.UNDEFINED:
			result = value;
			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public List<NemakiTypeDefinition> getTypeDefinitions() {
		return types;
	}

	@Override
	public NemakiPropertyDefinitionCore getPropertyDefinitionCore(String coreId) {
		return cores.get(coreId);
	}

	@Override
	public NemakiPropertyDefinitionDetail getPropertyDefinitionDetail(
			String detailId) {
		return details.get(detailId);
	}

	@Override
	public List<NemakiPropertyDefinitionDetail> getPropertyDefinitionDetailByCoreNodeId(
			String coreNodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public AbstractTypeDefinition buildTypeDefinitionFromDB(
			NemakiTypeDefinition nemakiType) {
		return null;
	}

	/**
	 * 
	 * yyyy/MM/dd yy/MM/dd yyyy-MM-dd yy-MM-dd yyyyMMdd
	 * 
	 * HH:mm HH:mm:ss HH:mm:ss.SSS
	 * 
	 */
	public static Calendar toCalendar(String strDate) {
		strDate = format(strDate);
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);

		int yyyy = Integer.parseInt(strDate.substring(0, 4));
		int MM = Integer.parseInt(strDate.substring(5, 7));
		int dd = Integer.parseInt(strDate.substring(8, 10));
		int HH = cal.get(Calendar.HOUR_OF_DAY);
		int mm = cal.get(Calendar.MINUTE);
		int ss = cal.get(Calendar.SECOND);
		int SSS = cal.get(Calendar.MILLISECOND);
		cal.clear();
		cal.set(yyyy, MM - 1, dd);
		int len = strDate.length();
		switch (len) {
		case 10:
			break;
		case 16: // yyyy/MM/dd HH:mm
			HH = Integer.parseInt(strDate.substring(11, 13));
			mm = Integer.parseInt(strDate.substring(14, 16));
			cal.set(Calendar.HOUR_OF_DAY, HH);
			cal.set(Calendar.MINUTE, mm);
			break;
		case 19: // yyyy/MM/dd HH:mm:ss
			HH = Integer.parseInt(strDate.substring(11, 13));
			mm = Integer.parseInt(strDate.substring(14, 16));
			ss = Integer.parseInt(strDate.substring(17, 19));
			cal.set(Calendar.HOUR_OF_DAY, HH);
			cal.set(Calendar.MINUTE, mm);
			cal.set(Calendar.SECOND, ss);
			break;
		case 23: // yyyy/MM/dd HH:mm:ss.SSS
			HH = Integer.parseInt(strDate.substring(11, 13));
			mm = Integer.parseInt(strDate.substring(14, 16));
			ss = Integer.parseInt(strDate.substring(17, 19));
			SSS = Integer.parseInt(strDate.substring(20, 23));
			cal.set(Calendar.HOUR_OF_DAY, HH);
			cal.set(Calendar.MINUTE, mm);
			cal.set(Calendar.SECOND, ss);
			cal.set(Calendar.MILLISECOND, SSS);
			break;
		default:
			throw new IllegalArgumentException("Argument[" + strDate
					+ "]cannot be converted to Date string");
		}
		return cal;
	}

	private static String format(String str) {
		if (str == null || str.trim().length() < 8) {
			throw new IllegalArgumentException("Argument[" + str
					+ "]cannot be converted to Date string");
		}
		str = str.trim();
		String yyyy = null;
		String MM = null;
		String dd = null;
		String HH = null;
		String mm = null;
		String ss = null;
		String SSS = null;

		if (str.indexOf("/") == -1 && str.indexOf("-") == -1) {
			if (str.length() == 8) {
				yyyy = str.substring(0, 4);
				MM = str.substring(4, 6);
				dd = str.substring(6, 8);
				return yyyy + "/" + MM + "/" + dd;
			}
			yyyy = str.substring(0, 4);
			MM = str.substring(4, 6);
			dd = str.substring(6, 8);
			HH = str.substring(9, 11);
			mm = str.substring(12, 14);
			ss = str.substring(15, 17);
			return yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm + ":" + ss;
		}
		StringTokenizer token = new StringTokenizer(str, "_/-:. ");
		StringBuffer result = new StringBuffer();
		for (int i = 0; token.hasMoreTokens(); i++) {
			String temp = token.nextToken();
			switch (i) {
			case 0:
				yyyy = fillString(str, temp, "L", "20", 4);
				result.append(yyyy);
				break;
			case 1:
				MM = fillString(str, temp, "L", "0", 2);
				result.append("/" + MM);
				break;
			case 2:
				dd = fillString(str, temp, "L", "0", 2);
				result.append("/" + dd);
				break;
			case 3:
				HH = fillString(str, temp, "L", "0", 2);
				result.append(" " + HH);
				break;
			case 4:
				mm = fillString(str, temp, "L", "0", 2);
				result.append(":" + mm);
				break;
			case 5:
				ss = fillString(str, temp, "L", "0", 2);
				result.append(":" + ss);
				break;
			case 6:
				SSS = fillString(str, temp, "R", "0", 3);
				result.append("." + SSS);
				break;
			}
		}
		return result.toString();
	}

	private static String fillString(String strDate, String str,
			String position, String addStr, int len) {
		if (str.length() > len) {
			throw new IllegalArgumentException("Argument[" + strDate
					+ "]cannot be converted to Date string");
		}
		return fillString(str, position, len, addStr);
	}

	private static String fillString(String str, String position, int len,
			String addStr) {
		if (addStr == null || addStr.length() == 0) {
			throw new IllegalArgumentException("Illegal value to add. addStr="
					+ addStr);
		}
		if (str == null) {
			str = "";
		}
		StringBuffer buffer = new StringBuffer(str);
		while (len > buffer.length()) {
			if (position.equalsIgnoreCase("l")) {
				int sum = buffer.length() + addStr.length();
				if (sum > len) {
					addStr = addStr.substring(0, addStr.length() - (sum - len));
					buffer.insert(0, addStr);
				} else {
					buffer.insert(0, addStr);
				}
			} else {
				buffer.append(addStr);
			}
		}
		if (buffer.length() == len) {
			return buffer.toString();
		}
		return buffer.toString().substring(0, len);
	}

	private String escapeQueryName(String str) {
		// TODO enough only with period ? See Spec 2.1.2.1.3
		String replaced = str.replaceAll("\\.", "_");
		return replaced;
	}

	public void setCincomManager(CincomManager cincomManager) {
		this.cincomManager = cincomManager;
	}

	@Override
	public NemakiTypeDefinition getTypeDefinition(String typeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NemakiTypeDefinition createTypeDefinition(
			NemakiTypeDefinition typeDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NemakiTypeDefinition updateTypeDefinition(
			NemakiTypeDefinition typeDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTypeDefinition(String typeId) {
		// TODO Auto-generated method stub

	}

	@Override
	public NemakiPropertyDefinition getPropertyDefinition(String detailNodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NemakiPropertyDefinitionCore> getPropertyDefinitionCores() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NemakiPropertyDefinitionCore getPropertyDefinitionCoreByPropertyId(
			String propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NemakiPropertyDefinitionDetail createPropertyDefinition(
			NemakiPropertyDefinition propertyDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NemakiPropertyDefinitionDetail updatePropertyDefinitionDetail(
			NemakiPropertyDefinitionDetail propertyDefinitionDetail) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPropertyManager(NemakiPropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}
}
