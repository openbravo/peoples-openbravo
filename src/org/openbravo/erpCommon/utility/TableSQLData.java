/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.FieldProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import org.apache.log4j.Logger;


public class TableSQLData {
  static Logger log4j = Logger.getLogger(TableSQLData.class);
  private final String internalPrefix = "@@";
  private static final String FIELD_CONCAT = " || ' - ' || ";
  private static final String INACTIVE_DATA = "**";
  private VariablesSecureApp vars;
  private ConnectionProvider pool;
  private Hashtable<String, String> parameters = new Hashtable<String, String>();
  private Vector<Properties> structure = new Vector<Properties>();
  private Vector<QueryParameterStructure> paramSelect = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramFrom = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramWhere = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramOrderBy = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramFilter = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramInternalFilter = new Vector<QueryParameterStructure>();
  private Vector<QueryParameterStructure> paramInternalOrderBy = new Vector<QueryParameterStructure>();
  private Vector<QueryFieldStructure> select = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> from = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> where = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> orderBy = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> filter = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> internalFilter = new Vector<QueryFieldStructure>();
  private Vector<QueryFieldStructure> internalOrderBy = new Vector<QueryFieldStructure>();
  private Vector<String> orderByPosition = new Vector<String>();
  private Vector<String> orderByDirection = new Vector<String>();
  private int index = 0;
  private boolean isSameTable = false;

  public TableSQLData() {
  }

  public TableSQLData(VariablesSecureApp _vars, ConnectionProvider _conn, String _adTabId, String _orgList, String _clientList) throws Exception {
    if (_vars!=null) setVars(_vars);
    setPool(_conn);
    setTabID(_adTabId);
    setOrgList(_orgList);
    setClientList(_clientList);
    setWindowDefinition();
    generateStructure();
    generateSQL();
  }

  public void setParameter(String name, String value) throws Exception {
    if (name==null || name.equals("")) throw new Exception("Invalid parameter name");
    if (this.parameters==null) this.parameters = new Hashtable<String, String>();
    if (value==null || value.equals("")) this.parameters.remove(name.toUpperCase());
    else this.parameters.put(name.toUpperCase(), value);
  }

  public String getParameter(String name) {
    if (name==null || name.equals("")) return "";
    else if (this.parameters==null) return "";
    else return this.parameters.get(name.toUpperCase());
  }

  public Vector<String> getParameters() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled()) log4j.debug("Obtaining parameters");
    Vector<QueryParameterStructure> vAux = getSelectParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux==null || strAux.equals("")) result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Select parameters obtained");
    vAux = getFromParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux==null || strAux.equals("")) result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("From parameters obtained");
    vAux = getWhereParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux==null || strAux.equals("")) result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Where parameters obtained");
    vAux = getFilterParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux==null || strAux.equals("")) result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Filter parameters obtained");
    vAux = getOrderByParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        String strAux = getParameter(aux.getName());
        if (strAux==null || strAux.equals("")) result.addElement(aux.getName());
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Order by parameters obtained");
    result.addElement("#AD_LANGUAGE");
    return result;
  }

  public Vector<String> getParameterValues() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled()) log4j.debug("Obtaining parameters values");
    Vector<QueryParameterStructure> vAux = getSelectParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Select parameters obtained");
    vAux = getFromParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("From parameters obtained");
    vAux = getWhereParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Where parameters obtained");
    vAux = getFilterParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Filter parameters obtained");
    vAux = getOrderByParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Order by parameters obtained");
    return result;
  }

  public Vector<String> getParameterValuesTotalSQL() {
    Vector<String> result = new Vector<String>();
    if (log4j.isDebugEnabled()) log4j.debug("Obtaining parameters values");
    Vector<QueryParameterStructure> vAux = getFromParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("From parameters obtained");
    vAux = getWhereParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Where parameters obtained");
    vAux = getFilterParameters();
    if (vAux!=null) {
      for (int i=0;i<vAux.size();i++) {
        QueryParameterStructure aux = vAux.elementAt(i);
        result.addElement(getParameter(aux.getName()));
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Filter parameters obtained");
    return result;
  }

  public void setVars(VariablesSecureApp _vars) throws Exception {
    if (_vars==null) throw new Exception("The session vars is null");
    this.vars = _vars;
  }
  
  public VariablesSecureApp getVars() {
    return this.vars;
  }

  public void setPool(ConnectionProvider _conn) throws Exception {
    if (_conn==null) throw new Exception("The pool is null");
    this.pool = _conn;
  }
  
  public ConnectionProvider getPool() {
    return this.pool;
  }

  public void setTabID(String _data) throws Exception {
    if (_data==null || _data.equals("")) throw new Exception("The Tab ID must be specified");
    setParameter(internalPrefix + "AD_Tab_ID", _data);
  }

  public String getTabID() {
    return getParameter(internalPrefix + "AD_Tab_ID");
  }

  public boolean getIsSameTable() {
    return this.isSameTable;
  }

  public void setWindowDefinition() throws Exception {
    TableSQLQueryData[] data = TableSQLQueryData.selectWindowDefinition(getPool(), getVars().getLanguage(), getTabID());
    if (data==null || data.length==0) throw new Exception("Couldn't extract window information");
    setParameter(internalPrefix + "AD_Window_ID", data[0].adWindowId);
    setParameter(internalPrefix + "AD_Table_ID", data[0].adTableId);
    setParameter(internalPrefix + "TabLevel", data[0].tablevel);
    setParameter(internalPrefix + "IsReadOnly", data[0].isreadonly);
    setParameter(internalPrefix + "HasTree", data[0].hastree);
    setParameter(internalPrefix + "WhereClause", data[0].whereclause);
    setParameter(internalPrefix + "OrderByClause", data[0].orderbyclause);
    setParameter(internalPrefix + "AD_Process_ID", data[0].adProcessId);
    setParameter(internalPrefix + "AD_ColumnSortOrder_ID", data[0].adColumnsortorderId);
    setParameter(internalPrefix + "AD_ColumnSortYesNo_ID", data[0].adColumnsortyesnoId);
    setParameter(internalPrefix + "IsSortTab", data[0].issorttab);
    setParameter(internalPrefix + "FilterClause", data[0].filterclause);
    setParameter(internalPrefix + "EditReference", data[0].editreference);
    setParameter(internalPrefix + "WindowType", data[0].windowtype);
    setParameter(internalPrefix + "IsSOTrx", data[0].issotrx);
    setParameter(internalPrefix + "WindowName", data[0].windowName);
    setParameter(internalPrefix + "WindowNameTrl", data[0].windowNameTrl);
    setParameter(internalPrefix + "TabName", data[0].tabName);
    setParameter(internalPrefix + "TabNameTrl", data[0].tabNameTrl);
    setParameter(internalPrefix + "TableName", data[0].tablename);
    if (!getTabLevel().equals("0")) {
      TableSQLQueryData[] parentFields = TableSQLQueryData.parentsColumnName(getPool(), getTabID());
      if (parentFields != null && parentFields.length>0) {
        if (log4j.isDebugEnabled()) log4j.debug("Parent key: " + parentFields[0].columnname);
        setParameter(internalPrefix + "ParentColumnName", parentFields[0].columnname);
      } else {
        parentFields = TableSQLQueryData.parentsColumnNameKey(getPool(), getTabID());
        if (parentFields != null && parentFields.length>0) {
          if (log4j.isDebugEnabled()) log4j.debug("Parent key: " + parentFields[0].columnname);
          setParameter(internalPrefix + "ParentColumnName", parentFields[0].columnname);
          this.isSameTable = true;
        }
      }
    }
  }

  public String getWindowID() {
    return getParameter(internalPrefix + "AD_Window_ID");
  }

  public String getKeyColumn() {
    return getParameter(internalPrefix + "KeyColumn");
  }

  public String getTableID() {
    return getParameter(internalPrefix + "AD_Table_ID");
  }

  public String getTableName() {
    return getParameter(internalPrefix + "TableName");
  }

  public String getWhereClause() {
    return getParameter(internalPrefix + "WhereClause");
  }

  public String getOrderByClause() {
    return getParameter(internalPrefix + "OrderByClause");
  }

  public String getFilterClause() {
    return getParameter(internalPrefix + "FilterClause");
  }

  public String getTabLevel() {
    return getParameter(internalPrefix + "TabLevel");
  }

  public String getWindowType() {
    return getParameter(internalPrefix + "WindowType");
  }

  public String getParentColumnName() {
    return getParameter(internalPrefix + "ParentColumnName");
  }

  public void setOrgList(String _orgList) throws Exception {
    setParameter(internalPrefix + "orgList", _orgList);
  }

  public String getOrgList() {
    return getParameter(internalPrefix + "orgList");
  }

  public void setClientList(String _clientList) throws Exception {
    setParameter(internalPrefix + "clientList", _clientList);
  }

  public String getClientList() {
    return getParameter(internalPrefix + "clientList");
  }

  public void addStructure(Properties _prop) {
    if (_prop==null) return;
    if (this.structure == null) this.structure = new Vector<Properties>();
    this.structure.addElement(_prop);
  }

  public Vector<Properties> getStructure() {
    return this.structure;
  }

  public void addOrderByPosition(String _data) {
    if (_data==null) return;
    if (this.orderByPosition == null) this.orderByPosition = new Vector<String>();
    this.orderByPosition.addElement(_data);
  }

  public Vector<String> getOrderByPosition() {
    return this.orderByPosition;
  }

  public void addOrderByDirection(String _data) {
    if (_data==null) return;
    if (this.orderByDirection == null) this.orderByDirection = new Vector<String>();
    this.orderByDirection.addElement(_data);
  }

  public Vector<String> getOrderByDirection() {
    return this.orderByDirection;
  }

  public Vector<Properties> getFilteredStructure(String propertyName, String propertyValue) {
    Vector<Properties> vAux = new Vector<Properties>();
    if (this.structure==null) return vAux;
    for (Enumeration<Properties> e = this.structure.elements();e.hasMoreElements();) {
      Properties prop = e.nextElement();
      if (prop.getProperty(propertyName).equals(propertyValue)) vAux.addElement(prop);
    }
    return vAux;
  }

  public void setIndex(int _index) {
    this.index = _index;
  }

  public int getIndex() {
    return this.index;
  }

  public void addSelectField(String _field, String _alias) {
    addSelectField(_field, _alias, false);
  }

  public void addSelectField(String _field, String _alias, boolean _new) {
    QueryFieldStructure p = new QueryFieldStructure(_field, " AS ", _alias, "SELECT");
    if (this.select == null) this.select = new Vector<QueryFieldStructure>();
    else {
      QueryFieldStructure pOld = getSelectFieldElement(_alias);
      if (pOld!=null) {
        p = new QueryFieldStructure(pOld.getField() + FIELD_CONCAT + _field, " AS ", _alias, "SELECT");
        this.select.remove(pOld);
      }
    }
    this.select.addElement(p);
  }

  public QueryFieldStructure getSelectFieldElement(String _alias) {
    if (this.select == null) return null;
    for (int i=0;i<this.select.size();i++) {
      QueryFieldStructure p = this.select.elementAt(i);
      if (p.getAlias().equalsIgnoreCase(_alias)) return p;
    }
    return null;
  }

  public String getSelectField(String _alias) {
    if (this.select == null) return "";
    for (int i=0;i<this.select.size();i++) {
      QueryFieldStructure p = this.select.elementAt(i);
      if (p.getAlias().equalsIgnoreCase(_alias)) return p.getField();
    }
    return "";
  }

  public Vector<QueryFieldStructure> getSelectFields() {
    return this.select;
  }

  public void addFromField(String _field, String _alias) {
    QueryFieldStructure p = new QueryFieldStructure(_field, " ", _alias, "FROM");
    if (this.from == null) this.from = new Vector<QueryFieldStructure>();
    from.addElement(p);
  }

  public Vector<QueryFieldStructure> getFromFields() {
    return this.from;
  }

  public void addWhereField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.where == null) this.where = new Vector<QueryFieldStructure>();
    where.addElement(p);
  }

  public Vector<QueryFieldStructure> getWhereFields() {
    return this.where;
  }

  public void addFilterField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.filter == null) this.filter = new Vector<QueryFieldStructure>();
    filter.addElement(p);
  }

  public Vector<QueryFieldStructure> getFilterFields() {
    return this.filter;
  }

  public void addInternalFilterField(String _field, String _type) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", _type);
    if (this.internalFilter == null) this.internalFilter = new Vector<QueryFieldStructure>();
    internalFilter.addElement(p);
  }

  public Vector<QueryFieldStructure> getInternalFilterFields() {
    return this.internalFilter;
  }

  public void addOrderByField(String _field) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", "ORDERBY");
    if (this.orderBy == null) this.orderBy = new Vector<QueryFieldStructure>();
    this.orderBy.addElement(p);
  }

  public Vector<QueryFieldStructure> getOrderByFields() {
    return this.orderBy;
  }

  public void addInternalOrderByField(String _field) {
    QueryFieldStructure p = new QueryFieldStructure(_field, "", "", "ORDERBY");
    if (this.internalOrderBy == null) this.internalOrderBy = new Vector<QueryFieldStructure>();
    this.internalOrderBy.addElement(p);
  }

  public Vector<QueryFieldStructure> getInternalOrderByFields() {
    return this.internalOrderBy;
  }

  public void addSelectParameter(String _parameter, String _fieldName) {
    if (this.paramSelect == null) this.paramSelect = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "SELECT");
    this.paramSelect.addElement(aux);
  }

  public Vector<QueryParameterStructure> getSelectParameters() {
    return this.paramSelect;
  }

  public void addFromParameter(String _parameter, String _fieldName) {
    if (this.paramFrom == null) this.paramFrom = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "FROM");
    this.paramFrom.addElement(aux);
  }

  public Vector<QueryParameterStructure> getFromParameters() {
    return this.paramFrom;
  }

  public void addWhereParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramWhere == null) this.paramWhere = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramWhere.addElement(aux);
  }

  public Vector<QueryParameterStructure> getWhereParameters() {
    return this.paramWhere;
  }

  public void addFilterParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramFilter == null) this.paramFilter = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramFilter.addElement(aux);
  }

  public Vector<QueryParameterStructure> getFilterParameters() {
    return this.paramFilter;
  }

  public void addInternalFilterParameter(String _parameter, String _fieldName, String _type) {
    if (this.paramInternalFilter == null) this.paramInternalFilter = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, _type);
    this.paramInternalFilter.addElement(aux);
  }

  public Vector<QueryParameterStructure> getInternalFilterParameters() {
    return this.paramInternalFilter;
  }

  public void addOrderByParameter(String _parameter, String _fieldName) {
    if (this.paramOrderBy == null) this.paramOrderBy = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "ORDERBY");
    this.paramOrderBy.addElement(aux);
  }

  public Vector<QueryParameterStructure> getOrderByParameters() {
    return this.paramOrderBy;
  }

  public void addInternalOrderByParameter(String _parameter, String _fieldName) {
    if (this.paramInternalOrderBy == null) this.paramInternalOrderBy = new Vector<QueryParameterStructure>();
    QueryParameterStructure aux = new QueryParameterStructure(_parameter, _fieldName, "ORDERBY");
    this.paramInternalOrderBy.addElement(aux);
  }

  public Vector<QueryParameterStructure> getInternalOrderByParameters() {
    return this.paramInternalOrderBy;
  }

  public String parseContext(String context, String type) {
    if (context==null || context.equals("")) return "";
    StringBuffer strOut = new StringBuffer();
    String value = new String(context);
    String token, defStr;
    int i = value.indexOf("@");
    while (i!=-1) {
      strOut.append(value.substring(0,i));
      value = value.substring(i+1);
      int j=value.indexOf("@");
      if (j==-1) {
        strOut.append(value);
        return strOut.toString();
      }
      token = value.substring(0, j);
      if (token.equalsIgnoreCase("#User_Client")) defStr=getClientList();
      else if (token.equalsIgnoreCase("#User_Org")) defStr=getOrgList();
      else defStr="?";
      
      if (defStr.equals("?")) {
        if (type.equalsIgnoreCase("WHERE")) addWhereParameter(token, type.toUpperCase(), type.toUpperCase());
        else if (type.equalsIgnoreCase("FILTER")) addInternalFilterParameter(token, type.toUpperCase(), type.toUpperCase());
        else if (type.equalsIgnoreCase("INTERNAL_ORDERBY")) addInternalOrderByParameter(token, "ORDERBY");
      }
      strOut.append(defStr);
      value=value.substring(j+1);
      i=value.indexOf("@");
    }
    strOut.append(value);
    return strOut.toString().replace("'?'","?");
  }

  private void generateStructure() throws Exception {
    if (getPool()==null) throw new Exception("No pool defined for database connection");
    else if (getTabID().equals("")) throw new Exception("No Tab defined");
    TableSQLQueryData[] data = TableSQLQueryData.selectStructure(getPool(), getTabID(), getVars().getLanguage());
    if (data==null || data.length==0) throw new Exception("Couldn't get structure for tab " + getTabID());
    String primaryKey = "";
    String secondaryKey = "";
    for (int i=0;i<data.length;i++) {
      Properties prop = new Properties();
      prop.setProperty("ColumnName", data[i].columnname);
      prop.setProperty("AD_Reference_ID", data[i].adReferenceId);
      prop.setProperty("AD_Reference_Value_ID", data[i].adReferenceValueId);
      prop.setProperty("AD_Val_Rule_ID", data[i].adValRuleId);
      prop.setProperty("FieldLength", data[i].fieldlength);
      prop.setProperty("DefaultValue", data[i].defaultvalue);
      prop.setProperty("IsKey", data[i].iskey);
      prop.setProperty("IsParent", data[i].isparent);
      prop.setProperty("IsMandatory", data[i].ismandatory);
      prop.setProperty("IsUpdateable", data[i].isupdateable);
      prop.setProperty("ReadOnlyLogic", data[i].readonlylogic);
      prop.setProperty("IsIdentifier", data[i].isidentifier);
      prop.setProperty("SeqNo", data[i].seqno);
      prop.setProperty("IsTranslated", data[i].istranslated);
      prop.setProperty("IsEncrypted", data[i].isencrypted);
      prop.setProperty("VFormat", data[i].vformat);
      prop.setProperty("ValueMin", data[i].valuemin);
      prop.setProperty("ValueMax", data[i].valuemax);
      prop.setProperty("IsSelectionColumn", data[i].isselectioncolumn);
      prop.setProperty("AD_Process_ID", data[i].adProcessId);
      prop.setProperty("IsSessionAttr", data[i].issessionattr);
      prop.setProperty("IsSecondaryKey", data[i].issecondarykey);
      prop.setProperty("IsDesencryptable", data[i].isdesencryptable);
      prop.setProperty("AD_CallOut_ID", data[i].adCalloutId);
      prop.setProperty("Name", data[i].name);
      prop.setProperty("AD_FieldGroup_ID", data[i].adFieldgroupId);
      prop.setProperty("IsDisplayed", data[i].isdisplayed);
      prop.setProperty("DisplayLogic", data[i].displaylogic);
      prop.setProperty("DisplayLength", data[i].displaylength);
      prop.setProperty("IsReadOnly", data[i].isreadonly);
      prop.setProperty("SortNo", data[i].sortno);
      prop.setProperty("IsSameLine", data[i].issameline);
      prop.setProperty("IsHeading", data[i].isheading);
      prop.setProperty("IsFieldOnly", data[i].isfieldonly);
      prop.setProperty("ShowInRelation", data[i].showinrelation);
      prop.setProperty("ColumnNameSearch", data[i].columnnameSearch);
      addStructure(prop);
      String parentKey = getParentColumnName();
      if (parentKey==null) parentKey = "";
      if (primaryKey.equals("") && data[i].iskey.equals("Y") && (getIsSameTable() || !data[i].columnname.equalsIgnoreCase(parentKey))) {
        primaryKey = data[i].columnname;
      } else if (secondaryKey.equals("") && data[i].issecondarykey.equals("Y") && (getIsSameTable() || !data[i].columnname.equalsIgnoreCase(parentKey))) {
        secondaryKey = data[i].columnname;
      }
    }
    if (!primaryKey.equals("")) setParameter(internalPrefix + "KeyColumn", primaryKey);
    else if (!secondaryKey.equals("")) setParameter(internalPrefix + "KeyColumn", secondaryKey);
    else {
      primaryKey = TableSQLQueryData.columnNameKey(getPool(), getTabID());
      if (!primaryKey.equals("")) setParameter(internalPrefix + "KeyColumn", primaryKey);
      else throw new Exception("No column key defined for this tab");
    }
  }

  public void generateSQL() throws Exception {
    if (getPool()==null) throw new Exception("No pool defined for database connection");
    Vector<Properties> headers = getStructure();
    if (headers==null || headers.size()==0) throw new Exception("No structure defined");
    addFromField(getTableName(), getTableName());
    for (Enumeration<Properties> e = headers.elements();e.hasMoreElements();) {
      Properties prop = e.nextElement();
      switch (Integer.valueOf(prop.getProperty("AD_Reference_ID")).intValue()) {
        case 17: //List
        case 18: //Table
        case 19: //TableDir
        case 30: //Search
        case 31: //Locator
        case 35:
        case 25: //Account
        case 800011: //Product Search
          addSelectField(getTableName() + "." + prop.getProperty("ColumnName"), prop.getProperty("ColumnName"));
          identifier(getTableName(), prop, prop.getProperty("ColumnName") + "_R");
          break;
        default:
          identifier(getTableName(), prop, prop.getProperty("ColumnName"));
          break;
      }
    }
    setWindowFilters();
  }

  public void identifier(String parentTableName, Properties field, String identifierName) throws Exception {
    String reference;
    if (field==null) return;
    else reference = field.getProperty("AD_Reference_ID");
    switch (Integer.valueOf(reference).intValue()) {
      case 17: //List
        setListQuery(parentTableName, field.getProperty("ColumnName"), field.getProperty("AD_Reference_Value_ID"), identifierName);
        break;
      case 18: //Table
        setTableQuery(parentTableName, field.getProperty("ColumnName"), field.getProperty("AD_Reference_Value_ID"), identifierName);
        break;
      case 19: //TableDir
        setTableDirQuery(parentTableName, field.getProperty("ColumnNameSearch"), field.getProperty("ColumnName"), field.getProperty("AD_Reference_Value_ID"), identifierName);
        break;
      case 35: //PAttribute
      case 30: //Search
        setTableDirQuery(parentTableName, field.getProperty("ColumnNameSearch"), field.getProperty("ColumnName"), field.getProperty("AD_Reference_Value_ID"), identifierName);
        break;
      case 32: //Image
        setImageQuery(parentTableName, field.getProperty("ColumnNameSearch"), identifierName);
        break;
      case 28: //Button
        if (field.getProperty("AD_Reference_Value_ID")!=null && !field.getProperty("AD_Reference_Value_ID").equals("")) {
          setListQuery(parentTableName, field.getProperty("ColumnName"), field.getProperty("AD_Reference_Value_ID"), identifierName);
        } else addSelectField(formatField((parentTableName + "." + field.getProperty("ColumnName")), reference), identifierName);
        break;
      default:
        if (!checkTableTranslation(parentTableName, field, reference, identifierName)) {
          addSelectField(formatField((parentTableName + "." + field.getProperty("ColumnName")), reference), identifierName);
        }
        break;
    }
  }

  private boolean checkTableTranslation(String tableName, Properties field, String reference, String identifierName) throws Exception {
    if (tableName==null || tableName.equals("") || field==null) return false;
    ComboTableQueryData[] data = ComboTableQueryData.selectTranslatedColumn(getPool(), field.getProperty("TableName"), field.getProperty("ColumnName"));
    if (data==null || data.length==0) return false;
    int myIndex = this.index++;
    addSelectField("(CASE WHEN td_trl" + myIndex + "." + data[0].columnname + " IS NULL THEN " + formatField((tableName + "." + field.getProperty("ColumnName")), reference) + " ELSE " + formatField(("td_trl" + myIndex + "." + data[0].columnname), reference) + " END)", identifierName);
    addFromField("(SELECT AD_Language, " + data[0].reference + ", " + data[0].columnname + " FROM " + data[0].tablename + ") td_trl" + myIndex + " on " + tableName + "." + data[0].reference + " = td_trl" + myIndex + "." + data[0].reference + " AND td_trl" + myIndex + ".AD_Language = ?", "td_trl" + myIndex);
    addFromParameter("#AD_LANGUAGE", "LANGUAGE");
    return true;
  }
  
  private String formatField(String field, String reference) {
    String result = "";
    if (field==null) return "";
    else if (reference==null || reference.length()==0) return field;
    switch (Integer.valueOf(reference).intValue()) {
    case 11: //INTEGER
      result = "CAST(" + field + " AS INTEGER)";
      break;
    case 12: //AMOUNT
    case 22: //NUMBER
    case 26: //ROWID
    case 29: //QUANTITY
    case 800008: //PRICE
    case 800019: //GENERAL QUANTITY
      result = "TO_NUMBER(" + field + ")";
      break;
    case 15: //DATE
      result = "TO_CHAR(" + field + (getVars()==null?"":(", '" + getVars().getSessionValue("#AD_SqlDateFormat") + "'")) + ")";
      break;
    case 16: //DATETIME
      result = "(" + field + ")";
      break;
    case 24: //TIME
      result = "TO_CHAR(" + field + ", 'HH24:MI:SS')";
      break;
    case 20: //YESNO
      result = "COALESCE(" + field + ", 'N')";
      break;
    case 23: //Binary
      result = field;
      break;
    default:
      result = "TO_CHAR(" + field + ")";
      break;
    }
    return result;
  }

  private Properties fieldToProperties(FieldProvider field) throws Exception {
    Properties aux = new Properties();
    if (field!=null) {
      aux.setProperty("ColumnName", field.getField("name"));
      aux.setProperty("TableName", field.getField("tablename"));
      aux.setProperty("AD_Reference_ID", field.getField("reference"));
      aux.setProperty("AD_Reference_Value_ID", field.getField("referencevalue"));
      aux.setProperty("IsMandatory", field.getField("required"));
      aux.setProperty("ColumnNameSearch", field.getField("columnname"));
    }
    return aux;
  }

  private void setImageQuery(String tableName, String fieldName, String identifierName) throws Exception {
    int myIndex = this.index++;
    addSelectField("((CASE td" + myIndex + ".isActive WHEN 'N' THEN '" + INACTIVE_DATA + "' ELSE '' END) || td" + myIndex + ".imageURL)", identifierName);
    String tables = "(select IsActive, AD_Image_ID, ImageURL from AD_Image) td" + myIndex;
    tables += " on " + tableName + "." + fieldName;
    tables += " = td" + myIndex + ".AD_Image_ID";
    addFromField(tables, "td" + myIndex);
  }

  private void setListQuery(String tableName, String fieldName, String referenceValue, String identifierName) throws Exception {
    int myIndex = this.index++;
    addSelectField("((CASE td" + myIndex + ".isActive WHEN 'N' THEN '" + INACTIVE_DATA + "' ELSE '' END) || (CASE WHEN td_trl" + myIndex + ".name IS NULL THEN td" + myIndex + ".name ELSE td_trl" + myIndex + ".name END))", identifierName);
    String tables = "(select IsActive, ad_ref_list_id, ad_reference_id, value, name from ad_ref_list) td" + myIndex;
    tables += " on ";
    if (fieldName.equalsIgnoreCase("DocAction")) tables += "(CASE " + tableName + "." + fieldName + " WHEN '--' THEN 'CL' ELSE TO_CHAR(" + tableName + "." + fieldName + ") END)";
    else tables += tableName + "." + fieldName;
    tables += " = td" + myIndex + ".value AND td" + myIndex + ".ad_reference_id = ?";
    addFromField(tables, "td" + myIndex);
    addFromParameter("TD" + myIndex + ".AD_REFERENCE_ID", "KEY");
    setParameter("TD" + myIndex + ".AD_REFERENCE_ID", referenceValue);
    addFromField("(SELECT ad_language, name, ad_ref_list_id from ad_ref_list_trl) td_trl" + myIndex + " on td" + myIndex + ".ad_ref_list_id = td_trl" + myIndex + ".ad_ref_list_id AND td_trl" + myIndex + ".ad_language = ?", "td_trl" + myIndex);
    addFromParameter("#AD_LANGUAGE", "LANGUAGE");
  }

  private void setTableQuery(String tableName, String fieldName, String referenceValue, String identifierName) throws Exception {
    int myIndex = this.index++;
    ComboTableQueryData trd[] = ComboTableQueryData.selectRefTable(getPool(), referenceValue);
    if (trd==null || trd.length==0) return;
    String tables = "(SELECT ";
    if (trd[0].isvaluedisplayed.equals("Y")) {
      addSelectField("td" + myIndex + ".VALUE", identifierName);
      tables += "value, ";
    }
    tables += trd[0].keyname + ", " + trd[0].name + " FROM ";
    Properties fieldsAux = fieldToProperties(trd[0]);
    tables += trd[0].tablename + ") td" + myIndex;
    tables += " on " + tableName + "." + fieldName + " = td" + myIndex + "." + trd[0].keyname;
    addFromField(tables, "td" + myIndex);
    identifier("td" + myIndex, fieldsAux, identifierName);
  }

  private void setTableDirQuery(String tableName, String fieldName, String parentFieldName, String referenceValue, String identifierName) throws Exception {
    int myIndex = this.index++;
    String name = fieldName;
    String tableDirName = name.substring(0,name.length()-3);
    if (referenceValue!=null && !referenceValue.equals("")) {
      TableSQLQueryData[] search = TableSQLQueryData.searchInfo(getPool(), referenceValue);
      if (search!=null && search.length!=0) {
        name = search[0].columnname;
        tableDirName = search[0].tablename;
      }
    }
    ComboTableQueryData trd[] = ComboTableQueryData.identifierColumns(getPool(), tableDirName);
    String tables = "(SELECT " + name;
    for (int i=0;i<trd.length;i++) tables += ", " + trd[i].name;
    tables += " FROM ";
    tables += tableDirName + ") td" + myIndex;
    tables += " on " + tableName + "." + parentFieldName + " = td" + myIndex + "." + name + "\n";
    addFromField(tables, "td" + myIndex);
    for (int i=0;i<trd.length;i++) identifier("td" + myIndex, fieldToProperties(trd[i]), identifierName);
  }

  private int findCloseTarget(String text, int pos, String openChar, String closeChar) {
    if (text==null || text.equals("")) return -1;
    int nextOpen = text.indexOf(openChar, pos);
    int nextClose = text.indexOf(closeChar, pos);
    if (nextClose!=-1) {
      while (pos!=-1 && nextClose!=-1 && (nextOpen!=-1 && nextClose>nextOpen)) {
        pos = findCloseTarget(text, nextOpen+1, openChar, closeChar);
        nextOpen = text.indexOf(openChar, pos);
        nextClose = text.indexOf(closeChar, pos);
      }
      if (pos==-1) nextClose = -1;
    }
    return nextClose;
  }

  private Vector<String> getOrdeByIntoFields(String text) {
    Vector<String> result = new Vector<String>();
    if (text!=null && !text.equals("")) {
      int lastPos = 0;
      int openPos = text.indexOf("(");
      int actualPos = text.indexOf(",");
      while (actualPos!=-1) {
        if (actualPos>openPos) openPos = findCloseTarget(text, openPos+1, "(", ")");
        if (openPos == -1) {
          log4j.error("Parsing failed on orderby clause: " + text + " - pos: " + Integer.toString(actualPos));
          result.addElement(text.substring(lastPos));
          lastPos=-1;
          actualPos=-1;
        } else {
          result.addElement(text.substring(lastPos, actualPos));
          lastPos = actualPos+1;
          openPos = text.indexOf("(", lastPos);
          actualPos = text.indexOf(",", lastPos);
        }
      }
      if (lastPos!=-1 && lastPos<text.length()) result.addElement(text.substring(lastPos));
    }
    return result;
  }

  private void setWindowFilters() throws Exception {
    String strWhereClause = getWhereClause();
    if (strWhereClause!=null && !strWhereClause.equals("")) {
      if (strWhereClause.indexOf("@")!=-1) strWhereClause = parseContext(strWhereClause, "WHERE");
      addWhereField(strWhereClause, "WHERE");
    }
    if (!getTableName().toUpperCase().endsWith("_ACCESS")) {
      addWhereField(getTableName() + ".AD_Client_ID IN (" + getClientList() + ")", "WHERE");
      addWhereField(getTableName() + ".AD_Org_ID IN (" + getOrgList() + ")", "WHERE");
    }
    String parentKey = getParentColumnName();
    if (parentKey!=null && !parentKey.equals("")) {
      addWhereField(getTableName() + "." + parentKey + " = ?", "PARENT");
      addWhereParameter("PARENT", "PARENT", "PARENT");
    }
    String strOrderByClause = getOrderByClause();
    if (strOrderByClause==null || strOrderByClause.equals("")) getFieldsOrderByClause();
    else {
      if (strOrderByClause.indexOf("@")!=-1) strOrderByClause = parseContext(strOrderByClause, "INTERNAL_ORDERBY");
      Vector<String> vecOrdersAux = getOrdeByIntoFields(strOrderByClause);
      if (vecOrdersAux!=null && vecOrdersAux.size()>0) {
        for (int i=0;i<vecOrdersAux.size();i++) addInternalOrderByField(getRealOrderByColumn(vecOrdersAux.elementAt(i)));
      } else addInternalOrderByField(getRealOrderByColumn(strOrderByClause));
    }
    String strFilterClause = getFilterClause();
    if (strFilterClause != null && !strFilterClause.equals("")) {
      if (strFilterClause.indexOf("@")!=-1) strFilterClause = parseContext(strFilterClause, "FILTER");
      addInternalFilterField(strFilterClause, "FILTER");
    }
    if (getWindowType().equalsIgnoreCase("T") && getTabLevel().equals("0")) {
      addInternalFilterField("(COALESCE(" + getTableName() + ".Processed, 'N')='N' OR " + getTableName() + ".Updated > now()-TO_NUMBER(?))", "FILTER");
      addInternalFilterParameter("#Transactional$Range", "FILTER", "FILTER");
    }
  }

  private void getFieldsOrderByClause() throws Exception {
    TableSQLQueryData[] orderByData = TableSQLQueryData.selectOrderByFields(getPool(), getTabID());
    if (orderByData==null) return;
    for (int i=0;i<orderByData.length;i++) {
      addInternalOrderByField(getTableName() + "." + orderByData[i].columnname + " " + (orderByData[i].columnname.equalsIgnoreCase("DocumentNo")?"DESC":"ASC"));
    }
  }

  private String getRealOrderByColumn(String data) {
    if (data==null || data.equals("")) return "";
    data = data.trim();
    String orderDirection = "ASC";
    String orderField = data;
    int pos = data.toUpperCase().lastIndexOf(" DESC");
    if (pos==-1 || pos!= (data.length()-5)) {
      pos = data.toUpperCase().lastIndexOf(" ASC");
      if (pos!=-1 && pos== (data.length()-4)) {
        orderField = data.substring(0,pos);
      } else return data;
    } else {
      orderField = data.substring(0,pos);
      orderDirection = "DESC";
    }
    String _alias = getSelectFieldAlias(orderField);
    if (!_alias.equals("") && this.structure!=null) {
      boolean isTranslated = false;
      if (_alias.endsWith("_R")) {
        isTranslated = true;
        _alias = _alias.substring(0, _alias.length()-2);
      }
      int position = -1;
      String reference = "";
      Properties myProps = getColumnPosition(_alias);
      if (myProps != null) {
        position = Integer.valueOf(myProps.getProperty("Position")).intValue();
        reference = myProps.getProperty("AD_Reference_ID");
      }
      if (position!=-1) {
        addOrderByPosition(Integer.toString(position));
        addOrderByDirection(orderDirection);
      }
      if (!isTranslated) {
        String aux = getSelectField(_alias + "_R");
        if (aux!=null && !aux.equals("")) orderField = aux;
      }
      if (reference.equals("15") || reference.equals("16") || reference.equals("24")) {
        orderField = "TO_DATE(" + orderField + ")";
      }
    }
    return orderField + " " + orderDirection;
  }

  public SQLReturnObject[] getHeaders() {
    return getHeaders(false);
  }

  public SQLReturnObject[] getHeaders(boolean withoutIdentifiers) {
    SQLReturnObject[] data = null;
    Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();
    Vector<Properties> structure = getStructure();
    for (Enumeration<Properties> e = structure.elements();e.hasMoreElements();) {
      Properties prop = e.nextElement();
      if (prop.getProperty("IsKey").equals("Y") || prop.getProperty("IsSecondaryKey").equals("Y") || (prop.getProperty("IsDisplayed").equals("Y") && prop.getProperty("ShowInRelation").equals("Y") && prop.getProperty("IsEncrypted").equals("N") && !prop.getProperty("ColumnName").equals(getParentColumnName()))) {
        SQLReturnObject dataAux = new SQLReturnObject();
                
        boolean cloneRecord = (!withoutIdentifiers && (prop.getProperty("IsDisplayed").equals("Y") && prop.getProperty("ShowInRelation").equals("Y")) && prop.getProperty("ColumnName").equals(getKeyColumn()) && !getSelectField(prop.getProperty("ColumnName") + "_R").equals(""));
        
        dataAux.setData("columnname", prop.getProperty("ColumnName"));
        dataAux.setData("gridcolumnname", prop.getProperty("ColumnName"));
        dataAux.setData("adReferenceId", prop.getProperty("AD_Reference_ID"));
        dataAux.setData("adReferenceValueId", prop.getProperty("AD_Reference_Value_ID"));
        dataAux.setData("isidentifier", (prop.getProperty("IsIdentifier").equals("Y")?"true":"false"));
        dataAux.setData("iskey", (prop.getProperty("ColumnName").equals(getKeyColumn()) && !cloneRecord)?"true":"false");
        dataAux.setData("isvisible", ((prop.getProperty("IsDisplayed").equals("Y") && prop.getProperty("ShowInRelation").equals("Y"))?"true":"false"));
        dataAux.setData("name", prop.getProperty("Name"));
        String type = "string";
        if (prop.getProperty("AD_Reference_ID").equals("17") || prop.getProperty("AD_Reference_ID").equals("18") || prop.getProperty("AD_Reference_ID").equals("19")) { 
          type="dynamicEnum";
        } else if (prop.getProperty("AD_Reference_ID").equals("800101")) {
          type="url";
        } else if (prop.getProperty("AD_Reference_ID").equals("32")) {
          type="img";
        }
        dataAux.setData("type", type);
        String strWidth = prop.getProperty("DisplayLength");
        if (strWidth==null || strWidth.equals("")) strWidth = "0";
        int width = Integer.valueOf(strWidth).intValue();
        width = width * 6;
        if (width<23) width=23;
        else if (width>300) width=300;
        dataAux.setData("width", Integer.toString(width));
        
        if (cloneRecord) {
          dataAux.setData("isvisible", "true");
          dataAux.setData("gridcolumnname", prop.getProperty("ColumnName"));
        }
        vAux.addElement(dataAux);
        if (cloneRecord) vAux.addElement(getClone(dataAux));
      }
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }
  
  public SQLReturnObject getClone(SQLReturnObject data) {
    SQLReturnObject dataAux = new SQLReturnObject();
    dataAux.setData("columnname", data.getData("columnname"));
    dataAux.setData("gridcolumnname", "keyname");
    dataAux.setData("adReferenceId", data.getData("adReferenceId"));
    dataAux.setData("adReferenceValueId", data.getData("adReferenceValueId"));
    dataAux.setData("isidentifier", data.getData("isidentifier"));
    dataAux.setData("iskey", "true");
    dataAux.setData("isvisible", "false");
    dataAux.setData("name", data.getData("name"));
    dataAux.setData("type", data.getData("type"));
    dataAux.setData("width", data.getData("width"));
    return dataAux;
  }

  public Properties getColumnPosition(String _name) {
    SQLReturnObject[] vAux = getHeaders();
    Properties _prop = new Properties();
    for (int i = 0;i<vAux.length;i++) {
      if (vAux[i].getData("columnname").equalsIgnoreCase(_name) || (getTableName() + "." + vAux[i].getData("columnname")).equalsIgnoreCase(_name)) {
        _prop.setProperty("AD_Reference_ID", vAux[i].getData("adReferenceId"));
        _prop.setProperty("Position", Integer.toString(i));
        return _prop;
      }
    }
    return null;
  }

  public String getSelectFieldAlias(String _data) {
    if (this.select == null) return "";
    for (int i=0;i<this.select.size();i++) {
      QueryFieldStructure p = this.select.elementAt(i);
      if (p.getField().equalsIgnoreCase(_data)) return p.getAlias();
      else if (p.getAlias().equalsIgnoreCase(_data)) return p.getAlias();
      else if ((getTableName() + "." + p.getAlias()).equalsIgnoreCase(_data.trim())) return p.getAlias();
    }
    return "";
  }

  public void setOrderBy(Vector<String> _fields, Vector<String> _params) {
    this.orderBy = new Vector<QueryFieldStructure>();
    this.paramOrderBy = new Vector<QueryParameterStructure>();
    if (_fields==null || _fields.size()==0) {
      if (this.internalOrderBy!=null) {
        for (int i=0;i<this.internalOrderBy.size();i++) {
          QueryFieldStructure aux = this.internalOrderBy.elementAt(i);
          addOrderByField(getRealOrderByColumn(aux.getField()));
        }
        if (this.paramInternalOrderBy!=null) {
          for (int i=0;i<this.paramInternalOrderBy.size();i++) {
            QueryParameterStructure aux = this.paramInternalOrderBy.elementAt(i);
            addOrderByParameter(aux.getName(), aux.getField());
          }
        }
      }
    } else {
      for (int i=0;i<_fields.size();i++) {
        String strOrderByClause = _fields.elementAt(i);
        Vector<String> vecOrdersAux = getOrdeByIntoFields(strOrderByClause);
        if (vecOrdersAux!=null && vecOrdersAux.size()>0) {
          for (int j=0;j<vecOrdersAux.size();j++) addOrderByField(getRealOrderByColumn(vecOrdersAux.elementAt(j)));
        } else addOrderByField(getRealOrderByColumn(strOrderByClause));
      }
      if (_params!=null)
        for (int i=0;i<_params.size();i++) addOrderByParameter(_params.elementAt(i), _params.elementAt(i));
    }
  }

  public void setFilter(Vector<String> _fields, Vector<String> _params) {
    this.filter = new Vector<QueryFieldStructure>();
    this.paramFilter = new Vector<QueryParameterStructure>();
    if (_fields==null || _fields.size()==0) {
      if (this.internalFilter!=null) {
        for (int i=0;i<this.internalFilter.size();i++) {
          QueryFieldStructure aux = this.internalFilter.elementAt(i);
          addFilterField(aux.getField(), aux.getType());
        }
        if (this.paramInternalFilter!=null) {
          for (int i=0;i<this.paramInternalFilter.size();i++) {
            QueryParameterStructure aux = this.paramInternalFilter.elementAt(i);
            addFilterParameter(aux.getName(), aux.getField(), aux.getType());
          }
        }
      }
    } else {
      for (int i=0;i<_fields.size();i++) addFilterField(_fields.elementAt(i), "FILTER");
      if (_params!=null)
        for (int i=0;i<_params.size();i++) addFilterParameter(_params.elementAt(i), _params.elementAt(i), "FILTER");
    }
  }

  public String getSQL() {
    return getSQL(null, null, null, null, null);
  }

  public String getSQL(Vector<String> _FilterFields, Vector<String> _FilterParams, Vector<String> _OrderFields, Vector<String> _OrderParams, String selectFields) {
    StringBuffer text = new StringBuffer();
    boolean hasWhere = false;
    Vector<QueryFieldStructure> aux = null;
    if (selectFields==null || selectFields.equals("")) {
      aux = getSelectFields();
      if (aux!=null) {
        text.append("SELECT ");
        for (int i=0;i<aux.size();i++) {
          QueryFieldStructure auxStructure = aux.elementAt(i);
          if (i>0) text.append(", ");
          text.append(auxStructure.toString(true)).append(" ");
        }
        text.append(" \n");
      }
    } else {
      text.append("SELECT ").append(selectFields).append("\n");
    }
    
    aux = getFromFields();
    if (aux!=null) {
      StringBuffer txtAux = new StringBuffer();
      text.append("FROM ");
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!txtAux.toString().equals("")) txtAux.append("left join ");
        txtAux.append(auxStructure.toString()).append(" \n");
      }
      text.append(txtAux.toString());
    }
    
    aux = getWhereFields();
    StringBuffer txtAuxWhere = new StringBuffer();
    if (aux!=null) {
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        hasWhere=true;
        if (!txtAuxWhere.toString().equals("")) txtAuxWhere.append("AND ");
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }
    setFilter(_FilterFields, _FilterParams);
    aux = getFilterFields();
    if (aux!=null) {
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        hasWhere=true;
        if (!txtAuxWhere.toString().equals("")) txtAuxWhere.append("AND ");
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }
    if (hasWhere) text.append("WHERE ").append(txtAuxWhere.toString());
    setOrderBy(_OrderFields, _OrderParams);
    aux = getOrderByFields();
    if (aux!=null) {
      StringBuffer txtAux = new StringBuffer();
      text.append("ORDER BY ");
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!txtAux.toString().equals("")) txtAux.append(", ");
        txtAux.append(auxStructure.toString());
      }
      if (!txtAux.toString().equals("")) txtAux.append(", ");
      txtAux.append(getTableName()).append(".").append(getKeyColumn());
      text.append(txtAux.toString());
    }
    return text.toString();
  }

  public String getTotalSQL() {
    StringBuffer text = new StringBuffer();
    Vector<QueryFieldStructure> aux = null;
    boolean hasWhere = false;
    text.append("SELECT COUNT(*) AS TOTAL ");
    
    aux = getFromFields();
    if (aux!=null) {
      StringBuffer txtAux = new StringBuffer();
      text.append("FROM ");
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        if (!txtAux.toString().equals("")) txtAux.append("left join ");
        txtAux.append(auxStructure.toString()).append(" \n");
      }
      text.append(txtAux.toString());
    }
    
    aux = getWhereFields();
    StringBuffer txtAuxWhere = new StringBuffer();
    if (aux!=null) {
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        hasWhere=true;
        if (!txtAuxWhere.toString().equals("")) txtAuxWhere.append("AND ");
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }
    aux = getFilterFields();
    if (aux!=null) {
      for (int i=0;i<aux.size();i++) {
        QueryFieldStructure auxStructure = aux.elementAt(i);
        hasWhere=true;
        if (!txtAuxWhere.toString().equals("")) txtAuxWhere.append("AND ");
        txtAuxWhere.append(auxStructure.toString()).append(" \n");
      }
    }
    if (hasWhere) text.append("WHERE ").append(txtAuxWhere.toString());
    
    return text.toString();
  }
}
