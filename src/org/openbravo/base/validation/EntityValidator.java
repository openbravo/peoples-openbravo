/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SL All
 * portions are Copyright (C) 2008 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package org.openbravo.base.validation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;

/**
 * Validates an entity, keeps list of property validators which are called one
 * by one for a passed entity.
 * 
 * @author mtaal
 */

public class EntityValidator {
  
  private List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
  private boolean validateRequired = false;
  private Entity entity;
  
  public void initialize(Entity e) {
    entity = e;
    for (Property p : e.getProperties()) {
      if (StringPropertyValidator.isValidationRequired(p)) {
        final StringPropertyValidator spv = new StringPropertyValidator();
        spv.setProperty(p);
        spv.initialize();
        validators.add(spv);
      } else if (NumericPropertyValidator.isValidationRequired(p)) {
        final NumericPropertyValidator nv = new NumericPropertyValidator();
        nv.setProperty(p);
        nv.initialize();
        validators.add(nv);
      }
    }
    validateRequired = !validators.isEmpty();
  }
  
  public void validate(Object o) {
    if (!validateRequired) {
      return;
    }
    final ValidationException ve = new ValidationException();
    for (PropertyValidator pv : validators) {
      final String msg = pv.validate(o);
      if (msg != null) {
        ve.addMessage(pv.getProperty(), msg);
      }
    }
    if (ve.hasMessages()) {
      throw ve;
    }
  }
}
