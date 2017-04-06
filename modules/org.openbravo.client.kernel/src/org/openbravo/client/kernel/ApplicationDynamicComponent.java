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
 * All portions are Copyright (C) 2010-2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.Set;

import org.openbravo.base.model.Entity;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * The component responsible for generating some dynamic elements of the application js file which
 * are related to the user of the current context.
 * 
 * @author mtaal
 */
public class ApplicationDynamicComponent extends SessionDynamicTemplateComponent {

  @Override
  public String getId() {
    return KernelConstants.APPLICATION_DYNAMIC_COMPONENT_ID;
  }

  @Override
  protected String getTemplateId() {
    return KernelConstants.APPLICATION_DYNAMIC_TEMPLATE_ID;
  }

  public Set<Entity> getAccessibleEntities() {
    final Set<Entity> entities = OBContext.getOBContext().getEntityAccessChecker()
        .getReadableEntities();
    entities.addAll(OBContext.getOBContext().getEntityAccessChecker().getWritableEntities());
    return entities;
  }

  public User getUser() {
    return OBContext.getOBContext().getUser();
  }

  public Client getClient() {
    return OBContext.getOBContext().getCurrentClient();
  }

  public Organization getOrganization() {
    return OBContext.getOBContext().getCurrentOrganization();
  }

  public Role getRole() {
    return OBContext.getOBContext().getRole();
  }
}
