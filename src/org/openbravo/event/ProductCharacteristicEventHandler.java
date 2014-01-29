/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.CharacteristicSubsetValue;
import org.openbravo.model.common.plm.CharacteristicValue;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.model.common.plm.ProductCharacteristicConf;

public class ProductCharacteristicEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      ProductCharacteristic.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductCharacteristic prCh = (ProductCharacteristic) event.getTargetInstance();
    if (prCh.isVariant() && prCh.getProduct().isGeneric()) {
      if (!prCh.getProduct().getProductGenericProductList().isEmpty()) {
        throw new OBException(OBMessageUtils.messageBD("NewVariantChWithVariantsError"));
      }
      if (prCh.isDefinesPrice()) {
        // Check there is only 1.
        for (ProductCharacteristic prChAux : prCh.getProduct().getProductCharacteristicList()) {
          if (prChAux.isDefinesPrice() && !prChAux.getId().equals(prCh.getId())) {
            throw new OBException(OBMessageUtils.messageBD("DuplicateDefinesPrice"));
          }
        }
      }
      if (prCh.isDefinesImage()) {
        // Check there is only 1.
        for (ProductCharacteristic prChAux : prCh.getProduct().getProductCharacteristicList()) {
          if (prChAux.isDefinesImage() && !prChAux.getId().equals(prCh.getId())) {
            throw new OBException(OBMessageUtils.messageBD("DuplicateDefinesImage"));
          }
        }
      }
      final Entity prodCharEntity = ModelProvider.getInstance().getEntity(
          ProductCharacteristic.ENTITY_NAME);

      final Property charConfListProperty = prodCharEntity
          .getProperty(ProductCharacteristic.PROPERTY_PRODUCTCHARACTERISTICCONFLIST);
      @SuppressWarnings("unchecked")
      List<ProductCharacteristicConf> prChConfs = (List<ProductCharacteristicConf>) event
          .getCurrentState(charConfListProperty);
      Set<String[]> newChValues = getValuesToAdd(prCh);
      for (String[] strChValueId : newChValues) {
        prChConfs.add(getCharacteristicConf(prCh, strChValueId[0], strChValueId[1]));
      }
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final ProductCharacteristic prCh = (ProductCharacteristic) event.getTargetInstance();
    if (!prCh.isVariant() && prCh.getProduct().isGeneric()
        && !prCh.getProduct().getProductGenericProductList().isEmpty()) {
      throw new OBException(OBMessageUtils.messageBD("NewVariantChWithVariantsError"));
    }
    if (prCh.isVariant() && prCh.getProduct().isGeneric()) {
      final Entity prodCharEntity = ModelProvider.getInstance().getEntity(
          ProductCharacteristic.ENTITY_NAME);
      final Property variantProperty = prodCharEntity
          .getProperty(ProductCharacteristic.PROPERTY_VARIANT);
      boolean oldIsVariant = (Boolean) event.getPreviousState(variantProperty);

      if (!prCh.getProduct().getProductGenericProductList().isEmpty() && !oldIsVariant) {
        throw new OBException(OBMessageUtils.messageBD("NewVariantChWithVariantsError"));
      }
      if (prCh.isDefinesPrice()) {
        // Check there is only 1.
        for (ProductCharacteristic prChAux : prCh.getProduct().getProductCharacteristicList()) {
          if (prChAux.isDefinesPrice() && !prChAux.getId().equals(prCh.getId())) {
            throw new OBException(OBMessageUtils.messageBD("DuplicateDefinesPrice"));
          }
        }
      }
      if (prCh.isDefinesImage()) {
        // Check there is only 1.
        for (ProductCharacteristic prChAux : prCh.getProduct().getProductCharacteristicList()) {
          if (prChAux.isDefinesImage() && !prChAux.getId().equals(prCh.getId())) {
            throw new OBException(OBMessageUtils.messageBD("DuplicateDefinesImage"));
          }
        }
      }

      final Property charConfListProperty = prodCharEntity
          .getProperty(ProductCharacteristic.PROPERTY_PRODUCTCHARACTERISTICCONFLIST);
      @SuppressWarnings("unchecked")
      List<ProductCharacteristicConf> prChConfs = (List<ProductCharacteristicConf>) event
          .getCurrentState(charConfListProperty);

      final List<String> existingValues = new ArrayList<String>();
      for (ProductCharacteristicConf prChConf : prCh.getProductCharacteristicConfList()) {
        existingValues.add((String) DalUtil.getId(prChConf.getCharacteristicValue()));
      }
      Set<String[]> valuesToAdd = getValuesToAdd(prCh);
      for (String[] strNewValue : valuesToAdd) {
        if (existingValues.remove(strNewValue[0])) {
          OBCriteria<ProductCharacteristicConf> prChConfCrit = OBDal.getInstance().createCriteria(
              ProductCharacteristicConf.class);
          prChConfCrit.add(Restrictions.eq(
              ProductCharacteristicConf.PROPERTY_CHARACTERISTICOFPRODUCT, prCh));
          prChConfCrit.add(Restrictions.eq(ProductCharacteristicConf.PROPERTY_CHARACTERISTICVALUE,
              OBDal.getInstance().get(CharacteristicValue.class, strNewValue[0])));
          ProductCharacteristicConf prChConf = (ProductCharacteristicConf) prChConfCrit
              .uniqueResult();
          prChConf.setCode(strNewValue[1]);
          OBDal.getInstance().save(prChConf);
          continue;
        }
        prChConfs.add(getCharacteristicConf(prCh, strNewValue[0], strNewValue[1]));
      }
      // remove not needed
      if (!existingValues.isEmpty()) {
        for (String strChValueId : existingValues) {
          OBCriteria<ProductCharacteristicConf> prChConfCrit = OBDal.getInstance().createCriteria(
              ProductCharacteristicConf.class);
          prChConfCrit.add(Restrictions.eq(
              ProductCharacteristicConf.PROPERTY_CHARACTERISTICOFPRODUCT, prCh));
          prChConfCrit.add(Restrictions.eq(ProductCharacteristicConf.PROPERTY_CHARACTERISTICVALUE,
              OBDal.getInstance().get(CharacteristicValue.class, strChValueId)));
          ProductCharacteristicConf prChConf = (ProductCharacteristicConf) prChConfCrit
              .uniqueResult();

          prChConfs.remove(prChConf);
          OBDal.getInstance().remove(prChConf);
        }
      }
    }
  }

  private Set<String[]> getValuesToAdd(ProductCharacteristic prCh) {
    // If a subset is defined insert only values of it.
    Set<String[]> chValues = new HashSet<String[]>();
    if (prCh.getCharacteristicSubset() != null) {
      for (CharacteristicSubsetValue subsetValue : prCh.getCharacteristicSubset()
          .getCharacteristicSubsetValueList()) {
        String strCode = subsetValue.getCode();
        if (StringUtils.isBlank(strCode)) {
          strCode = subsetValue.getCharacteristicValue().getCode();
        }
        String[] strValues = { subsetValue.getCharacteristicValue().getId(), strCode };
        chValues.add(strValues);
      }
      return chValues;
    }
    // Add all not summary values.
    for (CharacteristicValue chValue : prCh.getCharacteristic().getCharacteristicValueList()) {
      if (!chValue.isSummaryLevel()) {
        String[] strValues = { chValue.getId(), chValue.getCode() };
        chValues.add(strValues);
      }
    }
    return chValues;
  }

  private ProductCharacteristicConf getCharacteristicConf(ProductCharacteristic prCh,
      String strCharacteristicValueId, String strCode) {
    ProductCharacteristicConf charConf = OBProvider.getInstance().get(
        ProductCharacteristicConf.class);
    charConf.setCharacteristicOfProduct(prCh);
    charConf.setOrganization(prCh.getOrganization());
    charConf.setCharacteristicValue((CharacteristicValue) OBDal.getInstance().getProxy(
        CharacteristicValue.ENTITY_NAME, strCharacteristicValueId));
    charConf.setCode(strCode);
    return charConf;
  }
}