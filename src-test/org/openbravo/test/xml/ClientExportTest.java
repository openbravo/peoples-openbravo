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

package org.openbravo.test.xml;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.service.db.DataExportService;

/**
 * Tests export and import of client dataset.
 * 
 * @author mtaal
 */

public class ClientExportTest extends XMLBaseTest {
  public void _testListClients() {
    setErrorOccured(true);
    setUserContext("0");
    final List<Client> cls = OBDal.getInstance().createCriteria(Client.class).list();
    for (Client c : cls) {
      System.err.println(c.getId() + " " + c.getName());
    }
    setErrorOccured(false);
  }

  public void testExportClient() throws Exception {
    setErrorOccured(true);
    setUserContext("0");
    DataExportService des = DataExportService.getInstance();
    final Map<String, Object> params = new HashMap<String, Object>();
    params.put(DataExportService.CLIENT_ID_PARAMETER_NAME, "1000000");
    final FileWriter fw = new FileWriter("/home/mtaal/mytmp/bb.xml");
    DataExportService.getInstance().exportClientToXML(params, false, fw);
    fw.close();
    setErrorOccured(false);
  }

}