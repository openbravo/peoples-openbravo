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

package org.openbravo.base.seam.test;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openbravo.base.seam.remote.DataResolvingMode;
import org.openbravo.base.seam.remote.DataToJsonConverter;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the conversion of data to and from json strings.
 * 
 * @author mtaal
 */
public class TestToJsonConverter extends BaseTest {

  private static final long serialVersionUID = 1L;

  public void testJSONSerialization() throws Exception {
    setBigBazaarUserContext();
    addReadWriteAccess(BusinessPartner.class);
    final OBCriteria<BusinessPartner> criteria = OBDal.getInstance().createCriteria(
        BusinessPartner.class);
    final List<BusinessPartner> list = criteria.list();

    final DataToJsonConverter toJsonConverter = new DataToJsonConverter();
    final String jsonStringBPFULL = toJsonConverter.convertToJsonString(list.get(0),
        DataResolvingMode.FULL);
    new JSONObject(jsonStringBPFULL);
    final String jsonStringBPsFull = toJsonConverter.convertToJsonString(list,
        DataResolvingMode.FULL);
    System.err.println(jsonStringBPFULL);
    new JSONArray(jsonStringBPsFull);
    final String jsonStringBPsShort = toJsonConverter.convertToJsonString(list,
        DataResolvingMode.SHORT);
    System.err.println(jsonStringBPsShort);
    final JSONArray jsonArrayShort = new JSONArray(jsonStringBPsShort);
    System.err.println(jsonArrayShort);
  }
}