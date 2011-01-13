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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.application.ApplicationUtils;
import org.openbravo.client.application.window.OBViewFormComponent.FormFieldComparator;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.TabTrl;
import org.openbravo.utils.FormatUtilities;

/**
 * Represents the Openbravo Tab (form and grid combination).
 * 
 * @author mtaal
 */
public class OBViewTab extends BaseTemplateComponent {

  private static final String TEMPLATE_ID = "B5124C0A450D4D3A867AEAC7DF64D6F0";

  private Tab tab;
  private String tabTitle;
  private List<OBViewTab> childTabs = new ArrayList<OBViewTab>();
  private OBViewTab parentTabComponent;
  private List<ButtonField> buttonFields = null;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public void addChildTabComponent(OBViewTab childTabComponent) {
    childTabComponent.setParentTabComponent(this);
    childTabs.add(childTabComponent);
  }

  public String getDefaultEditMode() {
    return tab.isDefaultEditMode() == null ? "false" : Boolean.toString(tab.isDefaultEditMode());
  }

  public List<ButtonField> getButtonFields() {
    if (buttonFields != null) {
      return buttonFields;
    }
    buttonFields = new ArrayList<ButtonField>();
    final List<Field> adFields = new ArrayList<Field>(tab.getADFieldList());
    Collections.sort(adFields, new FormFieldComparator());
    for (Field fld : adFields) {
      if (fld.isActive() && fld.isDisplayed()) {
        if (!(ApplicationUtils.isUIButton(fld))) {
          continue;
        }
        buttonFields.add(new ButtonField(fld));
      }
    }
    return buttonFields;
  }

  public String getParentProperty() {
    if (parentTabComponent == null) {
      return "";
    }
    final Entity thisEntity = ModelProvider.getInstance().getEntity(tab.getTable().getName());
    final Entity parentEntity = ModelProvider.getInstance().getEntity(
        parentTabComponent.getTab().getTable().getName());
    for (Property property : thisEntity.getProperties()) {
      if (property.isPrimitive() || property.isOneToMany()) {
        continue;
      }
      if (property.getTargetEntity() == parentEntity) {
        return property.getName();
      }
    }
    return "";
  }

  public String getViewForm() {
    final OBViewFormComponent viewFormComponent = createComponent(OBViewFormComponent.class);
    viewFormComponent.setParameters(getParameters());
    viewFormComponent.setTab(tab);
    viewFormComponent.setParentProperty(getParentProperty());
    return viewFormComponent.generate();
  }

  public String getViewGrid() {
    final OBViewGridComponent viewGridComponent = createComponent(OBViewGridComponent.class);
    viewGridComponent.setParameters(getParameters());
    viewGridComponent.setTab(tab);
    return viewGridComponent.generate();
  }

  public OBViewTab getParentTabComponent() {
    return parentTabComponent;
  }

  public void setParentTabComponent(OBViewTab parentTabComponent) {
    this.parentTabComponent = parentTabComponent;
  }

  public List<OBViewTab> getChildTabs() {
    return childTabs;
  }

  public Tab getTab() {
    return tab;
  }

  public void setTab(Tab tab) {
    this.tab = tab;
  }

  public boolean isTabSet() {
    return tab != null;
  }

  public String getTabId() {
    return tab.getId();
  }

  public String getModuleId() {
    return tab.getModule().getId();
  }

  public String getEntityName() {
    return tab.getTable().getName();
  }

  public String getTabTitle() {
    if (tabTitle == null) {
      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      for (TabTrl tabTrl : tab.getADTabTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(tabTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = tabTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = tab.getName();
      }
    }
    return tabTitle;
  }

  public String getDataSourceId() {
    return tab.getTable().getName();
  }

  public void setTabTitle(String tabTitle) {
    this.tabTitle = tabTitle;
  }

  public List<FieldProperty> getAllFields() {
    List<FieldProperty> fields = new ArrayList<FieldProperty>();
    Field keyField = null;
    for (Field field : tab.getADFieldList()) {
      if (field.getColumn().isKeyColumn()) {
        keyField = field;
      }
      FieldProperty fp = new FieldProperty(field);

      fields.add(fp);
    }

    // Additional key column, set in session with db column name
    if (keyField != null) {
      FieldProperty fp = new FieldProperty(keyField);
      fp.columnName = keyField.getColumn().getDBColumnName();
      fp.session = true;
      fields.add(fp);
    }

    return fields;
  }

  public class FieldProperty {
    private String columnName;
    private String dbColumnName;
    private String propertyName;
    private boolean session;

    public FieldProperty(Field field) {
      Column col = field.getColumn();
      columnName = "inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName());
      dbColumnName = col.getDBColumnName();
      propertyName = KernelUtils.getInstance().getPropertyFromColumn(col).getName();
      session = col.isStoredInSession();
    }

    public String getColumnName() {
      return columnName;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public String getSession() {
      return session ? "true" : "false";
    }

    public String getDbColumnName() {
      return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
      this.dbColumnName = dbColumnName;
    }
  }

  public class ButtonField {
    private String id;
    private String label;
    private String url;
    private String propertyName;
    private List<Value> labelValues;
    private boolean autosave;

    public ButtonField(Field fld) {
      id = fld.getId();
      label = OBViewUtil.getLabel(fld);
      Column column = fld.getColumn();

      propertyName = KernelUtils.getInstance().getPropertyFromColumn(column).getName();
      autosave = column.isAutosave();

      // Define command
      Process process = column.getProcess();
      if (process != null) {
        String manualProcessMapping = null;
        for (ModelImplementation impl : process.getADModelImplementationList()) {
          if (impl.isDefault()) {
            for (ModelImplementationMapping mapping : impl.getADModelImplementationMappingList()) {
              if (mapping.isDefault()) {
                manualProcessMapping = mapping.getMappingName();
                break;
              }
            }
            break;
          }
        }

        if (manualProcessMapping == null) {
          // Standard UI process
          url = Utility.getTabURL(fld.getTab().getId(), "E", false);
          command = "BUTTON" + FormatUtilities.replace(column.getDBColumnName())
              + column.getProcess().getId();
        } else {
          url = manualProcessMapping;
          command = "DEFAULT";
        }
      } else {
        if ("Posted".equals(column.getDBColumnName()) && column.getProcess() == null) {
          command = "BUTTONPosted";
          url = Utility.getTabURL(fld.getTab().getId(), "E", false);
        }
      }

      labelValues = new ArrayList<Value>();
      if (column.getReferenceSearchKey() != null) {
        for (org.openbravo.model.ad.domain.List valueList : column.getReferenceSearchKey()
            .getADListList()) {
          labelValues.add(new Value(valueList));
        }
      }
    }

    public boolean isAutosave() {
      return autosave;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public List<Value> getLabelValues() {
      return labelValues;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getCommand() {
      return command;
    }

    public void setCommand(String command) {
      this.command = command;
    }

    private String command;

    public String getLabel() {
      // this type of escaping not needed for javascript
      // gives jslint warnings
      if (label.contains("\\>")) {
        return label.replace("\\>", ">");
      }
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public class Value {
      private String value;
      private String labelValue;

      public Value(org.openbravo.model.ad.domain.List valueList) {
        labelValue = OBViewUtil.getLabel(valueList, valueList.getADListTrlList());
        value = valueList.getSearchKey();
      }

      public String getValue() {
        return value;
      }

      public String getLabel() {
        return labelValue;
      }
    }
  }
}
