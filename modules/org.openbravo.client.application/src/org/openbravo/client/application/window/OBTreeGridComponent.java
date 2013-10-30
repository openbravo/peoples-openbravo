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

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.base.util.Check;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.ReferencedTree;
import org.openbravo.model.ad.domain.ReferencedTreeField;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.ADTreeType;
import org.openbravo.model.ad.utility.TableTree;
import org.openbravo.service.json.JsonConstants;

/**
 * The backing bean for generating the OBTreeGridPopup client-side representation.
 * 
 * @author AugustoMauch
 */
public class OBTreeGridComponent extends BaseTemplateComponent {

  private static final String DEFAULT_TEMPLATE_ID = "74451C30650946FC855FCFDB4577070C";
  protected static final Map<String, String> TEMPLATE_MAP = new HashMap<String, String>();

  private static final String TREENODE_DATASOURCE = "90034CAE96E847D78FBEF6D38CB1930D";
  private static final String LINKTOPARENT_DATASOURCE = "610BEAE5E223447DBE6FF672B703F72F";

  private static final String TREENODE_STRUCTURE = "ADTree";
  private static final String LINKTOPARENT_STRUCTURE = "LinkToParent";

  private Tab tab;
  private OBViewTab viewTab;
  private String referencedTableId;

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
  }

  public OBViewTab getViewTab() {
    return viewTab;
  }

  public void setViewTab(OBViewTab viewTab) {
    this.viewTab = viewTab;
  }

  public String getReferencedTableId() {
    return tab.getTable().getId();
  }

  public void setReferencedTableId(String referencedTableId) {
    this.referencedTableId = referencedTableId;
  }

  public boolean isOrderedTree() {
    TableTree tableTree = tab.getTableTree();
    if (tableTree != null) {
      return tableTree.getTreeCategory().isOrdered();
    } else {
      return false;
    }
  }

  public String getDataSourceId() {
    String dataSourceId = null;
    TableTree tableTree = tab.getTableTree();
    if (tableTree != null) {
      ADTreeType treeCategory = tableTree.getTreeCategory();
      if (TREENODE_STRUCTURE.equals(treeCategory.getTreeStructure())) {
        dataSourceId = TREENODE_DATASOURCE;
      } else if (LINKTOPARENT_STRUCTURE.equals(treeCategory.getTreeStructure())) {
        dataSourceId = LINKTOPARENT_DATASOURCE;
      }
      return dataSourceId;
    } else {
      return null;
    }
  }

  public String getTreeStructure() {
    TableTree tableTree = tab.getTableTree();
    if (tableTree != null) {
      ADTreeType treeCategory = tableTree.getTreeCategory();
      return treeCategory.getTreeStructure();
    } else {
      return null;
    }
  }

  public static String getAdditionalProperties(ReferencedTree referencedTree,
      boolean onlyDisplayField) {
    if (onlyDisplayField
        && (referencedTree.getDisplayfield() == null || !referencedTree.getDisplayfield()
            .isActive())) {
      return "";
    }
    final StringBuilder extraProperties = new StringBuilder();
    for (ReferencedTreeField treeField : referencedTree.getADReferencedTreeFieldList()) {
      if (onlyDisplayField && treeField != referencedTree.getDisplayfield()) {
        continue;
      }
      if (!treeField.isActive()) {
        continue;
      }
      String fieldName = getPropertyOrDataSourceField(treeField);
      final DomainType domainType = getDomainType(treeField);
      if (domainType instanceof ForeignKeyDomainType) {
        fieldName = fieldName + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER;
      }
      if (extraProperties.length() > 0) {
        extraProperties.append(",");
      }
      extraProperties.append(fieldName);
    }
    return extraProperties.toString();
  }

  private static String getPropertyOrDataSourceField(ReferencedTreeField treeField) {
    String result = null;
    if (treeField.getProperty() != null) {
      result = treeField.getProperty();
    }
    // TODO
    // else if (treeField.getDisplayColumnAlias() != null) {
    // result = treeField.getDisplayColumnAlias();
    // } else if (treeField.getObserdsDatasourceField() != null) {
    // result = treeField.getObserdsDatasourceField().getName();
    // } else {
    // throw new IllegalStateException("Selectorfield " + treeField
    // + " has a null datasource and a null property");
    // }
    return result.replace(DalUtil.DOT, DalUtil.FIELDSEPARATOR);
  }

  private static DomainType getDomainType(ReferencedTreeField treeField) {
    if (treeField.getRefTree().getTable() != null && treeField.getProperty() != null) {
      final String entityName = treeField.getRefTree().getTable().getName();
      final Entity entity = ModelProvider.getInstance().getEntity(entityName);
      final Property property = DalUtil.getPropertyFromPath(entity, treeField.getProperty());
      Check.isNotNull(property, "Property " + treeField.getProperty() + " not found in Entity "
          + entity);
      return property.getDomainType();
    }
    // TODO
    // else if (treeField.getRefTree().getTable() != null && treeField.getRefTree().isCustomQuery()
    // && treeField.getReference() != null) {
    // return getDomainType(treeField.getReference().getId());
    // } else if (treeField.getObserdsDatasourceField().getReference() != null) {
    // return getDomainType(treeField.getObserdsDatasourceField().getReference().getId());
    // }
    return null;
  }

}
