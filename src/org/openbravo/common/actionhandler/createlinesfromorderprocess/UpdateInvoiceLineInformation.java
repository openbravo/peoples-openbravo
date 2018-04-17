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

import java.util.Set;

import javax.enterprise.context.Dependent;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;

@Dependent
@Qualifier(CreateLinesFromOrderProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdateInvoiceLineInformation implements CreateLinesFromOrderProcessImplementationInterface {
  private Invoice processingInvoice;
  private OrderLine orderLine;
  private InvoiceLine invoiceLine;

  @Override
  public int getOrder() {
    return -50;
  }

  /**
   * Updates the Information of the new Invoice Line that is related with the Invoice Header and the
   * copied order line.
   */
  @Override
  public void exec(final Invoice currentInvoice, final OrderLine copiedOrderLine,
      InvoiceLine newInvoiceLine) {
    this.processingInvoice = currentInvoice;
    this.orderLine = copiedOrderLine;
    this.invoiceLine = newInvoiceLine;

    // Create to the new invoice line the reference to the order line from it is created
    updateOrderLineReference();

    // Information updated from invoice header: Client, Description and Business Partner
    updateInformationFromInvoice();

    // Information updated from orderLine: Organization, Project, CostCenter, Asset, User1
    // Dimension and User2 Dimension
    udpateInformationFromOrderLine();

  }

  /**
   * Creates to the new invoice line the reference to the order line from it is created.
   */
  private void updateOrderLineReference() {
    invoiceLine.setSalesOrderLine(orderLine);
  }

  /**
   * Updates some invoice line information from the order it is created and links the invoice line
   * to the order.
   */
  private void updateInformationFromInvoice() {
    invoiceLine.setInvoice(processingInvoice);
    invoiceLine.setClient(processingInvoice.getClient());
    invoiceLine.setDescription(processingInvoice.getDescription());
    invoiceLine.setBusinessPartner(processingInvoice.getBusinessPartner());
  }

  private void udpateInformationFromOrderLine() {
    invoiceLine.setOrganization(getOrganizationForNewLine());
    invoiceLine.setProject(orderLine.getProject());
    invoiceLine.setCostcenter(orderLine.getCostcenter());
    invoiceLine.setAsset(orderLine.getAsset());
    invoiceLine.setStDimension(orderLine.getStDimension());
    invoiceLine.setNdDimension(orderLine.getNdDimension());
  }

  private Organization getOrganizationForNewLine() {
    Organization organizationForNewLine = processingInvoice.getOrganization();
    Set<String> parentOrgTree = new OrganizationStructureProvider().getChildTree(
        organizationForNewLine.getId(), true);
    // If the Organization of the line that is being copied belongs to the child tree of the
    // Organization of the document header of the new line, use the organization of the line being
    // copied, else use the organization of the document header of the new line
    if (parentOrgTree.contains(orderLine.getOrganization().getId())) {
      organizationForNewLine = orderLine.getOrganization();
    }
    return organizationForNewLine;
  }

}
