package org.openbravo.base.structure;

import org.openbravo.model.ad.access.Role;

public interface InheritedAccessEnabled {
  public Role getRole();

  public void setRole(Role role);

  public Role getInheritedFrom();

  public void setInheritedFrom(Role inheritedFrom);
}
