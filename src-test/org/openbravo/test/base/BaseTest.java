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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.base;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalLayerInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * Testcase.
 * 
 * @author mtaal
 */

public class BaseTest extends TestCase {

  private boolean errorOccured = false;

  @Override
  protected void setUp() throws Exception {
    initializeDalLayer();
    // clear the session otherwise it keeps the old model
    setBigBazaarUserContext();
    super.setUp();
    setErrorOccured(true);
  }

  protected void initializeDalLayer() throws Exception {
    if (!DalLayerInitializer.getInstance().isInitialized()) {
      setConfigPropertyFiles();
      DalLayerInitializer.getInstance().initialize(true);
    }
  }

  protected void setConfigPropertyFiles() {

    // get the location of the current class file
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".class");
    File f = new File(url.getPath());
    // go up 7 levels
    for (int i = 0; i < 7; i++) {
      f = f.getParentFile();
    }
    final File configDirectory = new File(f, "config");
    f = new File(configDirectory, "Openbravo.properties");
    if (!f.exists()) {
      throw new OBException("The testrun assumes that it is run from "
          + "within eclipse and that the Openbravo.properties "
          + "file is located as a grandchild of the 7th ancestor " + "of this class");
    }
    OBPropertiesProvider.getInstance().setProperties(f.getAbsolutePath());
    OBConfigFileProvider.getInstance().setFileLocation(configDirectory.getAbsolutePath());
  }

  protected void setSystemAdministratorContext() {
    setUserContext("0");
  }

  protected void setBigBazaarUserContext() {
    setUserContext("1000000");
  }

  protected void setUserContext(String userId) {
    OBContext.setOBContext(userId);
  }

  protected void setBigBazaarAdminContext() {
    setUserContext("100");
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (SessionHandler.isSessionHandlerPresent()) {
        if (SessionHandler.getInstance().getDoRollback()) {
          SessionHandler.getInstance().rollback();
        } else if (isErrorOccured()) {
          SessionHandler.getInstance().rollback();
        } else {
          SessionHandler.getInstance().commitAndClose();
        }
      }
    } catch (final Exception e) {
      SessionHandler.getInstance().rollback();
      reportException(e);
      throw e;
    } finally {
      SessionHandler.deleteSessionHandler();
      OBContext.setOBContext((OBContext) null);
    }
    super.tearDown();
  }

  protected void reportException(Exception e) {
    if (e == null)
      return;
    e.printStackTrace(System.err);
    if (e instanceof SQLException) {
      reportException(((SQLException) e).getNextException());
    }
  }

  public boolean isErrorOccured() {
    return errorOccured;
  }

  public void commitTransaction() {
    setErrorOccured(false);
  }

  public void setErrorOccured(boolean errorOccured) {
    this.errorOccured = errorOccured;
  }

  protected <T extends BaseOBObject> T getOneInstance(Class<T> clz) {
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    if (obc.list().size() == 0) {
      throw new OBException("There are zero instances for class " + clz.getName());
    }
    return obc.list().get(0);
  }
}