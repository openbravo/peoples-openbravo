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

package org.openbravo.test.xml;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLProcessor;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.model.financialmgmt.calendar.PeriodControl;
import org.openbravo.model.financialmgmt.payment.DebtPayment;
import org.openbravo.model.materialmgmt.onhandquantity.StoragePending;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;
import org.openbravo.service.db.ClientImportProcessor;
import org.openbravo.service.db.DataExportService;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Tests export and import of client dataset.
 * 
 * @author mtaal
 */

public class ExportClientTest extends XMLBaseTest {
    public void testExportImportClient1000000() {
        exportImport("1000000");
    }

    public void testExportImportClient1000001() {
        exportImport("1000001");
    }

    private void exportImport(String clientId) {
        setErrorOccured(true);
        setUserContext("0");
        final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(
                DataSet.class);
        obc.add(Expression.eq("name", "Client Definition"));
        assertTrue(obc.list().size() == 1);
        final DataSet dataSet = obc.list().get(0);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ClientID", clientId);
        final String xml = DataExportService.getInstance().exportClientToXML(
                dataSet, null, parameters);

        try {
            final File f = new File("/home/mtaal/mytmp/export.xml");
            if (f.exists()) {
                f.delete();
            }
            final FileWriter fw = new FileWriter(f);
            fw.write(xml);
            fw.close();
        } catch (final Exception e) {
            throw new OBException(e);
        }

        final ClientImportProcessor importProcessor = new TestClientImportProcessor();
        importProcessor.setNewName("" + System.currentTimeMillis());
        try {
            final ImportResult ir = DataImportService.getInstance()
                    .importClientData(xml, importProcessor, null);
            if (ir.getException() != null) {
                ir.getException().printStackTrace(System.err);
                throw new OBException(ir.getException());
            }
            if (ir.getErrorMessages() != null) {
                fail(ir.getErrorMessages());
            }
            System.err.println(ir.getWarningMessages());
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            throw new OBException(e);
        }
        setErrorOccured(false);
    }

    // is added to repair some incorrect data values in test databases
    private class TestClientImportProcessor extends ClientImportProcessor {

        /**
         * @see EntityXMLProcessor#process(List, List)
         */
        @Override
        public void process(List<BaseOBObject> newObjects,
                List<BaseOBObject> updatedObjects) {
            for (final BaseOBObject newObj : newObjects) {
                if (newObj instanceof User && newObj.getId() != null
                        && newObj.getId().equals("-1")) {
                    newObj.setId(null);
                }
            }
            super.process(newObjects, updatedObjects);
        }

        @Override
        public Object replaceValue(BaseOBObject owner, Property property,
                Object importedValue) {
            // if (owner instanceof Project
            // && property.getName().equals("changeProjectStatus")
            // && importedValue != null && importedValue.equals("N")) {
            // return property.getAllowedValues().iterator().next();
            // }
            if (owner instanceof DebtPayment
                    && ((DebtPayment) owner).getCashJournalLine() != null) {
                ((DebtPayment) owner).setCashJournalLine(null);
            } else if (owner.getEntityName().equals("FinancialMgmtPaymentTerm")
                    && property.getName().equals("fixMonthOffset")) {
                return new Integer(1);
            } else if (owner.getEntityName().equals(
                    "MaterialMgmtStoragePending")
                    && property.getName().equals("quantityOrdered")
                    && importedValue != null
                    && ((Number) importedValue).intValue() <= 0) {
                return new BigDecimal(10);
            } else if (owner.getEntityName().equals("Invoice")
                    && property.getName().equals("taxdate")
                    && importedValue == null) {
                return new Date();
            } else if (owner instanceof ElementValue && importedValue != null
                    && importedValue.equals("B")) {
                return "D";
            } else if (owner instanceof PeriodControl && importedValue != null
                    && importedValue.equals("DMP")) {
                return "AMZ";
            } else if (owner instanceof StoragePending && importedValue != null
                    && property.getName().equals("quantityOrdered")
                    && ((BigDecimal) importedValue).intValue() < 10) {
                return new BigDecimal(10);
            } else if (owner instanceof ProductionTransaction
                    && importedValue == null
                    && property.getName().equalsIgnoreCase("endtime")) {
                return new Timestamp(new Date().getTime());
            } else if (owner instanceof EmailServerConfiguration
                    && importedValue == null && importedValue == null) {
                // values are mandatory
                return "test";
            } else if (owner instanceof ProductionPlan && importedValue == null
                    && property.getName().equals("costcenteruse")) {
                return new Float(10);
            } else if (owner instanceof ProductionPlan && importedValue == null
                    && property.getName().equals("neededquantity")) {
                return new Integer(10);
            } else if (owner instanceof ProductionPlan
                    && property.getName().equals("rejectedquantity")) {
                return new Integer(10);
            }

            // - C_Acct_Rpt.c_acctschema_id is mandatory in ad_column but
            // non-mandatory in the database
            // - changed whereclause of ad_user datasettable: id<>'0' and
            // id<>'100' and client.id=:ClientID
            // - changed whereclause of client datasettable: id=:ClientID
            // - M_MatchPO.M_InOutLine_ID to not mandatory
            // - Default value of id of ADUser and of C_YEAR_V are set to -1
            // this does not work for Hibernate
            // - changed C_Project.changeProjectStatus to a YesNo Field

            return super.replaceValue(owner, property, importedValue);
        }

        int i = 0;

        // this method is overridden to make it work for current clients
        @Override
        protected String replace(String currentValue, String replaceValue) {
            // TODO:TODO: note the i = 0 is really just a trick to make it
            // unique,
            // will be replaced with another method
            return currentValue + getNewName() + (i++);
        }

    }
}