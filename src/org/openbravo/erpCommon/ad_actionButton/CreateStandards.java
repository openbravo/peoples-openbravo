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
package org.openbravo.erpCommon.ad_actionButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.AttributeSetInstanceValue;
import org.openbravo.erpCommon.utility.AttributeSetInstanceValueData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.manufacturing.processplan.OperationProduct;
import org.openbravo.model.manufacturing.processplan.OperationProductAttribute;
import org.openbravo.model.manufacturing.transaction.WorkRequirementProduct;
import org.openbravo.model.materialmgmt.transaction.ProductionLine;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.utils.Replace;

public class CreateStandards implements org.openbravo.scheduling.Process {

  private static final String lotSearchKey = "LOT";
  private static final String serialNoSearchKey = "SNO";
  private static final String expirationDateSearchKey = "EXD";
  private static final Logger log4j = Logger.getLogger(CreateStandards.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    final String strMProductionPlanID = (String) bundle.getParams().get("M_ProductionPlan_ID");
    final ConnectionProvider conn = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();

    try {

      ProductionPlan productionPlan = OBDal.getInstance().get(ProductionPlan.class,
          strMProductionPlanID);

      createStandards(productionPlan, conn, vars);
      OBDal.getInstance().save(productionPlan);
      OBDal.getInstance().flush();

      copyAttributes(conn, vars, productionPlan);
      createInstanciableAttributes(conn, vars, productionPlan);

      final OBError msg = new OBError();

      msg.setType("Success");
      msg.setTitle(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      msg.setMessage(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log4j.error("Error creating standards", e);
      final OBError msg = new OBError();
      msg.setType("Error");
      if (e instanceof org.hibernate.exception.GenericJDBCException) {
        msg.setMessage(((org.hibernate.exception.GenericJDBCException) e).getSQLException()
            .getNextException().getMessage());
      } else if (e instanceof org.hibernate.exception.ConstraintViolationException) {
        msg.setMessage(((org.hibernate.exception.ConstraintViolationException) e).getSQLException()
            .getNextException().getMessage());
      } else {
        msg.setMessage(e.getMessage());
      }
      msg.setTitle(Utility.messageBD(conn, "Error", bundle.getContext().getLanguage()));
      bundle.setResult(msg);
    }
  }

  private void createStandards(ProductionPlan productionplan, ConnectionProvider conn,
      VariablesSecureApp vars) throws Exception {
    try {
      OBContext.setAdminMode(true);

      org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, "800105");

      final ProcessInstance pInstance = OBProvider.getInstance().get(ProcessInstance.class);
      pInstance.setProcess(process);
      pInstance.setActive(true);
      pInstance.setRecordID(productionplan.getId());
      pInstance.setUserContact(OBContext.getOBContext().getUser());

      OBDal.getInstance().save(pInstance);
      OBDal.getInstance().flush();

      try {
        final Connection connection = OBDal.getInstance().getConnection();
        PreparedStatement ps = null;
        final Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();
        if (obProps.getProperty("bbdd.rdbms") != null
            && obProps.getProperty("bbdd.rdbms").equals("POSTGRE")) {
          ps = connection.prepareStatement("SELECT * FROM ma_productionrun_standard(?)");
        } else {
          ps = connection.prepareStatement("CALL ma_productionrun_standard(?)");
        }
        ps.setString(1, pInstance.getId());
        ps.execute();

      } catch (Exception e) {
        throw new IllegalStateException(e);
      }

      OBDal.getInstance().getSession().refresh(pInstance);

      if (pInstance.getResult() == 0) {
        // error processing
        OBError myMessage = Utility.getProcessInstanceMessage(conn, vars,
            getPInstanceData(pInstance));
        throw new OBException("ERROR: " + myMessage.getMessage());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private PInstanceProcessData[] getPInstanceData(ProcessInstance pInstance) throws Exception {
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PInstanceProcessData objectPInstanceProcessData = new PInstanceProcessData();
    objectPInstanceProcessData.result = pInstance.getResult().toString();
    objectPInstanceProcessData.errormsg = pInstance.getErrorMsg();
    objectPInstanceProcessData.pMsg = "";
    vector.addElement(objectPInstanceProcessData);
    PInstanceProcessData pinstanceData[] = new PInstanceProcessData[1];
    vector.copyInto(pinstanceData);
    return pinstanceData;
  }

  private void copyAttributes(ConnectionProvider conn, VariablesSecureApp vars,
      ProductionPlan productionPlan) throws Exception {
    try {
      OBContext.setAdminMode(true);

      // check phase exists
      if (productionPlan.getWRPhase() != null
          && productionPlan.getWRPhase().getMASequence() != null) {

        // loop production lines
        for (OperationProduct opProduct : productionPlan.getWRPhase().getMASequence()
            .getManufacturingOperationProductList()) {
          // only production type + and has attset and has attlist
          if (opProduct.getProductionType() != null && opProduct.getProductionType().equals("+")
              && !opProduct.getManufacturingOperationProductAttributeList().isEmpty()
              && opProduct.getProduct() != null && opProduct.getProduct().getAttributeSet() != null) {

            // new Attribute
            AttributeSetInstanceValue attSetInstanceTo = new AttributeSetInstanceValue();
            HashMap<String, String> attValues = new HashMap<String, String>();

            // loop attributes
            for (OperationProductAttribute opProductAtt : opProduct
                .getManufacturingOperationProductAttributeList()) {

              // check attFrom exists
              AttributeSetInstance attSetInstanceFrom = null;

              OBCriteria<ProductionLine> ProductionLineCriteria = OBDal.getInstance()
                  .createCriteria(ProductionLine.class);
              ProductionLineCriteria.add(Restrictions.eq(ProductionLine.PROPERTY_PRODUCTIONPLAN,
                  productionPlan));
              ProductionLineCriteria.createAlias(ProductionLine.PROPERTY_WRPRODUCTPHASE, "wrpp");
              ProductionLineCriteria
                  .add(Restrictions.eq("wrpp." + WorkRequirementProduct.PROPERTY_SEQUENCEPRODUCT,
                      opProductAtt.getProductFrom()));

              List<ProductionLine> plinesToCopyFrom = ProductionLineCriteria.list();

              if (!plinesToCopyFrom.isEmpty()) {
                int i = 0;
                while (attSetInstanceFrom == null) {
                  attSetInstanceFrom = plinesToCopyFrom.get(i).getAttributeSetValue();
                  i++;
                }
              }

              if (attSetInstanceFrom != null && !attSetInstanceFrom.getId().equals("0")) {
                if (opProductAtt.isSpecialatt()) {
                  // special att
                  // lot
                  if (opProductAtt.getSpecialatt().equals(lotSearchKey))
                    attSetInstanceTo.setLot(attSetInstanceFrom.getLotName());
                  // serNo
                  if (opProductAtt.getSpecialatt().equals(serialNoSearchKey))
                    attSetInstanceTo.setSerialNumber(attSetInstanceFrom.getSerialNo());
                  // gDate
                  if (opProductAtt.getSpecialatt().equals(expirationDateSearchKey)) {
                    attSetInstanceTo.setGuaranteeDate(dateToString(attSetInstanceFrom
                        .getExpirationDate()));
                  }
                } else {
                  // normal att
                  // check attTo exists
                  if (opProductAtt.getAttributeuseto() != null
                      && opProductAtt.getAttributeuseto().getAttribute() != null) {
                    // getValue from
                    OBCriteria<AttributeInstance> attributeInstanceCriteria = OBDal.getInstance()
                        .createCriteria(AttributeInstance.class);
                    attributeInstanceCriteria.add(Restrictions.eq(
                        AttributeInstance.PROPERTY_ATTRIBUTESETVALUE, attSetInstanceFrom));
                    attributeInstanceCriteria.add(Restrictions.eq(
                        AttributeInstance.PROPERTY_ATTRIBUTE, opProductAtt.getAttributeUse()
                            .getAttribute()));
                    List<AttributeInstance> AttributeInstanceList = attributeInstanceCriteria
                        .list();
                    // add value
                    if (!AttributeInstanceList.isEmpty()) {
                      if (AttributeInstanceList.get(0).getAttributeValue() == null) {
                        attValues.put(replace(opProductAtt.getAttributeuseto().getAttribute()
                            .getName()), AttributeInstanceList.get(0).getSearchKey());
                      } else {
                        attValues.put(replace(opProductAtt.getAttributeuseto().getAttribute()
                            .getName()), AttributeInstanceList.get(0).getAttributeValue().getId());
                      }

                    }
                  }
                }
              }
            } // end loop attributes

            // update lines

            OBCriteria<ProductionLine> ProductionLineCriteria = OBDal.getInstance().createCriteria(
                ProductionLine.class);
            ProductionLineCriteria.add(Restrictions.eq(ProductionLine.PROPERTY_PRODUCTIONPLAN,
                productionPlan));
            ProductionLineCriteria
                .add(Restrictions.eq(ProductionLine.PROPERTY_PRODUCTIONTYPE, "+"));
            ProductionLineCriteria.createAlias(ProductionLine.PROPERTY_WRPRODUCTPHASE, "wrpp");
            ProductionLineCriteria.add(Restrictions.eq("wrpp."
                + WorkRequirementProduct.PROPERTY_SEQUENCEPRODUCT, opProduct));

            List<ProductionLine> plinesToCopyTo = ProductionLineCriteria.list();

            for (ProductionLine pline : plinesToCopyTo) {

              // create attribute
              if (pline.getProduct().getAttributeSet().isExpirationDate()
                  && (attSetInstanceTo.getGuaranteeDate() == null || attSetInstanceTo
                      .getGuaranteeDate().equals(""))
                  && pline.getProduct().getAttributeSet().getGuaranteedDays() != null
                  && pline.getProduct().getAttributeSet().getGuaranteedDays() != 0L) {
                // set guaranteeDate if is not copied
                Date movementdate = ((productionPlan.getProductionplandate() != null) ? productionPlan
                    .getProductionplandate() : productionPlan.getProduction().getMovementDate());
                int days = pline.getProduct().getAttributeSet().getGuaranteedDays().intValue();
                attSetInstanceTo.setGuaranteeDate(dateToString(addDays(movementdate, days)));
              }
              AttributeSetInstanceValueData[] data = AttributeSetInstanceValueData.select(conn,
                  opProduct.getProduct().getAttributeSet().getId());
              OBError createAttributeInstanceError = attSetInstanceTo.setAttributeInstance(conn,
                  vars, data, opProduct.getProduct().getAttributeSet().getId(), "", "", "N",
                  opProduct.getProduct().getId(), attValues);
              if (!createAttributeInstanceError.getType().equals("Success")) {
                throw new OBException(createAttributeInstanceError.getMessage());
              }

              OBDal.getInstance().flush();

              AttributeSetInstance newAttSetinstance = OBDal.getInstance().get(
                  AttributeSetInstance.class, attSetInstanceTo.getAttSetInstanceId());

              pline.setAttributeSetValue(newAttSetinstance);
              OBDal.getInstance().save(pline);
            }
            OBDal.getInstance().flush();
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void createInstanciableAttributes(ConnectionProvider conn, VariablesSecureApp vars,
      ProductionPlan productionPlan) throws Exception {
    try {
      OBContext.setAdminMode(true);
      OBCriteria<ProductionLine> ProductionLineCriteria = OBDal.getInstance().createCriteria(
          ProductionLine.class);
      ProductionLineCriteria.add(Restrictions.eq(ProductionLine.PROPERTY_PRODUCTIONPLAN,
          productionPlan));
      ProductionLineCriteria.add(Restrictions.eq(ProductionLine.PROPERTY_PRODUCTIONTYPE, "+"));
      List<ProductionLine> plines = ProductionLineCriteria.list();
      for (ProductionLine line : plines) {
        // check has empty attribute
        if (line.getProduct().getAttributeSet() != null && line.getAttributeSetValue() == null) {
          AttributeSet attSet = line.getProduct().getAttributeSet();
          // check if has automatic attributes
          if ((attSet.isLot() && attSet.getLotControl() != null)
              || (attSet.isSerialNo() && attSet.getSerialNoControl() != null)
              || (attSet.isExpirationDate() && attSet.getGuaranteedDays() != null && attSet
                  .getGuaranteedDays() != 0L)) {

            AttributeSetInstanceValue attSetInstance = new AttributeSetInstanceValue();
            HashMap<String, String> attValues = new HashMap<String, String>();

            if (attSet.isExpirationDate()) {
              Date movementdate = ((productionPlan.getProductionplandate() != null) ? productionPlan
                  .getProductionplandate() : productionPlan.getProduction().getMovementDate());
              int days = attSet.getGuaranteedDays().intValue();
              attSetInstance.setGuaranteeDate(dateToString(addDays(movementdate, days)));
            }
            AttributeSetInstanceValueData[] data = AttributeSetInstanceValueData.select(conn,
                attSet.getId());
            OBError createAttributeInstanceError = attSetInstance.setAttributeInstance(conn, vars,
                data, attSet.getId(), "", "", "N", line.getProduct().getId(), attValues);
            if (!createAttributeInstanceError.getType().equals("Success"))
              throw new OBException(createAttributeInstanceError.getMessage());

            OBDal.getInstance().flush();

            AttributeSetInstance newAttSetinstance = OBDal.getInstance().get(
                AttributeSetInstance.class, attSetInstance.getAttSetInstanceId());

            line.setAttributeSetValue(newAttSetinstance);
            OBDal.getInstance().save(line);
          }
          OBDal.getInstance().flush();
        }
      }

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String replace(String strIni) {
    // delete characters: " ","&",","
    return Replace.replace(Replace.replace(Replace.replace(
        Replace.replace(Replace.replace(Replace.replace(strIni, "#", ""), " ", ""), "&", ""), ",",
        ""), "(", ""), ")", "");
  }

  private String dateToString(Date date) throws Exception {
    if (date == null)
      return "";
    String dateformat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat formater = new SimpleDateFormat(dateformat);
    return formater.format(date);
  }

  private Date addDays(Date date, int days) throws Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, days);
    Date gdate = calendar.getTime();
    return gdate;
  }
}
