/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2011-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.Parameter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.ReferencedTable;
import org.openbravo.model.ad.domain.ReferencedTree;
import org.openbravo.model.ad.domain.ReferencedTreeField;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.userinterface.selector.Selector;
import org.openbravo.userinterface.selector.SelectorField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class caches some AD structures used by the Form Initialization component. Basically, it
 * caches: AD components (fields, columns, auxiliary inputs) and ComboTableData instances. This
 * caching occurs to obtain better performance in FIC computations. For this cache to be used, the
 * system needs to be on 'production' mode, that is, all the modules need to be not in development
 */
@SessionScoped
public class ApplicationDictionaryCachedStructures implements Serializable {
  private static final long serialVersionUID = 1L;

  private Map<String, Tab> tabMap = new HashMap<String, Tab>();
  private Map<String, Table> tableMap = new HashMap<String, Table>();
  private Map<String, List<Field>> fieldMap = new HashMap<String, List<Field>>();
  private Map<String, List<Column>> columnMap = new HashMap<String, List<Column>>();
  private Map<String, List<AuxiliaryInput>> auxInputMap = new HashMap<String, List<AuxiliaryInput>>();
  private Map<String, ComboTableData> comboTableDataMap = new ConcurrentHashMap<String, ComboTableData>();
  private Map<String, List<Parameter>> attMethodMetadataMap = new ConcurrentHashMap<String, List<Parameter>>();
  private List<String> initializedWindows = new ArrayList<String>();

  private static final Logger log = LoggerFactory
      .getLogger(ApplicationDictionaryCachedStructures.class);

  private boolean useCache;

  public ApplicationDictionaryCachedStructures() {
    // The cache will only be active when there are no modules in development in the system
    final String query = "select m from ADModule m where m.inDevelopment=true";
    final Query indevelMods = OBDal.getInstance().getSession().createQuery(query);
    useCache = indevelMods.list().size() == 0;
  }

  /**
   * In case caching is enabled, Tab for tabId is returned from cache if present. If it is not, this
   * tab and all the ones in the same window are initialized and cached.
   * <p>
   * Note as this method is in charge of doing the full initialization, it should be invoked before
   * any other getter in this class. Other case, partially initialized object could be cached, being
   * potentially harmful if obtained from another thread and tried to be initialized.
   * 
   * @param tabId
   *          , ID of the tab to look for
   * @return Tab for the tabId, from cache if it is enabled
   */
  public synchronized Tab getTab(String tabId) {
    log.debug("get tab {}", tabId);
    if (useCache() && tabMap.containsKey(tabId)) {
      log.debug("got tab {} from cache", tabId);
      return tabMap.get(tabId);
    }
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    if (!useCache()) {
      // not using cache, initialize just current tab and go
      return tab;
    } else {
      // using cache, do complete initialization
      initializeWindow(tab.getWindow().getId());
    }
    return tab;
  }

  /**
   * Initialized all the tabs for a given window
   */
  private void initializeWindow(String windowId) {
    if (!useCache() || initializedWindows.contains(windowId)) {
      return;
    }
    Window window = OBDal.getInstance().get(Window.class, windowId);
    for (Tab tab : window.getADTabList()) {
      initializeTab(tab);
    }
    initializedWindows.add(windowId);
  }

  /**
   * Initializes a tab and its related elements (table, fields, columns, auxiliary inputs and table
   * combo data). If cache is enabled, tab is obtained from cache if it is already present and if
   * not, it is put in cache after initialization
   * 
   * @param tab
   */
  private void initializeTab(Tab tab) {
    String tabId = tab.getId();
    initializeDALObject(tab);
    if (useCache()) {
      tabMap.put(tabId, tab);
    }
    // initialize other elements related with the tab
    getAuxiliarInputList(tabId);
    getFieldsOfTab(tabId);
    initializeDALObject(tab.getTable());
    getColumnsOfTable(tab.getTable().getId());
  }

  public Table getTable(String tableId) {
    if (useCache() && tableMap.containsKey(tableId)) {
      return tableMap.get(tableId);
    }
    Table table = OBDal.getInstance().get(Table.class, tableId);
    initializeDALObject(table);
    initializeDALObject(table.getADColumnList());
    if (useCache()) {
      tableMap.put(tableId, table);
    }
    return table;
  }

  public List<Field> getFieldsOfTab(String tabId) {
    if (useCache() && fieldMap.containsKey(tabId)) {
      return fieldMap.get(tabId);
    }
    Tab tab = getTab(tabId);
    String tableId = tab.getTable().getId();
    List<Field> fields = tab.getADFieldList();
    for (Field f : fields) {
      if (f.getColumn() == null) {
        continue;
      }
      initializeDALObject(f.getColumn());
      initializeColumn(f.getColumn());

      // Property fields can link to columns in a different table than tab's one, in this case
      // initialize table
      if (!tableId.equals(f.getColumn().getTable().getId())) {
        initializeDALObject(f.getColumn().getTable());
      }
    }
    if (useCache()) {
      fieldMap.put(tabId, fields);
    }
    return fields;
  }

  public List<Column> getColumnsOfTable(String tableId) {
    if (useCache() && columnMap.get(tableId) != null) {
      return columnMap.get(tableId);
    }
    Table table = getTable(tableId);
    List<Column> columns = table.getADColumnList();
    for (Column c : columns) {
      initializeColumn(c);
    }
    if (useCache()) {
      columnMap.put(tableId, columns);
    }
    return columns;
  }

  private void initializeColumn(Column c) {

    initializeDALObject(c.getValidation());
    if (c.getValidation() != null) {
      initializeDALObject(c.getValidation().getValidationCode());
    }
    if (c.getCallout() != null) {
      initializeDALObject(c.getCallout());
      initializeDALObject(c.getCallout().getADModelImplementationList());
      for (ModelImplementation imp : c.getCallout().getADModelImplementationList()) {
        initializeDALObject(imp);
      }
    }

    if (c.getReference() != null) {
      initializeDALObject(c.getReference());
      initializeReference(c.getReference());
    }
    if (c.getReferenceSearchKey() != null) {
      initializeReference(c.getReferenceSearchKey());
    }

  }

  private void initializeReference(Reference reference) {
    initializeDALObject(reference.getADReferencedTableList());
    for (ReferencedTable t : reference.getADReferencedTableList()) {
      initializeDALObject(t);
    }
    initializeDALObject(reference.getOBUISELSelectorList());
    for (Selector s : reference.getOBUISELSelectorList()) {
      initializeDALObject(s);
      SelectorField displayField = s.getDisplayfield();
      initializeDALObject(displayField);
    }
    for (ReferencedTree t : reference.getADReferencedTreeList()) {
      initializeDALObject(t);
      ReferencedTreeField displayField = t.getDisplayfield();
      initializeDALObject(displayField);
    }
    for (org.openbravo.model.ad.domain.List list : reference.getADListList()) {
      initializeDALObject(list);
    }
    initializeDALObject(reference.getOBUIAPPRefWindowList());
  }

  public List<AuxiliaryInput> getAuxiliarInputList(String tabId) {
    if (useCache() && auxInputMap.get(tabId) != null) {
      return auxInputMap.get(tabId);
    }
    Tab tab = getTab(tabId);
    initializeDALObject(tab.getADAuxiliaryInputList());
    List<AuxiliaryInput> auxInputs = new ArrayList<AuxiliaryInput>(tab.getADAuxiliaryInputList());
    for (AuxiliaryInput auxIn : auxInputs) {
      initializeDALObject(auxIn);
    }
    if (useCache()) {
      auxInputMap.put(tabId, auxInputs);
    }
    return auxInputs;
  }

  private synchronized void initializeDALObject(Object obj) {
    Hibernate.initialize(obj);
  }

  public ComboTableData getComboTableData(VariablesSecureApp vars, String ref, String colName,
      String objectReference, String validation, String orgList, String clientList) {
    String comboId = ref + colName + objectReference + validation + orgList + clientList;
    if (useCache() && comboTableDataMap.get(comboId) != null) {
      return comboTableDataMap.get(comboId);
    }
    ComboTableData comboTableData;
    try {
      comboTableData = new ComboTableData(vars, new DalConnectionProvider(false), ref, colName,
          objectReference, validation, orgList, clientList, 0);
    } catch (Exception e) {
      throw new OBException("Error while computing combo table data for column " + colName, e);
    }
    if (useCache() && comboTableData.canBeCached()) {
      comboTableDataMap.put(comboId, comboTableData);
    }
    return comboTableData;

  }

  /**
   * Gets the list of parameters associated to an Attachment Method and a Tab. The list is sorted so
   * the fixed parameters are returned first.
   * 
   * @param strAttMethodId
   *          active attachment method id
   * @param strTabId
   *          tab id to take metadata
   * @return List of parameters by attachment method and tab sorted by Fixed and Sequence Number
   *         where fixed parameters are first.
   */
  public List<Parameter> getMethodMetadataParameters(String strAttMethodId, String strTabId) {
    String strMethodTab = strAttMethodId + "-" + strTabId;
    if (useCache() && attMethodMetadataMap.get(strMethodTab) != null) {
      return attMethodMetadataMap.get(strMethodTab);
    }

    StringBuilder where = new StringBuilder();
    where.append(Parameter.PROPERTY_ATTACHMENTMETHOD + ".id = :attMethod");
    where.append(" and (" + Parameter.PROPERTY_TAB + " is null or " + Parameter.PROPERTY_TAB
        + ".id = :tab)");
    where.append(" order by CASE WHEN " + Parameter.PROPERTY_FIXED + " is true THEN 1 ELSE 2 END");
    where.append(" , " + Parameter.PROPERTY_SEQUENCENUMBER);
    final OBQuery<Parameter> qryParams = OBDal.getInstance().createQuery(Parameter.class,
        where.toString());
    qryParams.setNamedParameter("attMethod", strAttMethodId);
    qryParams.setNamedParameter("tab", strTabId);
    List<Parameter> metadatas = qryParams.list();
    for (Parameter metadata : metadatas) {
      initializeMetadata(metadata);
    }

    if (useCache()) {
      attMethodMetadataMap.put(strMethodTab, metadatas);
    }
    return metadatas;
  }

  private void initializeMetadata(Parameter metadata) {
    initializeDALObject(metadata);
    if (metadata.getReference() != null) {
      initializeDALObject(metadata.getReference());
      initializeReference(metadata.getReference());
    }
    if (metadata.getReferenceSearchKey() != null) {
      initializeReference(metadata.getReferenceSearchKey());
    }
  }

  private boolean useCache() {
    return useCache;
  }
}
