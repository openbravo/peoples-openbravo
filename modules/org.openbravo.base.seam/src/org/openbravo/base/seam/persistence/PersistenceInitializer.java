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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.persistence;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.dal.core.DalSessionFactoryController;
import org.openbravo.dal.core.SessionHandler;

/**
 * Initializes the components relevant for persistence through Seam. Makes sure that Openbravo uses
 * the Seam components instead of own implementations.
 * 
 * @see OBProvider
 * @author mtaal
 */
@Name("persistenceInitializer")
@Startup(depends = { "entityManagerFactoryController" })
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.APPLICATION)
public class PersistenceInitializer {

  public PersistenceInitializer() {
    OBProvider.getInstance().registerInstance(DalSessionFactoryController.class,
        EntityManagerFactoryController.getInstance(), true);
    SessionFactoryController.setInstance(EntityManagerFactoryController.getInstance());
    OBProvider.getInstance().register(SessionHandler.class, SeamSessionHandler.class, true);
  }
}
