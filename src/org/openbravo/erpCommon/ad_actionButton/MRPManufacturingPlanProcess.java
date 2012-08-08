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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.mrp.ProductionRun;
import org.openbravo.model.mrp.ProductionRunLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class MRPManufacturingPlanProcess extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(MRPManufacturingPlanProcess.class);
  private static final String NULL = null;

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    final String strManufacturingMRPID = (String) bundle.getParams().get("MRP_Run_Production_ID");
    final ProductionRun productionRun = OBDal.getInstance().get(ProductionRun.class,
        strManufacturingMRPID);
    final String userId = (String) DalUtil.getId(OBContext.getOBContext().getUser());
    final String orgId = (String) DalUtil.getId(productionRun.getOrganization());
    final String clientId = (String) DalUtil.getId(productionRun.getClient());
    final String plannerId = productionRun.getPlanner() != null ? (String) DalUtil
        .getId(productionRun.getPlanner()) : NULL;
    final String productId = productionRun.getProduct() != null ? (String) DalUtil
        .getId(productionRun.getProduct()) : NULL;
    final String productCategoryId = productionRun.getProductCategory() != null ? (String) DalUtil
        .getId(productionRun.getProductCategory()) : NULL;
    final String bpId = productionRun.getBusinessPartner() != null ? (String) DalUtil
        .getId(productionRun.getBusinessPartner()) : NULL;
    final String bpCatId = productionRun.getBusinessPartnerCategory() != null ? (String) DalUtil
        .getId(productionRun.getBusinessPartnerCategory()) : NULL;
    final long timeHorizon = productionRun.getTimeHorizon();
    final long safetyLeadTime = productionRun.getSafetyLeadTime();
    final Date docDate = productionRun.getDocumentDate();

    try {
      log4j.debug("Prepare process delete not fixed lines and set exploded to false.");

      deleteNotFixedLines(strManufacturingMRPID);

      ScrollableResults linesToUpdate = getLinesToUpdate(strManufacturingMRPID);
      int i = 0;
      while (linesToUpdate.next()) {
        ProductionRunLine prLine = (ProductionRunLine) linesToUpdate.get(0);
        prLine.setInserted(false);
        OBDal.getInstance().save(prLine);

        if (i % 100 == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
      }

      log4j.debug("Call MRP_Run_Initialize process");
      // v_ResultStr:='Initialize';
      // MRP_RUN_INITIALIZE(v_User_ID, v_Org_ID, v_Client_ID, v_Record_ID, v_Planner_ID,
      // v_Product_ID,
      // v_Product_Category_ID, v_BPartner_ID, v_BP_Group_ID, NULL, v_TimeHorizon,
      // v_PlanningDate, 'Y');
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(userId);
      parameters.add(orgId);
      parameters.add(clientId);
      parameters.add(strManufacturingMRPID);
      parameters.add(plannerId);
      parameters.add(productId);
      parameters.add(productCategoryId);
      parameters.add(bpId);
      parameters.add(bpCatId);
      parameters.add(NULL);
      parameters.add(timeHorizon);
      parameters.add(docDate);
      parameters.add("Y");
      call("MRP_RUN_INITIALIZE", parameters, null);

      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().getSession().clear();

      log4j.debug("Call MRP_Run_Explode process");
      // v_ResultStr:='Explode';
      // MRP_RUN_EXPLODE(v_User_ID, v_Org_ID, v_Client_ID, v_Record_ID, v_Planner_ID, v_TimeHorizon,
      // v_PlanningDate, v_SecurityMargin);
      parameters = new ArrayList<Object>();
      parameters.add(userId);
      parameters.add(orgId);
      parameters.add(clientId);
      parameters.add(strManufacturingMRPID);
      parameters.add(plannerId);
      parameters.add(timeHorizon);
      parameters.add(docDate);
      parameters.add(safetyLeadTime);
      call("MRP_RUN_EXPLODE", parameters, null);
      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().getSession().clear();

      log4j.debug("Call MRP_ProcessPlan process");
      // v_ResultStr:='ProcessPlan';
      // MRP_PROCESSPLAN(v_User_ID, v_Org_ID, v_Client_ID, v_Record_ID, v_Planner_ID, v_TimeHorizon,
      // v_PlanningDate, v_SecurityMargin);
      parameters = new ArrayList<Object>();
      parameters.add(userId);
      parameters.add(orgId);
      parameters.add(clientId);
      parameters.add(strManufacturingMRPID);
      parameters.add(plannerId);
      parameters.add(timeHorizon);
      parameters.add(docDate);
      parameters.add(safetyLeadTime);
      call("MRP_PROCESSPLAN", parameters, null);

      OBDal.getInstance().commitAndClose();
      OBDal.getInstance().getSession().clear();
    } catch (final Exception e) {
      deleteNotFixedLines(strManufacturingMRPID);

      log4j.error("Exception found in MRPManufacturingProcess: ", e);
      msg = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(),
          OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
      msg.setTitle(OBMessageUtils.messageBD("Error"));

    } finally {
      bundle.setResult(msg);
    }

  }

  private void deleteNotFixedLines(String strManufacturingMRPID) {
    StringBuffer deleteLines = new StringBuffer();
    deleteLines.append(" delete from " + ProductionRunLine.ENTITY_NAME);
    deleteLines.append(" where " + ProductionRunLine.PROPERTY_MANUFACTURINGPLAN + ".id = :prodRun");
    deleteLines.append("   and " + ProductionRunLine.PROPERTY_FIXED + " = false");
    Query delete = OBDal.getInstance().getSession().createQuery(deleteLines.toString());
    delete.setString("prodRun", strManufacturingMRPID);
    delete.executeUpdate();
    OBDal.getInstance().flush();
  }

  private ScrollableResults getLinesToUpdate(String productionRunId) {
    StringBuffer where = new StringBuffer();
    where.append(" where " + ProductionRunLine.PROPERTY_MANUFACTURINGPLAN + ".id = :prodRun");
    where.append("   and " + ProductionRunLine.PROPERTY_QUANTITY + " < 0");
    where.append("   and " + ProductionRunLine.PROPERTY_TRANSACTIONTYPE + " <> 'WR'");
    OBQuery<ProductionRunLine> prlQry = OBDal.getInstance().createQuery(ProductionRunLine.class,
        where.toString());
    prlQry.setNamedParameter("prodRun", productionRunId);

    return prlQry.scroll(ScrollMode.FORWARD_ONLY);
  }

  private void call(String name, List<Object> parameters, List<Class<?>> types) {
    final StringBuilder sb = new StringBuilder();

    final Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    if (obProps.getProperty("bbdd.rdbms") != null
        && obProps.getProperty("bbdd.rdbms").equals("POSTGRE")) {
      sb.append("SELECT * FROM ");
    } else {
      sb.append(" CALL ");
    }

    sb.append(name);
    for (int i = 0; i < parameters.size(); i++) {
      if (i == 0) {
        sb.append("(");
      } else {
        sb.append(",");
      }
      sb.append("?");
    }
    if (parameters.size() > 0) {
      sb.append(")");
    }
    final Connection conn = OBDal.getInstance().getConnection(true);
    try {
      final PreparedStatement ps = conn.prepareStatement(sb.toString(),
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      int index = 0;

      for (Object parameter : parameters) {
        final int sqlIndex = index + 1;
        if (parameter == null) {
          if (types == null || types.size() < index) {
            ps.setNull(sqlIndex, Types.NULL);
          } else {
            ps.setNull(sqlIndex, getSqlType(types.get(index)));
          }
        } else if (parameter instanceof String && parameter.toString().equals("")) {
          ps.setNull(sqlIndex, Types.VARCHAR);
        } else if (parameter instanceof Boolean) {
          ps.setObject(sqlIndex, ((Boolean) parameter) ? "Y" : "N");
        } else if (parameter instanceof BaseOBObject) {
          ps.setObject(sqlIndex, ((BaseOBObject) parameter).getId());
        } else if (parameter instanceof Timestamp) {
          ps.setTimestamp(sqlIndex, (Timestamp) parameter);
        } else if (parameter instanceof Date) {
          ps.setDate(sqlIndex, new java.sql.Date(((Date) parameter).getTime()));
        } else {
          ps.setObject(sqlIndex, parameter);
        }
        index++;
      }
      final ResultSet resultSet = ps.executeQuery();
      resultSet.close();
      ps.close();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private int getSqlType(Class<?> clz) {
    if (clz == null) {
      return Types.VARCHAR;
    }
    if (clz == Boolean.class) {
      return Types.VARCHAR;
    } else if (clz == String.class) {
      return Types.VARCHAR;
    } else if (clz == BaseOBObject.class) {
      return Types.VARCHAR;
    } else if (Number.class.isAssignableFrom(clz)) {
      return Types.NUMERIC;
    } else if (clz == Timestamp.class) {
      return Types.TIMESTAMP;
    } else if (Date.class.isAssignableFrom(clz)) {
      return Types.DATE;
    } else if (BaseOBObject.class.isAssignableFrom(clz)) {
      return Types.VARCHAR;
    } else {
      throw new IllegalStateException("Type not supported, please add it here " + clz.getName());
    }
  }
}
