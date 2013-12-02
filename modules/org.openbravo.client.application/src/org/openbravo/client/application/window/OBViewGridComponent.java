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
 * All portions are Copyright (C) 2010-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.application.window.OBViewFieldHandler.OBViewField;
import org.openbravo.client.application.window.OBViewFieldHandler.OBViewFieldDefinition;
import org.openbravo.client.application.window.OBViewTab.ButtonField;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

/**
 * The backing bean for generating the OBViewGrid client-side representation.
 * 
 * @author mtaal
 */
public class OBViewGridComponent extends BaseTemplateComponent {

  private static final String DEFAULT_TEMPLATE_ID = "91DD63545B674BE8801E1FA4F48FF4C6";
  protected static final Map<String, String> TEMPLATE_MAP = new HashMap<String, String>();

  static {
    // Map: WindowType - Template
    TEMPLATE_MAP.put("OBUIAPP_PickAndExecute", "EE3A4F4E485D47CB8057B90C40D134A0");
  }

  private boolean applyTransactionalFilter = false;
  private Tab tab;
  private Entity entity;

  private OBViewTab viewTab;

  protected Template getComponentTemplate() {
    final String windowType = tab.getWindow().getWindowType();
    if (TEMPLATE_MAP.containsKey(windowType)) {
      return OBDal.getInstance().get(Template.class, TEMPLATE_MAP.get(windowType));
    }
    return OBDal.getInstance().get(Template.class, DEFAULT_TEMPLATE_ID);
  }

  public Tab getTab() {
    return tab;
  }

  public void setTab(Tab tab) {
    this.tab = tab;
    entity = ModelProvider.getInstance().getEntityByTableId((String) DalUtil.getId(tab.getTable()));
  }

  public String getWhereClause() {
    if (tab.getHqlwhereclause() != null) {
      return tab.getHqlwhereclause();
    }
    return "";
  }

  public String getWhereClauseSQL() {
    if (tab.getSQLWhereClause() != null) {
      return tab.getSQLWhereClause();
    }
    return "";
  }

  public String getOrderByClause() {
    if (tab.getHqlorderbyclause() != null) {
      return tab.getHqlorderbyclause();
    }
    return "";
  }

  public String getOrderByClauseSQL() {
    if (tab.getSQLOrderByClause() != null) {
      return tab.getSQLOrderByClause();
    }
    return "";
  }

  public String getSortField() {
    if (getOrderByClause().length() > 0) {
      return "";
    }

    long lowestSortno = Long.MAX_VALUE;
    OBViewField sortByField = null;
    for (OBViewFieldDefinition localField : getViewTab().getFieldHandler().getFields()) {
      if (!(localField instanceof OBViewField)) {
        continue;
      }
      final OBViewField viewField = (OBViewField) localField;
      final Long recordSortno = viewField.getField().getRecordSortNo();
      if (viewField.getLength() < 2000 && viewField.isShowInitiallyInGrid() && recordSortno != null
          && recordSortno < lowestSortno) {
        sortByField = viewField;
      }
    }
    if (sortByField != null && sortByField.getProperty() != null) {
      return sortByField.getProperty().getName();
    }

    // use 2 examples of sequence number of line no
    if (entity.hasProperty(Tab.PROPERTY_SEQUENCENUMBER)) {
      return Tab.PROPERTY_SEQUENCENUMBER;
    }
    if (entity.hasProperty(OrderLine.PROPERTY_LINENO)) {
      return OrderLine.PROPERTY_LINENO;
    }

    for (OBViewFieldDefinition localField : getViewTab().getFieldHandler().getFields()) {
      if (!(localField instanceof OBViewField)) {
        continue;
      }
      final OBViewField viewField = (OBViewField) localField;
      if (viewField.getProperty() != null && viewField.getProperty().isIdentifier()) {
        return viewField.getProperty().getName();
      }
    }
    return "";
  }

  public String getFilterClause() {
    if (tab.getHqlfilterclause() != null) {
      return addTransactionalFilter(tab.getHqlfilterclause());
    }
    return addTransactionalFilter("");
  }

  public String getFilterClauseSQL() {
    if (tab.getFilterClause() != null) {
      return tab.getFilterClause();
    }
    return "";
  }

  public String getFilterName() {
    String filterName = "";

    if (tab.getHqlfilterclause() != null) {
      filterName = Utility.messageBD(new DalConnectionProvider(false), "OBUIAPP_ImplicitFilter",
          OBContext.getOBContext().getLanguage().getLanguage());
      if (tab.getFilterName() != null) {
        filterName += "<i>("
            + OBViewUtil.getLabel(tab, tab.getADTabTrlList(), Tab.PROPERTY_FILTERNAME) + ")</i>";
      }
    }

    if (isApplyTransactionalFilter()) {
      if (!filterName.isEmpty()) {
        filterName += " "
            + Utility.messageBD(new DalConnectionProvider(false), "And", OBContext.getOBContext()
                .getLanguage().getLanguage()) + " ";
      }
      filterName += Utility.messageBD(new DalConnectionProvider(false),
          "OBUIAPP_TransactionalFilter", OBContext.getOBContext().getLanguage().getLanguage())
          .replace(
              "%n",
              Utility.getTransactionalDate(new DalConnectionProvider(false), RequestContext.get()
                  .getVariablesSecureApp(), tab.getWindow().getId()));
    }

    if (!filterName.isEmpty()) {
      filterName = Utility.messageBD(new DalConnectionProvider(false), "OBUIAPP_FilteredGrid",
          OBContext.getOBContext().getLanguage().getLanguage()) + " " + filterName + ".";
    }

    return filterName;
  }

  private String addTransactionalFilter(String filterClause) {
    if (!this.isApplyTransactionalFilter()) {
      return filterClause;
    }
    String transactionalFilter = " e.updated > " + JsonConstants.QUERY_PARAM_TRANSACTIONAL_RANGE
        + " ";
    if (entity.hasProperty(Order.PROPERTY_PROCESSED)) {
      transactionalFilter += " or e.processed = 'N' ";
    }
    transactionalFilter = " (" + transactionalFilter + ") ";

    if (filterClause.length() > 0) {
      return " (" + transactionalFilter + " and (" + filterClause + ")) ";
    }
    return transactionalFilter;
  }

  public String getUiPattern() {
    return tab.getUIPattern();
  }

  public boolean isApplyTransactionalFilter() {
    return applyTransactionalFilter;
  }

  public void setApplyTransactionalFilter(boolean applyTransactionalFilter) {
    this.applyTransactionalFilter = applyTransactionalFilter;
  }

  public OBViewTab getViewTab() {
    return viewTab;
  }

  public void setViewTab(OBViewTab viewTab) {
    this.viewTab = viewTab;
  }

  /**
   * Returns the string representation of an array that contains all the properties that must always
   * be returned from the datasource when the grid asks for data: 
   * - id 
   * - client and organization
   * - all the properties that compose the identifier of the entity 
   * - all button fields with label values
   * - the link to parent properties
   * - all the properties that are part of the display logic of the tab buttons
   */
  public List<String> getRequiredGridProperties() {
    List<String> requiredGridProperties = new ArrayList<String>();
    requiredGridProperties.add("id");
    // Needed to check if the record is readonly (check addWritableAttribute method of DefaultJsonDataService)
    requiredGridProperties.add("client");
    requiredGridProperties.add("organization");
    // Audit fields are mandatory because the FIC does not returned them when called in EDIT mode
    requiredGridProperties.add("updatedBy");
    requiredGridProperties.add("updated");
    requiredGridProperties.add("creationDate");
    requiredGridProperties.add("createdBy");

    // Always include all the properties that are part of the identifier of the entity
    for (Property identifierProperty : this.entity.getIdentifierProperties()) {
      requiredGridProperties.add(identifierProperty.getName());
    }

    // Properties related to buttons that have label values
    List<ButtonField> buttonFields = getViewTab().getAllButtonFields();
    for (ButtonField buttonField : buttonFields) {
      requiredGridProperties.add(buttonField.getPropertyName());
    }

    // List of properties that are part of the display logic of the subtabs
    List<String> tabDisplayLogicFields = getViewTab().getDisplayLogicFields();
    for (String tabDisplayLogicField : tabDisplayLogicFields) {
      requiredGridProperties.add(tabDisplayLogicField);
    }

    // List of properties that are part of the display logic of buttons
    List<String> propertiesInButtonFieldDisplayLogic = getViewTab().getFieldHandler()
        .getPropertiesInButtonFieldDisplayLogic();
    for (String propertyName : propertiesInButtonFieldDisplayLogic) {
      requiredGridProperties.add(propertyName);
    }

    // Always include the propertyt that links to the parent tab
    String linkToParentPropertyName = this.getLinkToParentPropertyName();
    if (linkToParentPropertyName != null && !linkToParentPropertyName.isEmpty()) {
      requiredGridProperties.add(linkToParentPropertyName);
    }

    // Include the Stored in Session properties
    List<String> storedInSessionProperties = getViewTab().getFieldHandler()
        .getStoredInSessionProperties();
    for (String storedInSessionProperty : storedInSessionProperties) {
      requiredGridProperties.add(storedInSessionProperty);
    }

    return requiredGridProperties;
  }

  private String getLinkToParentPropertyName() {
    Tab parentTab = KernelUtils.getInstance().getParentTab(tab);
    if (parentTab == null) {
      return null;
    }
    List<Property> linkToparentPropertyList = entity.getParentProperties();
    if (linkToparentPropertyList.isEmpty()) {
      return null;
    }
    String parentTableId = parentTab.getTable().getId();
    for (Property linkToParentProperty : linkToparentPropertyList) {
      Property referencedProperty = linkToParentProperty.getReferencedProperty();
      String referencedTableId = referencedProperty.getEntity().getTableId();
      if (parentTableId.equals(referencedTableId)) {
        return linkToParentProperty.getName();
      }
    }
    return null;
  }
}
