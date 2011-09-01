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

import java.math.BigDecimal;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.manufacturing.processplan.OperationProduct;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class SequenceProductCreate implements Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    try {
      VariablesSecureApp vars = bundle.getContext().toVars();
      final String sequenceProductId = (String) bundle.getParams().get("MA_Sequenceproduct_ID");
      final String value = (String) bundle.getParams().get("value");
      final String name = (String) bundle.getParams().get("name");
      final String productionType = (String) bundle.getParams().get("productiontype");
      final String Qty = (String) bundle.getParams().get("qty");
      final ConnectionProvider conn = bundle.getConnection();

      // Create new product copy of selected
      OperationProduct OpProduct = OBDal.getInstance().get(OperationProduct.class,
          sequenceProductId);

      Product originalProduct = OpProduct.getProduct();
      Product newProduct = (Product) DalUtil.copy(originalProduct);

      // Modifies values
      newProduct.setSearchKey(value);
      newProduct.setName(name);

      // Empty values copied and filled by m_product_trg
      newProduct.setProductAccountsList(null);
      newProduct.setProductTrlList(null);

      // Save product
      OBDal.getInstance().save(newProduct);

      OBDal.getInstance().flush();

      // Create Operation Product line

      OperationProduct newOpProduct = OBProvider.getInstance().get(OperationProduct.class);

      newOpProduct.setMASequence(OpProduct.getMASequence());
      newOpProduct.setClient(OpProduct.getClient());
      newOpProduct.setOrganization(OpProduct.getOrganization());
      newOpProduct.setProduct(newProduct);
      newOpProduct.setQuantity(new BigDecimal(Qty));
      newOpProduct.setUOM(newProduct.getUOM());
      newOpProduct.setProductionType(productionType);

      // Save Operation Product line
      OBDal.getInstance().save(newOpProduct);

      OBDal.getInstance().flush();

      final OBError msg = new OBError();

      msg.setType("Success");
      msg.setTitle(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      msg.setMessage(Utility.messageBD(conn, "IOProductCreated", bundle.getContext().getLanguage())
          + newProduct.getName() + " " + Qty + " P" + productionType);
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
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
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }

}
