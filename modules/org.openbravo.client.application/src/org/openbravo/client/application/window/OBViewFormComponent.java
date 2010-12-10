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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.util.Log;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.ButtonDomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.FieldGroup;
import org.openbravo.model.ad.ui.Tab;

/**
 * The backing bean for generating the OBViewForm client-side representation.
 * 
 * @author mtaal
 */
public class OBViewFormComponent extends BaseTemplateComponent {
  private static Long ZERO = new Long(0);

  private static final String TEMPLATE_ID = "C1D176407A354A40815DC46D24D70EB8";

  private Tab tab;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public Tab getTab() {
    return tab;
  }

  public void setTab(Tab tab) {
    this.tab = tab;
  }

  public List<OBViewFieldDefinition> getFields() {

    final List<OBViewFieldDefinition> fields = new ArrayList<OBViewFieldDefinition>();
    final List<Field> adFields = new ArrayList<Field>(tab.getADFieldList());
    Collections.sort(adFields, new FormFieldComparator());

    OBViewFieldGroup currentFieldGroup = null;
    FieldGroup currentADFieldGroup = null;
    for (Field field : adFields) {
      if (field.getColumn() == null) {
        // TODO: throw an error?
        continue;
      }
      if (!field.isDisplayed()) {
        continue;
      }
      if (!field.isActive()) {
        continue;
      }
      final Property property = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn());

      // a button domain type, continue for now
      if (property.getDomainType() instanceof ButtonDomainType) {
        continue;
      }

      final OBViewField viewField = new OBViewField();
      viewField.setField(field);
      viewField.setProperty(property);
      // change in fieldgroup
      if (field.getFieldGroup() != null && field.getFieldGroup() != currentADFieldGroup) {
        // start of a fieldgroup use it
        final OBViewFieldGroup viewFieldGroup = new OBViewFieldGroup();
        fields.add(viewFieldGroup);
        viewFieldGroup.setFieldGroup(field.getFieldGroup());
        currentFieldGroup = viewFieldGroup;
        currentADFieldGroup = field.getFieldGroup();
      } else {
        fields.add(viewField);
      }
      if (currentFieldGroup != null) {
        currentFieldGroup.addChild(viewField);
      }
    }
    return fields;
  }

  public interface OBViewFieldDefinition {
    public String getLabel();

    public String getName();

    public String getType();

    public boolean getStandardField();

    public String getFieldProperties();
  }

  public class OBViewField implements OBViewFieldDefinition {
    private Field field;
    private Property property;
    private String label;
    private UIDefinition uiDefinition;

    public String getType() {
      return getUIDefinition().getName();
    }

    public String getFieldProperties() {
      String jsonString = getUIDefinition().getFieldProperties(field).trim();
      if (jsonString == null || jsonString.trim().length() == 0) {
        return "";
      }
      // strip the first and last { }
      if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
        // note -2 is done because the first substring takes of 1 already
        return jsonString.substring(1).substring(0, jsonString.length() - 2) + ",";
      } else if (jsonString.equals("{}")) {
        return "";
      }
      Log.warn("Can't process json string " + jsonString);
      return "";
    }

    private UIDefinition getUIDefinition() {
      if (uiDefinition != null) {
        return uiDefinition;
      }
      uiDefinition = UIDefinitionController.getInstance().getUIDefinition(property.getColumnId());
      return uiDefinition;
    }

    public String getName() {
      return property.getName();
    }

    public String getColumnName() {
      return property.getColumnName().toLowerCase();
    }

    public String getLabel() {
      // compute the label
      if (label == null) {
        label = OBViewUtil.getLabel(field);
      }
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public Field getField() {
      return field;
    }

    public void setField(Field field) {
      this.field = field;
    }

    public boolean getStandardField() {
      return true;
    }

    public Property getProperty() {
      return property;
    }

    public void setProperty(Property property) {
      this.property = property;
    }

    public boolean isRequired() {
      return property.isMandatory();
    }

    public int getLength() {
      return property.getFieldLength();
    }

    public boolean getForeignKeyField() {
      return property.getDomainType() instanceof ForeignKeyDomainType;
    }

    public String getDataSourceId() {
      return property.getTargetEntity().getName();
    }
  }

  public class OBViewFieldGroup implements OBViewFieldDefinition {
    private FieldGroup fieldGroup;
    private String label;
    private List<OBViewFieldDefinition> children = new ArrayList<OBViewFieldDefinition>();

    public String getFieldProperties() {
      return "";
    }

    public String getLabel() {
      // compute the label
      if (label == null) {
        label = OBViewUtil.getLabel(fieldGroup, fieldGroup.getADFieldGroupTrlList());
      }
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public FieldGroup getFieldGroup() {
      return fieldGroup;
    }

    public void setFieldGroup(FieldGroup fieldGroup) {
      this.fieldGroup = fieldGroup;
    }

    public void addChild(OBViewFieldDefinition viewFieldDefinition) {
      children.add(viewFieldDefinition);
    }

    public List<OBViewFieldDefinition> getChildren() {
      return children;
    }

    public boolean getStandardField() {
      return false;
    }

    public String getType() {
      return "section";
    }

    public String getName() {
      return fieldGroup.getId();
    }
  }

  public static class FormFieldComparator implements Comparator<Field> {

    @Override
    public int compare(Field arg0, Field arg1) {
      Long arg0Position = (arg0.getSequenceNumber() != null ? arg0.getSequenceNumber() : 0);
      Long arg1Position = (arg1.getSequenceNumber() != null ? arg1.getSequenceNumber() : 0);
      if (arg0Position == null) {
        arg0Position = ZERO;
      }
      if (arg1Position == null) {
        arg1Position = ZERO;
      }
      return (int) (arg0Position - arg1Position);
    }

  }
}
