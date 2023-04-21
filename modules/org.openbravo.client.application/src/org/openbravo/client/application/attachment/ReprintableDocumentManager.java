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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.attachment;

import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.attachment.AttachmentUtils.AttachmentType;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.ReprintableDocument;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;

/**
 * Centralizes the {@link ReprintableDocument} Management. Any action to manage reprintable
 * documents in Openbravo should be done through this class.
 */
@ApplicationScoped
public class ReprintableDocumentManager {

  /**
   * Supported formats for a {@link ReprintableDocument}
   */
  public enum Format {
    XML, PDF;
  }

  @Inject
  @Any
  private Instance<ReprintableDocumentAttachHandler> handlers;

  /**
   * Creates a new {@link ReprintableDocument} and uploads its data as an attachment
   * 
   * @param documentData
   *          An InputStream with the document data. This method is in charge of closing it when
   *          finish its execution.
   * @param format
   *          The format of document
   * @param additionalProperties
   *          Extra properties to complete the {@link ReprintableDocument} data. It may include:
   *          "orderId": in case the document is linked to an order <br>
   *          "invoiceId": in case the document is linked to an invoice
   */
  public ReprintableDocument upload(InputStream documentData, Format format,
      Map<String, Object> additionalProperties) {
    ReprintableDocument document = OBProvider.getInstance().get(ReprintableDocument.class);
    AttachmentConfig config = AttachmentUtils.getAttachmentConfig(AttachmentType.RD);
    document.setName("reprintableDocument." + format.name().toLowerCase());
    document.setFormat(format.name());
    document.setAttachmentConfiguration(config);
    String orderId = (String) additionalProperties.get("orderId");
    if (orderId != null) {
      document.setOrder(OBDal.getInstance().getProxy(Order.class, orderId));
    }
    String invoiceId = (String) additionalProperties.get("invoiceId");
    if (invoiceId != null) {
      document.setInvoice(OBDal.getInstance().getProxy(Invoice.class, invoiceId));
    }
    OBDal.getInstance().save(document);

    ReprintableDocumentAttachHandler handler = getHandler(config.getAttachmentMethod().getValue());
    try (documentData) {
      handler.upload(document, documentData);
    } catch (Exception ex) {
      throw new OBException("Error uploading reprintable document", ex);
    }
    return document;
  }

  private ReprintableDocumentAttachHandler getHandler(String strAttachMethod) {
    return handlers.select(new ComponentProvider.Selector(strAttachMethod))
        .stream()
        .findFirst()
        .orElseThrow(() -> new OBException(OBMessageUtils.messageBD("MoreThanOneImplementation")));
  }
}
