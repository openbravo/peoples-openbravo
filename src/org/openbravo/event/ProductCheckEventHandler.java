package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.core2.UserInputValue;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.ProductCheck;
import org.openbravo.model.common.enterprise.ReturnBin;

public class ProductCheckEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ProductCheck.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    ProductCheck prodChk = (ProductCheck) event.getTargetInstance();

    final Entity productCheckEntity = ModelProvider.getInstance()
        .getEntity(ProductCheck.ENTITY_NAME);
    event.setCurrentState(productCheckEntity.getProperty(ProductCheck.PROPERTY_REQUIREPRODUCTCHECK),
        requireProductCheck(prodChk));
  }

  private boolean requireProductCheck(ProductCheck prodChk) {
    ReturnBin returnBin = null;
    if (prodChk.getObc2UserInputValue() != null && prodChk.getNewStorageBin() != null) {
      returnBin = getReturnStorageBin(prodChk.getObc2UserInputValue());
      if (returnBin == null) {
        returnBin = getDefaultReturnBin(prodChk.getNewStorageBin());
      }
    }
    return returnBin != null ? returnBin.isRequireProductCheck() : false;
  }

  private ReturnBin getReturnStorageBin(UserInputValue reason) {
    final OBCriteria<ReturnBin> returnBinCriteria = OBDal.getInstance()
        .createCriteria(ReturnBin.class);
    returnBinCriteria.add(Restrictions.eq(ReturnBin.PROPERTY_OBC2USERINPUTVALUE, reason));
    returnBinCriteria.setFilterOnReadableOrganization(false);
    returnBinCriteria.setMaxResults(1);
    return (ReturnBin) returnBinCriteria.uniqueResult();
  }

  private ReturnBin getDefaultReturnBin(Locator locator) {
    final OBCriteria<ReturnBin> defaultReturnBinCriteria = OBDal.getInstance()
        .createCriteria(ReturnBin.class);
    defaultReturnBinCriteria.add(Restrictions.isNull(ReturnBin.PROPERTY_OBC2USERINPUTVALUE));
    defaultReturnBinCriteria.add(Restrictions.eq(ReturnBin.PROPERTY_STORAGEBIN, locator));
    defaultReturnBinCriteria.setFilterOnReadableOrganization(false);
    defaultReturnBinCriteria.setMaxResults(1);
    return (ReturnBin) defaultReturnBinCriteria.uniqueResult();
  }

}
