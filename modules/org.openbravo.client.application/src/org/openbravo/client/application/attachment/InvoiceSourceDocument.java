package org.openbravo.client.application.attachment;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.ReprintableDocument;
import org.openbravo.model.common.invoice.Invoice;

public class InvoiceSourceDocument extends SourceDocument<Invoice> {

  public InvoiceSourceDocument(String id) {
    super(id);
  }

  @Override
  Invoice getBaseDocument() {
    return OBDal.getInstance().getProxy(Invoice.class, id);
  }

  @Override
  protected Entity getEntity() {
    return ModelProvider.getInstance().getEntity("Invoice");
  }

  @Override
  String getProperty() {
    return ReprintableDocument.PROPERTY_INVOICE;
  }

}
