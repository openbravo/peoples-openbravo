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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.copyfromorderprocess;

import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;

public interface CopyFromOrdersProcessImplementationInterface {
  public static final String COPY_FROM_ORDER_PROCESS_HOOK_QUALIFIER = "CopyFromOrderProcessHookQualifier";

  public void exec(final Order processingOrder, final OrderLine orderLine, OrderLine newOrderLine);

  // Example of a hook:

  // package org.openbravo.advpaymentmngt.utility;
  //
  // import javax.enterprise.context.Dependent;
  //
  // import org.openbravo.client.kernel.ComponentProvider.Qualifier;
  // import
  // org.openbravo.common.actionhandler.copyfromorderprocess.CopyFromOrdersProcessImplementationInterface;
  // import org.openbravo.model.common.order.Order;
  // import org.openbravo.model.common.order.OrderLine;
  //
  // @Dependent
  // @Qualifier(CopyFromOrdersProcessImplementationInterface.COPY_FROM_ORDER_PROCESS_HOOK_QUALIFIER)
  // public class TestHook implements CopyFromOrdersProcessImplementationInterface {
  //
  // @Override
  // public void exec(Order processingOrder, OrderLine orderLine, OrderLine newOrderLine) {
  // newOrderLine.setDescription("Test");
  //
  // }

}
