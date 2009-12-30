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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model.domaintype;

import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Reference;
import org.openbravo.base.validation.ValidationException;

/**
 * The base class for all property types.
 * 
 * @author mtaal
 */

public abstract class BaseDomainType implements DomainType {

  private Reference reference;
  private ModelProvider modelProvider;

  /**
   * Method is empty in this class, subclasses should override and call super.initialize() (to allow
   * future additional initialization in this class).
   * 
   * Note: any subclass should clean-up and close database connections or hibernate sessions. If
   * this is not done then the update.database task may hang when disabling foreign keys.
   * 
   * @see org.openbravo.base.model.domaintype.DomainType#initialize(org.openbravo.base.model.ModelProvider)
   */
  public void initialize() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.DomainType#setReference(Reference)
   */
  public void setReference(Reference reference) {
    this.reference = reference;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.DomainType#getReference()
   */
  public Reference getReference() {
    return reference;
  }

  public ModelProvider getModelProvider() {
    return modelProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.openbravo.base.model.domaintype.DomainType#setModelProvider(org.openbravo.base.model.
   * ModelProvider )
   */
  public void setModelProvider(ModelProvider modelProvider) {
    this.modelProvider = modelProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.base.model.domaintype.DomainType#checkObjectIsValid(org.openbravo.base.structure
   * .BaseOBObjectDef, org.openbravo.base.model.Property)
   */
  public void checkObjectIsValid(BaseOBObjectDef obObject, Property property)
      throws ValidationException {
    checkIsValidValue(property, obObject.get(property.getName()));
  }

}
