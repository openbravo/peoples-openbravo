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

package org.openbravo.common.actionhandler.createlinesfromprocess;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;

/**
 * Interface to be implemented by the hooks to be executed on the Create Lines From Order process.
 * 
 * Example of a hook:
 * 
 * <pre>
 * import javax.enterprise.context.Dependent;
 * import org.openbravo.client.kernel.ComponentProvider.Qualifier;
 * import org.openbravo.common.actionhandler.createlinesfromprocess.CreateLinesFromProcessImplementationInterface;
 * import org.openbravo.base.structure.BaseOBObject;
 * import org.openbravo.model.common.invoice.Invoice;
 * import org.openbravo.model.common.invoice.InvoiceLine;
 * 
 * &#064;Dependent
 * &#064;Qualifier(CreateLinesFromProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
 * public class TestHook implements CreateLinesFromProcessImplementationInterface {
 * 
 *   &#064;Override
 *   public int getOrder() {
 *     return 10;
 *   }
 * 
 *   &#064;Override
 *   public void exec(final Invoice currentInvoice, final JSONObject pickExecuteLineValues,
 *       final BaseOBObject copiedLine, InvoiceLine newInvoiceLine) {
 *     newInvoiceLine.setDescription(&quot;Test&quot;);
 *   }
 * }
 * </pre>
 *
 */
public interface CreateLinesFromProcessImplementationInterface {
  static final String CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER = "CreatelinesFromProcessHookQualifier";

  /**
   * Returns the order when the concrete hook will be implemented. A positive value will execute the
   * hook after the core's logic
   */
  public int getOrder();

  /**
   * Executes the hook logic on the Create Lines From process
   * 
   * @param currentInvoice
   *          the Invoice we are updating
   * @param pickExecuteLineValues
   *          The values selected/calculated in the line of the Pick and Execute. These values can
   *          differ from the values of the original record been copied (copiedLine) if they are
   *          recalculated in the query or changed in the Pick and Execute window (for a future
   *          behavior)
   * @param copiedLine
   *          the order/shipment/receipt line from which we are creating the invoice line
   * @param newInvoiceLine
   *          the new invoice line created within the currentInvoice
   */
  public void exec(final Invoice currentInvoice, final JSONObject pickExecuteLineValues,
      final BaseOBObject copiedLine, InvoiceLine newInvoiceLine);
}
