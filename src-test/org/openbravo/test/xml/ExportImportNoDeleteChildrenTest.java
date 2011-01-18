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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.financialmgmt.tax.TaxZone;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;
import org.openbravo.test.base.BaseTest;

/**
 * Tests this issue:
 * 
 * Datasets marked as business objects, when applying the dataset delete records created manually
 * 
 * https://issues.openbravo.com/view.php?id=15690
 * 
 * @author mtaal
 */

public class ExportImportNoDeleteChildrenTest extends BaseTest {

  private static final String TAX_ID = "FF8081812D9B28B8012D9B29266B0010";

  // this test goes through the following tests:
  // 1) read a taxrate and its children
  // 2) remove some children and one new child and export to xml
  // 3) rollback/do not persist the removal of the children, so the xml contains less children
  // than there are in the database
  // 4) import the xml (using a new transaction)
  // 5) commit
  // 6) reread the tax rate from the db
  // 7) check that no children have been deleted and that the new child has been added
  public void testExportDeleteImport() throws Exception {
    setUserContext(QA_TEST_ADMIN_USER_ID);

    final TaxRate tax1 = OBDal.getInstance().get(TaxRate.class, TAX_ID);
    // make a copy
    final List<TaxZone> taxZones1 = new ArrayList<TaxZone>(tax1.getFinancialMgmtTaxZoneList());
    // remove at various places
    final List<String> removedZoneIds = new ArrayList<String>();
    TaxZone copyOne = null;
    for (int i = 0; i < 10; i++) {
      if (i == 0) {
        copyOne = tax1.getFinancialMgmtTaxZoneList().get(i);
      }
      removedZoneIds.add(tax1.getFinancialMgmtTaxZoneList().remove(i * 10).getId());
    }
    // create a copy
    copyOne = (TaxZone) DalUtil.copy(copyOne);
    OBDal.getInstance().save(copyOne);
    OBDal.getInstance().flush();
    tax1.getFinancialMgmtTaxZoneList().add(copyOne);

    // export
    final List<TaxRate> rates = Collections.singletonList(tax1);
    final String xml = getXML(rates);
    // are really not present in the xml
    for (String removedZoneId : removedZoneIds) {
      assertFalse(xml.contains(removedZoneId));
    }
    // the new child should be there
    assertTrue(xml.contains(copyOne.getId()));

    // rollback to not persist our removal
    OBDal.getInstance().rollbackAndClose();

    // import the xml
    final TaxRate tax2 = OBDal.getInstance().get(TaxRate.class, TAX_ID);
    assertTrue(tax1 != tax2);
    assertEquals(taxZones1.size(), tax2.getFinancialMgmtTaxZoneList().size());
    addReadWriteAccess(TaxRate.class);
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    assertFalse(ir.hasErrorOccured());

    OBDal.getInstance().commitAndClose();

    // now start again
    final TaxRate tax3 = OBDal.getInstance().get(TaxRate.class, TAX_ID);
    // different objects
    assertTrue(tax2 != tax3);
    assertEquals(tax2.getId(), tax3.getId());
    final List<TaxZone> taxZones3 = new ArrayList<TaxZone>(tax3.getFinancialMgmtTaxZoneList());

    // + 1 cause one new child has been added
    assertEquals(taxZones1.size() + 1, taxZones3.size());
    boolean foundCopyOne = false;
    for (int i = 0; i < taxZones1.size(); i++) {
      if (taxZones3.get(i).getId().equals(copyOne.getId())) {
        foundCopyOne = false;
        continue;
      }
      assertEquals(taxZones1.get(i).getId(), taxZones3.get(i).getId());
    }
    // not found in the middle must be at the end
    if (!foundCopyOne) {
      assertEquals(taxZones3.get(taxZones3.size() - 1).getId(), copyOne.getId());
    }
    // don't persist anything we don't want
    OBDal.getInstance().rollbackAndClose();
  }

  @SuppressWarnings("unchecked")
  protected <T extends BaseOBObject> String getXML(List<T> pts) {
    final EntityXMLConverter exc = EntityXMLConverter.newInstance();
    exc.setOptionIncludeReferenced(true);
    exc.setOptionEmbedChildren(true);
    exc.setOptionIncludeChildren(true);
    exc.setAddSystemAttributes(false);
    return exc.toXML((List<BaseOBObject>) pts);
  }
}
