package org.openbravo.base;

import org.openbravo.base.structure.BaseOBObject;

/**
 * Exception to select the property in the bob’s entity to look for the timezone
 * 
 * @author Eugen Hamuraru
 *
 */
public interface OrganizationPropertyHook extends Prioritizable {
  /**
   * @return the property in the bob’s entity to look for the timezone
   * 
   * @param bob
   * 
   */
  public String getOrganizationProperty(BaseOBObject bob);
}
