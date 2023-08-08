package org.openbravo.client.application.attachment;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.ReprintableDocument;
import org.openbravo.model.common.order.Order;

public class OrderSourceDocument extends SourceDocument<Order> {

  public OrderSourceDocument(String id) {
    super(id);
  }

  @Override
  Order getBaseDocument() {
    return OBDal.getInstance().getProxy(Order.class, id);
  }

  @Override
  protected Entity getEntity() {
    return ModelProvider.getInstance().getEntity("Order");
  }

  @Override
  String getProperty() {
    return ReprintableDocument.PROPERTY_ORDER;
  }
}
