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
 * All portions are Copyright (C) 2008-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes and provides the session factory for the runtime dal layer. This
 * SessionFactoryController is initialized after the model has been read in-memory. The
 * {@link DalMappingGenerator DalMappingGenerator} is used to generated the Hibernate mapping for
 * the runtime model (see {@link ModelProvider ModelProvider}.
 * 
 * @author mtaal
 */

public class DalSessionFactoryController extends SessionFactoryController {
  private static final Logger log = Logger.getLogger(DalSessionFactoryController.class);

  @Override
  protected void mapModel(Configuration configuration) {
    DalMappingGenerator mappingGenerator = DalMappingGenerator.getInstance();
    final String mapping = mappingGenerator.generateMapping();
    log.debug("Generated mapping: ");
    log.debug(mapping);

    if (mappingGenerator.getHibernateFileLocation() != null) {
      configuration.addFile(mappingGenerator.getHibernateFileLocation());
      return;
    }

    Path tmpFile = null;
    try {
      tmpFile = Files.createTempFile("", ".hbm");
      Files.write(tmpFile, mapping.getBytes());
      configuration.addFile(tmpFile.toString());
    } catch (IOException e) {
      log.error("Error writing temporary .hbm file for configuration", e);
    } finally {
      try {
        if (tmpFile != null) {
          Files.delete(tmpFile);
        }
      } catch (IOException ignore) {
      }
    }
  }

  @Override
  protected void setInterceptor(Configuration configuration) {
    configuration.setInterceptor(new OBInterceptor());
  }
}
