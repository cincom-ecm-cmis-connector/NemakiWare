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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.aegif.nemaki.cincom.model.CmisId;
import jp.aegif.nemaki.cincom.model.CmisPropertyId;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.CmisSelector;
import org.apache.chemistry.opencmis.server.support.query.ColumnReference;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * CMIS to Solr parser class for WHERE clause of query
 * 
 * @author linzhixing
 * 
 */
public class CincomPredicateWalker {

	private QueryObject queryObject;
	private static final Log logger = LogFactory.getLog(CincomPredicateWalker.class);
	

	
	public CincomPredicateWalker(QueryObject queryObject) {
		this.queryObject = queryObject;
	}

	public String walkPredicate(Tree node) {
		switch (node.getType()) {
		
		// Boolean walks
		case CmisQlStrictLexer.NOT:
			return walkNot(node.getChild(0));
		case CmisQlStrictLexer.AND:
			return walkAnd(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.OR:
			return walkOr(node.getChild(0), node.getChild(1));
		// Comparison walks
		case CmisQlStrictLexer.EQ:
			return walkEquals(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NEQ:
			return walkNotEquals(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.GT:
			return walkGreaterThan(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.GTEQ:
			return walkGreaterOrEquals(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.LT:
			return walkLessThan(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.LTEQ:
			return walkLessOrEquals(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.LIKE:
			return walkLike(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NOT_LIKE:
			return walkNotLike(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.IN:
			return walkIn(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NOT_IN:
			return walkNotIn(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.ANY:
			return walkInAny(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NOT_IN_ANY:
			return walkNotInAny(node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.IS_NULL:
			return walkIsNull(node.getChild(0));
		case CmisQlStrictLexer.IS_NOT_NULL:
			return walkIsNotNull(node.getChild(0));
		//Folder hierarchy walk	
		case CmisQlStrictLexer.IN_FOLDER:
			try {
				throw new Exception();
			} catch (Exception e) {
				logger.error("IN_FOLDER query is not supported.", e);
			}
		case CmisQlStrictLexer.IN_TREE:
			try {
				throw new Exception();
			} catch (Exception e) {
				logger.error("IN_TREE query is not supported.", e);
			}
		// Full-text search type walk
		case CmisQlStrictLexer.CONTAINS:
			return  walkContains(node.getChild(0));
		default:
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Definition of Boolean walks
	// //////////////////////////////////////////////////////////////////////////////
	private String walkNot(Tree node) {
		return "not(" + walkPredicate(node) + ")";
	}

	private String walkOr(Tree leftNode, Tree rightNode) {
		return "(" + walkPredicate(leftNode) + " or " + walkPredicate(rightNode) + ")";
	}

	private String walkAnd(Tree leftNode, Tree rightNode) {
		return "(" + walkPredicate(leftNode) + " and " + walkPredicate(rightNode) + ")";
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Definition of Comparison walks
	// //////////////////////////////////////////////////////////////////////////////
	private String walkEquals(Tree leftNode, Tree rightNode) {
		return walkCompareInternal(leftNode, rightNode, "=");
	}
	
	private String walkNotEquals(Tree leftNode, Tree rightNode) {
		return walkCompareInternal(leftNode, rightNode, "!=");
	}

	private String walkGreaterThan(Tree leftNode, Tree rightNode) {
		return walkCompareInternal(leftNode, rightNode, ">");
	}

	private String walkGreaterOrEquals(Tree leftNode, Tree rightNode) {
		return walkCompareInternal(leftNode, rightNode, ">=");
	}

	private String walkLessThan(Tree leftNode, Tree rightNode) {
		return walkCompareInternal(leftNode, rightNode, "<");
	}

	private String walkLessOrEquals(Tree leftNode, Tree rightNode) {
		return walkCompareInternal(leftNode, rightNode, "<=");
	}

	/**
	 * TODO Implement check for each kind of literal
	 * Parse field name & condition value. Field name is prepared for Solr
	 * query.
	 * 
	 * @param leftNode
	 * @param rightNode
	 * @return
	 */
	private String walkCompareInternal(Tree leftNode,
			Tree rightNode, String operator) {
		String leftSide = convertKey(leftNode.getChild(0));
		String rightSide = convertRightSide(leftNode, rightNode.toString());
		return leftSide + operator + rightSide; 
	}

	private String convertRightSide(Tree leftNode, String rightNodeElement){
		String rightSide = rightNodeElement;
		String leftSide = convertKey(leftNode.getChild(0));
		
		if("@uuid".equals(leftSide)){
			rightSide = rightSide.replaceAll("'", "");
			rightSide = CmisId.parse(rightSide).getBaseId();
			rightSide = "'" + rightSide + "'";
		}
		
		return rightSide;
	}
	
	private String walkLike(Tree colNode, Tree stringNode) {
		// Check for CMIS SQL specification
		Object rVal = walkExpr(stringNode);
		if (!(rVal instanceof String)) {
			throw new IllegalStateException(
					"LIKE operator requires String literal on right hand side.");
		}
		ColumnReference colRef = getColumnReference(colNode);
		String colRefName = colRef.getName();
		TypeDefinition td = colRef.getTypeDefinition();
		Map<String, PropertyDefinition<?>> pds = td.getPropertyDefinitions();
		PropertyDefinition<?> pd = pds.get(colRefName);
		PropertyType propType = pd.getPropertyType();
		if (propType != PropertyType.STRING && propType != PropertyType.HTML
				&& propType != PropertyType.ID && propType != PropertyType.URI) {
			throw new IllegalStateException("Property type " + propType.value()
					+ " is not allowed FOR LIKE");
		}
		if (pd.getCardinality() != Cardinality.SINGLE) {
			throw new IllegalStateException(
					"LIKE is not allowed for multi-value properties ");
		}

		String pattern = translatePattern((String) rVal);
		

		
		String field = convertKey(colNode.getChild(0));
		return "@contains(" + field + "," + "'" + pattern + "'" + ")";
	}
	
	private String walkNotLike(Tree colNode, Tree stringNode) {
		return "not" + "(" + walkLike(colNode, stringNode) + ")";
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Definition of multiple value type walks
	// //////////////////////////////////////////////////////////////////////////////
	private String walkIn(Tree colNode, Tree listNode) {
		//TODO cardinality check 
		
		// Check for CMIS SQL specification
		String field = convertKey(colNode);
		List<?> list = (List<?>) walkExpr(listNode);
		
		//Convert
		List<String> _list = new ArrayList<String>();
		for (Object elm : list) {
			String _elm = convertRightSide(colNode, elm.toString());
			_list.add(field + "=" + _elm);
		}
		
		return "(" + StringUtils.join(_list, " or ") + ")";
	}

	private String walkNotIn(Tree colNode, Tree listNode) {
		
		return "not" + walkIn(colNode, listNode);
	}

	private String walkInAny(Tree leftNode, Tree rightNode) {
		// Check for CMIS SQL specification
		ColumnReference colRef = getColumnReference(leftNode);
		PropertyDefinition<?> pd = colRef.getPropertyDefinition();
		if (pd.getCardinality() != Cardinality.MULTI) {
			throw new IllegalStateException(
					"Operator ANY...IN is only allowed for multi-value properties ");
		}
		
		return walkIn(leftNode, rightNode);
	}

	private String walkNotInAny(Tree leftNode, Tree rightNode) {
		ColumnReference colRef = getColumnReference(leftNode);
		PropertyDefinition<?> pd = colRef.getPropertyDefinition();
		if (pd.getCardinality() != Cardinality.MULTI) {
			throw new IllegalStateException(
					"Operator ANY...IN is only allowed for multi-value properties ");
		}
		
		return walkNotIn(leftNode, rightNode);
	}

	private String walkIsNull(Tree colNode) {
		String field = walkExpr(colNode).toString();
		return field;
	}

	private String walkIsNotNull(Tree colNode) {
		String field = walkExpr(colNode).toString();
		return "not" + field;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Definition of full-text search type walk
	// //////////////////////////////////////////////////////////////////////////////
	// Wildcards of CONTAINS() is the same as those of Solr, so leave them as they are.
	private String walkContains(Tree queryNode) {
		return "@contains(@content," + "'" + queryNode.getText() + "'" + ")";
	}

	private Query walkSearchExpr(Tree node) {
		switch (node.getType()) {
		case TextSearchLexer.TEXT_AND:
			return walkTextAnd(node);
		case TextSearchLexer.TEXT_OR:
			return walkTextOr(node);
		case TextSearchLexer.TEXT_MINUS:
			return walkTextMinus(node);
		case TextSearchLexer.TEXT_SEARCH_WORD_LIT:
			return walkTextWord(node);
		case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
			return walkTextPhrase(node);
		default:
			walkOtherExpr(node);
			return null;
		}
	}

	private Query walkTextAnd(Tree node) {
		BooleanQuery q = new BooleanQuery();
		for (int i = 0; i < node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			q.add(walkSearchExpr(child), Occur.MUST);
		}
		return q;
	}

	private Query walkTextOr(Tree node) {
		BooleanQuery q = new BooleanQuery();
		for (int i = 0; i < node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			q.add(walkSearchExpr(child), Occur.SHOULD);
		}
		return q;
	}

	private Query walkTextMinus(Tree node) {
		BooleanQuery q = new BooleanQuery();
		for (int i = 0; i < node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			q.add(walkSearchExpr(child), Occur.MUST);
		}
		return q;
	}

	private Query walkTextWord(Tree node) {
		Term term = new Term("text", node.toString());
		TermQuery q = new TermQuery(term);
		return q;
	}

	private Query walkTextPhrase(Tree node) {
		Term term = new Term("text", node.toString());
		TermQuery q = new TermQuery(term);
		return q;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Definition of walkExpr and its subwalks
	// These are used from various walks to evaluate a node value.
	// //////////////////////////////////////////////////////////////////////////////
	private Object walkExpr(Tree node) {
		switch (node.getType()) {
		case CmisQlStrictLexer.BOOL_LIT:
			return walkBoolean(node);
		case CmisQlStrictLexer.NUM_LIT:
			return walkNumber(node);
		case CmisQlStrictLexer.STRING_LIT:
			return walkString(node);
		case CmisQlStrictLexer.TIME_LIT:
			return walkTimestamp(node);
		case CmisQlStrictLexer.IN_LIST:
			return walkList(node);
		case CmisQlStrictLexer.COL:
			return walkCol(node);
		case CmisQlStrictLexer.ID:
			return walkId(node);
		default:
			return walkOtherExpr(node);
		}
	}

	private Object walkBoolean(Tree node) {
		String s = node.getText();
		return Boolean.valueOf(s);
	}

	private Object walkNumber(Tree node) {
		String s = node.getText();
		if (s.contains(".") || s.contains("e") || s.contains("E")) {
			return Double.valueOf(s);
		} else {
			return Long.valueOf(s);
		}
	}

	private Object walkString(Tree node) {
		String s = node.getText();
		s = s.substring(1, s.length() - 1);
		//return "\"" + ClientUtils.escapeQueryChars(s) + "\"";
		return s;
	}

	private Object walkTimestamp(Tree node) {
		String s = node.getText();
		s = s.substring(s.indexOf('\'') + 1, s.length() - 1);
		return s;
	}

	private Object walkList(Tree node) {
		int n = node.getChildCount();
		List<Object> res = new ArrayList<Object>(n);
		for (int i = 0; i < n; i++) {
			res.add(walkExpr(node.getChild(i)));
		}
		return res;
	}

	private Object walkCol(Tree node) {
		return null;
	}

	private Object walkId(Tree node) {
		String s;
		s = node.toStringTree();
		return s;
	}

	private Object walkOtherExpr(Tree node) {
		throw new CmisRuntimeException("Unknown node type: " + node.getType()
				+ " (" + node.getText() + ")");
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Utility methods
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Translate a full-text search expression from SQL style to Solr style
	 * 
	 * @param wildcardString
	 * @return
	 */
	private static String translatePattern(String wildcardString) {
		int index = 0;
		int start = 0;
		StringBuffer res = new StringBuffer();

		while (index >= 0) {
			index = wildcardString.indexOf('%', start);
			if (index < 0) {
				res.append(wildcardString.substring(start));
			} else if (index == 0 || index > 0
					&& wildcardString.charAt(index - 1) != '\\') {
				res.append(wildcardString.substring(start, index));
				res.append("*");
			} else {
				res.append(wildcardString.substring(start, index + 1));
			}
			start = index + 1;
		}
		wildcardString = res.toString();

		index = 0;
		start = 0;
		res = new StringBuffer();

		while (index >= 0) {
			index = wildcardString.indexOf('_', start);
			if (index < 0) {
				res.append(wildcardString.substring(start));
			} else if (index == 0 || index > 0
					&& wildcardString.charAt(index - 1) != '\\') {
				res.append(wildcardString.substring(start, index));
				res.append("?"); //
			} else {
				res.append(wildcardString.substring(start, index + 1));
			}
			start = index + 1;
		}
		return res.toString();
	}

	private ColumnReference getColumnReference(Tree columnNode) {
		CmisSelector sel = queryObject.getColumnReference(columnNode
				.getTokenStartIndex());
		if (null == sel) {
			throw new IllegalStateException("Unknown property query name "
					+ columnNode.getChild(0));
		} else if (sel instanceof ColumnReference) {
			return (ColumnReference) sel;
		} else {
			throw new IllegalStateException(
					"Unexpected numerical value function in where clause");
		}
	}

	private String convertKey(Tree node){
		
		String cmisQueryName = getColumnReference(node).getPropertyQueryName(); 
		
		String ID_COMMENTS = CmisPropertyId._buildSystem("comments");
		String ID_PROCEE_ID = CmisPropertyId._buildSystem("processId");
		
		String cincomQueryName = null;
		//Basic property's QueryName equals PropertyId 
		if(PropertyIds.OBJECT_ID.equals(cmisQueryName)){
			cincomQueryName = "uuid";
		}else if(PropertyIds.NAME.equals(cmisQueryName)){
			cincomQueryName = "name";
		}else if(PropertyIds.CREATED_BY.equals(cmisQueryName)){
			cincomQueryName = "createdBy";
		}else if(PropertyIds.CREATION_DATE.equals(cmisQueryName)){
			cincomQueryName = "creationDate";
		}else if(PropertyIds.LAST_MODIFIED_BY.equals(cmisQueryName)){
			cincomQueryName = "modifiedBy";
		}else if(PropertyIds.LAST_MODIFICATION_DATE.equals(cmisQueryName)){
			cincomQueryName = "modificationDate";
		}else if(PropertyIds.BASE_TYPE_ID.equals(cmisQueryName)){
			cincomQueryName = "primaryType";
		}else if(PropertyIds.OBJECT_TYPE_ID.equals(cmisQueryName)){
			cincomQueryName = "propertyGroup";
		}else if(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY.equals(cmisQueryName)){
			cincomQueryName = "lockToken";
		}else if(PropertyIds.CONTENT_STREAM_FILE_NAME.equals(cmisQueryName)){
			cincomQueryName = "fileName";
		}else if(PropertyIds.CONTENT_STREAM_LENGTH.equals(cmisQueryName)){
			cincomQueryName = "size";
		}else if(PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(cmisQueryName)){
			cincomQueryName = "mimeType";
		}else if(ID_COMMENTS.equals(cmisQueryName)){
			cincomQueryName = "comment";
		}else if(ID_PROCEE_ID.equals(cmisQueryName)){
			cincomQueryName = "status";
		}else{
			TypeDefinition td = queryObject.getMainFromName();
			
			//escape-back
			Map<String, PropertyDefinition<?>> properties = td.getPropertyDefinitions();
			for(Entry<String, PropertyDefinition<?>> property : properties.entrySet()){
				if(cmisQueryName.equals(property.getValue().getQueryName())){
					cincomQueryName = property.getValue().getId().replaceAll(td.getId() + ".", "");
					break;
				}
			}
		}
		
		//TODO make unqueryable unnecessary properties
		if(StringUtils.isNotBlank(cincomQueryName)){
			return "@" + cincomQueryName;
		}else{
			return null;
		}
	}
}
