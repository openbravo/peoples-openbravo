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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.createlinesfromorderprocess;

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;

/**
 * Interface to be implemented by the hooks to be executed on the Create Lines From Order process.
 * 
 * Example of a hook:
 * 
 * <pre>
 * import javax.enterprise.context.Dependent;
 * import org.openbravo.client.kernel.ComponentProvider.Qualifier;
 * import org.openbravo.common.actionhandler.createlinesfromorderprocess.CreateLinesFromOrderProcessImplementationInterface;
 * import org.openbravo.model.common.invoice.Invoice;
 * import org.openbravo.model.common.invoice.InvoiceLine;
 * import org.openbravo.model.common.order.OrderLine;
 * 
 * &#064;Dependent
 * &#064;Qualifier(CreateLinesFromOrderProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
 * public class TestHook implements CreateLinesFromOrderProcessImplementationInterface {
 * 
 *   &#064;Override
 *   public int getOrder() {
 *     return 10;
 *   }
 * 
 *   &#064;Override
 *   public void exec(final Invoice currentInvoice, final OrderLine copiedOrderLine,
 *       InvoiceLine newInvoiceLine);
 *   }
 * }
 * </pre>
 *
 */
public interface CreateLinesFromOrderProcessImplementationInterface {
  public static final String CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER = "CreatelinesFromProcessHookQualifier";

  /**
   * Returns the order when the concrete hook will be implemented. A positive value will execute the
   * hook after the core's logic
   */
  public int getOrder();

  /**
   * Executes the hook logic on the Copy From Orders process
   * 
   * @param currentInvoice
   *          the Invoice we are updating
   * @param copiedOrderLine
   *          the order line from which we are creating the invoice line
   * @param newInvoiceLine
   *          the new invoice line created within the currentInvoice
   */
  public void exec(final Invoice currentInvoice, final OrderLine copiedOrderLine,
      InvoiceLine newInvoiceLine);
}
