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
package jp.aegif.nemaki.cincom.query;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.aegif.nemaki.cincom.model.CmisTypeId;
import jp.aegif.nemaki.cincom.model.Document;
import jp.aegif.nemaki.cincom.model.NodeValueObject;
import jp.aegif.nemaki.cincom.service.node.impl.CEContentServiceImpl;
import jp.aegif.nemaki.cincom.shared.CincomManager;
import jp.aegif.nemaki.model.Content;
import jp.aegif.nemaki.model.Folder;
import jp.aegif.nemaki.query.QueryProcessor;
import jp.aegif.nemaki.repository.type.TypeManager;
import jp.aegif.nemaki.service.cmis.CompileObjectService;
import jp.aegif.nemaki.service.cmis.ExceptionService;
import jp.aegif.nemaki.service.cmis.PermissionService;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryWalker;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cincom.kmdata.client.api.search.ItemNotFoundException_Exception;
import com.cincom.kmdata.client.api.search.Parameters;
import com.cincom.kmdata.client.api.search.Parameters.QueryValues;
import com.cincom.kmdata.client.api.search.Parameters.QueryValues.Entry;
import com.cincom.kmdata.client.api.search.RepositoryException_Exception;
import com.cincom.kmdata.client.api.search.Result;
import com.cincom.kmdata.client.api.search.ResultSet;

@SuppressWarnings("deprecation")
public class CincomQueryProcessor implements QueryProcessor {

	private CEContentServiceImpl contentService;
	private PermissionService permissionService;
	private CompileObjectService compileObjectService;
	private ExceptionService exceptionService;
	private CincomManager cincomManager;
	private static final Log logger = LogFactory.getLog(CincomQueryProcessor.class);

	public CincomQueryProcessor() {
		
	}
	
	public ObjectList query(TypeManager typeManager, CallContext callContext,
			String username, String id, String statement,
			Boolean searchAllVersions, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount) {

		// QueryObject includes the SQL information
		QueryObject queryObject = new QueryObject(typeManager);

		// "WHERE" clause
		String whereQueryString = parseWhereClause(queryObject, statement);
		
		// "FROM" clause to Lucene query
		String fromQueryString = parseFromClause(queryObject);
		
		// Execute query
		QueryValues queryValues = new QueryValues();
		Entry entry = new Entry();
		entry.setKey(".");
		String query = "(" + whereQueryString + ")" + " and " + "(" + fromQueryString + ")";
		entry.setValue(query);
		queryValues.getEntry().add(entry);

		Parameters parameters = new Parameters();
		parameters.setFilter(1); // no filter
		parameters.setQueryValues(queryValues); 
		
		ResultSet resultSet = new ResultSet();
		try {
			resultSet = cincomManager.getSearchWS().sendQuery(cincomManager.getContext(), "", parameters);
		} catch (ItemNotFoundException_Exception e) {
			e.printStackTrace();
		} catch (RepositoryException_Exception e) {
			e.printStackTrace();
		}

		//Convert results
		List<Content> contents = new ArrayList<Content>();
		Collection<Result> collection = resultSet.getResults();
		
		if(CollectionUtils.isEmpty(collection)){
			ObjectListImpl nullList = new ObjectListImpl();
			nullList.setHasMoreItems(false);
			nullList.setNumItems(BigInteger.ZERO);
			return nullList;
		}else{
			for (Result result : collection) {
			      Object object = result.getObject();
			      
			      if(object instanceof com.cincom.kmdata.client.api.search.Folder
			        ){
			    	  NodeValueObject n = new NodeValueObject(object);
			    	  Folder f = contentService.convertFolder(n);
			    	  contents.add(f);
			      }else if(object instanceof com.cincom.kmdata.client.api.search.Document){
			    	  NodeValueObject n = new NodeValueObject(object);
			    	  Document d = contentService.convertDocument(n);
			    	  contents.add(d);
			      }
			}
			
		 	// Filter out by permissions
			List<Content> permitted = permissionService.getFiltered(callContext,
					contents);

			// Filter return value with SELECT clause
			Map<String, String>m = queryObject.getRequestedPropertiesByAlias();
			Map<String, String> aliases = new HashMap<String, String>();
			for(String alias : m.keySet()){
				aliases.put(m.get(alias), alias);
			}
			
			//Process aliases
			String filter = null;
			if(!aliases.keySet().contains("*")){
				filter = StringUtils.join(aliases.keySet(), ",");
			}
			
			return compileObjectService.compileObjectDataList(callContext, permitted, filter, includeAllowableActions, includeRelationships, renditionFilter, false, maxItems, skipCount, false, aliases);
		}
	}

	private String parseWhereClause(QueryObject queryObject, String statement){
		String whereQueryString = "";
		
		CmisQueryWalker queryWalker = null;
		try{
			queryWalker =  new QueryUtil().traverseStatementAndCatchExc(statement, queryObject, null);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Tree whereTree = queryWalker.getWherePredicateTree();
		
		if (whereTree == null || whereTree.isNil()) {
			//To get all
			whereQueryString = "@uuid";
		} else {
			try{
				CincomPredicateWalker predicateWalker = new CincomPredicateWalker(
						queryObject);
				
				//TODO null check
				String whereQuery = predicateWalker.walkPredicate(whereTree);
				whereQueryString = whereQuery.toString();
			}catch(Exception e){
				e.printStackTrace();
				//TODO Output more detailed exception
				exceptionService.invalidArgument("Invalid CMIS SQL statement!");
			}
		}
		
		return whereQueryString;
	}
	
	private String parseFromClause(QueryObject queryObject){
		String fromQueryString = "";
		
		TypeDefinition td = queryObject.getMainFromName();
		
		td = queryObject.getMainFromName();
		if(td.getId().equals(BaseTypeId.CMIS_DOCUMENT.value())){
			fromQueryString = "@primaryType='kmdata:document'";
		}else if(td.getId().equals(BaseTypeId.CMIS_FOLDER.value())){
			fromQueryString = "@primaryType='kmdata:folder'";
		}else{
			String pg = CmisTypeId.parse(td.getId()).getBaseId();
			fromQueryString = "@propertyGroup='"+ pg + "'";
		}
		
		return fromQueryString;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setCompileObjectService(
			CompileObjectService compileObjectService) {
		this.compileObjectService = compileObjectService;
	}

	public void setExceptionService(ExceptionService exceptionService) {
		this.exceptionService = exceptionService;
	}
	
	public void setCincomManager(CincomManager cincomManager) {
		this.cincomManager = cincomManager;
	}
	public void setContentService(CEContentServiceImpl contentService) {
		this.contentService = contentService;
	}
}
