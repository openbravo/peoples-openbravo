/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.model.Entity;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class ProcessVoidLayaway extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;

  @Override
  public JSONObject saveRecord(JSONObject jsonRecord) throws Exception {

    JSONArray respArray = new JSONArray();
    JSONObject jsonorder = (JSONObject) jsonRecord.get("order");
    Order order = OBDal.getInstance().get(Order.class, jsonorder.getString("id"));

    VoidLayaway proc = WeldUtils.getInstanceFromStaticBeanManager(VoidLayaway.class);
    proc.voidLayaway(jsonorder, order);

    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    return result;

  }

  protected String getImportQualifier() {
    return "OBPOS_VoidLayaway";
  }

  protected DocumentType getPaymentDocumentType(Organization org) {
    if (paymentDocTypes.get(DalUtil.getId(org)) != null) {
      return paymentDocTypes.get(DalUtil.getId(org));
    }
    final DocumentType docType = FIN_Utility.getDocumentType(org, AcctServer.DOCTYPE_ARReceipt);
    paymentDocTypes.put((String) DalUtil.getId(org), docType);
    return docType;

  }

  protected String getPaymentDescription() {
    if (paymentDescription == null) {
      String language = RequestContext.get().getVariablesSecureApp().getLanguage();
      paymentDescription = Utility.messageBD(new DalConnectionProvider(false), "OrderDocumentno",
          language);
    }
    return paymentDescription;
  }

  protected String getDocumentNo(Entity entity, DocumentType doctypeTarget, DocumentType doctype) {
    return Utility.getDocumentNo(OBDal.getInstance().getConnection(false),
        new DalConnectionProvider(false), RequestContext.get().getVariablesSecureApp(), "", entity
            .getTableName(), doctypeTarget == null ? "" : doctypeTarget.getId(),
        doctype == null ? "" : doctype.getId(), false, true);
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.voidLayaway";
  }
}
