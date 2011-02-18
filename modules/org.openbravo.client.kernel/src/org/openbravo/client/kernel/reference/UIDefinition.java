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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Field;

/**
 * Base implementation of a user interface client reference.
 * 
 * @author mtaal
 */
public abstract class UIDefinition {
  private static final String TYPE_NAME_PREFIX = "_id_";

  private Reference reference;
  private DomainType domainType;

  /**
   * Unique name used to identify the type.
   * 
   * @return
   */
  public String getName() {
    return TYPE_NAME_PREFIX + reference.getId();
  }

  /**
   * @return the Smartcient type from which this type inherits.
   */
  public String getParentType() {
    return "text";
  }

  /**
   * @return the form item type used for editing this reference in a form.
   */
  public String getFormEditorType() {
    return "OBText";
  }

  /**
   * @return the form item type used for editing this reference in a grid. As a default will return
   *         {@link #getFormEditorType()}
   */
  public String getGridEditorType() {
    return getFormEditorType();
  }

  /**
   * @return the form item type used for filtering in grids. As a default will return
   *         {@link #getFormEditorType()}
   */
  public String getFilterEditorType() {
    return getFormEditorType();
  }

  /**
   * Computes the properties used to define the type, this includes all the Smartclient SimpleType
   * properties.
   * 
   * @return a javascript string which can be included in the javascript defining the SimpleType.
   *         The default implementation returns an empty string.
   */
  public String getTypeProperties() {
    return "";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient form. This can be the
   * default value or the sets of values in the valuemap.
   * 
   * NOTE: the field parameter may be null, implementors of subclasses should take this into
   * account.
   * 
   * @param field
   *          the field for which the information should be computed. NOTE: the caller is allowed to
   *          pass null for cases where the field properties are needed for a FormItem which is not
   *          backed by an Openbravo field.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getFieldProperties(Field field) {
    return "";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient grid filter. This can be
   * the default value or the sets of values in the valuemap.
   * 
   * Note: the result should be either empty, if not empty then it start with a comma and end
   * without a comma, this to generate correct javascript.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getFilterEditorProperties(Field field) {
    if (getFilterEditorType() == null) {
      return ",canFilter: false, required: false";
    }
    return ", canFilter:true, required: false, filterEditorType: '" + getFilterEditorType() + "'"
        + getFilterEditorPropertiesProperty(field);
  }

  /**
   * Returns the filterEditorProperties property set on the gridfield.
   * 
   * @return
   */
  protected String getFilterEditorPropertiesProperty(Field field) {
    return ", filterEditorProperties: {selectOnFocus: true}";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient grid cell. This can be the
   * default value or the sets of values in the valuemap.
   * 
   * Note: the result should be either empty, if not empty then it start with a comma and end
   * without a comma, this to generate correct javascript.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getGridFieldProperties(Field field) {
    return ", name: '" + getGridFieldName(field) + "', canExport: true, canHide: true";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient grid cell when it is being
   * edited. This can be the default value or the sets of values in the valuemap.
   * 
   * Note: the result should be either empty, if not empty then it start with a comma and end
   * without a comma, this to generate correct javascript.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getGridEditorFieldProperties(Field field) {
    return "";
  }

  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
  }

  public DomainType getDomainType() {
    if (domainType == null) {
      if (reference == null) {
        throw new OBException("Domain type can not be computed, reference is not set");
      }
      domainType = ModelProvider.getInstance().getReference(reference.getId()).getDomainType();
    }
    return domainType;
  }

  // note can make sense to also enable hover of values for enums
  // but then the value should be converted to the translated
  // value of the enum
  protected String getShowHoverGridFieldSettings(Field field) {
    return ", showHover: true, width: 150, hoverHTML:\"return record['" + getGridFieldName(field)
        + "']\"";
  }

  protected String getGridFieldName(Field fld) {
    final Property prop = KernelUtils.getInstance().getPropertyFromColumn(fld.getColumn());
    return prop.getName();
  }
}
