/*
 ************************************************************************************
 * Copyright (C) 2017-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Attribute;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.AttributeUse;
import org.openbravo.model.common.plm.AttributeValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

public class AttributesUtils {

  private static final Logger log = Logger.getLogger(AttributesUtils.class);

  public static AttributeSetInstance fetchAttributeSetValue(String attributeValue,
      String productId, String posTerminalOrganizationId) {
    AttributeSetInstance attrSetInst = null;
    String validatedAttributeSetInstanceDescription = "";
    Product product = OBDal.getInstance().get(Product.class, productId);
    // Ensure that product is configured to use attributes
    if (product.getAttributeSet() != null) {
      try {
        if (StringUtils.isNotEmpty(attributeValue) || StringUtils.isNotBlank(attributeValue)) {
          // Generate a valid description for given values
          validatedAttributeSetInstanceDescription = AttributesUtils
              .generateValidAttSetInstanceDescription(attributeValue, product);
          // Try to find an Att Set Intance Id with given description
          OBCriteria<AttributeSetInstance> attrSICrit = OBDal.getInstance().createCriteria(
              AttributeSetInstance.class);
          attrSICrit.add(Restrictions.eq(AttributeSetInstance.PROPERTY_DESCRIPTION,
              validatedAttributeSetInstanceDescription));
          attrSICrit.addOrderBy("id", false);
          List<AttributeSetInstance> attrSIList = attrSICrit.list();
          if (attrSIList.isEmpty() && attrSIList.size() == 0) {
            // Att Set instance Id not found -> Create New One
            attrSetInst = AttributesUtils.createAttributeSetValue(
                validatedAttributeSetInstanceDescription, product);
            return attrSetInst;
          } else if (attrSIList.size() == 1) {
            // Just one Att Set instance Id found -> Use it
            attrSetInst = attrSIList.get(0);
            return attrSetInst;
          } else {
            // Issue 37308: We have found several Att Set Instances with the same description.
            // Lets try to find one which have stock
            // inside the warehouses used by the store
            StringBuilder stDetailWhereClause = new StringBuilder();
            stDetailWhereClause.append(" as e WHERE ");
            stDetailWhereClause.append("e.attributeSetValue.id in ( ");
            stDetailWhereClause.append("  select id from AttributeSetInstance attseti ");
            stDetailWhereClause.append("  where attseti.description = :attsetdescription ");
            stDetailWhereClause.append(") AND ");
            stDetailWhereClause.append("e.quantityOnHand > 0 AND ");
            stDetailWhereClause.append("e.storageBin.warehouse.id in ( ");
            stDetailWhereClause.append("  select warehouse.id from OrganizationWarehouse orgwh ");
            stDetailWhereClause.append("  where orgwh.organization.id = :orgid ");
            stDetailWhereClause.append(") ");
            stDetailWhereClause.append("ORDER BY e.quantityOnHand desc, e.attributeSetValue.id ");
            OBQuery<StorageDetail> querySdetail = OBDal.getInstance().createQuery(
                StorageDetail.class, stDetailWhereClause.toString());
            querySdetail.setNamedParameter("attsetdescription",
                validatedAttributeSetInstanceDescription);
            querySdetail.setNamedParameter("orgid", posTerminalOrganizationId);
            List<StorageDetail> lstSDResults = querySdetail.list();
            if (lstSDResults.size() > 0) {
              // Pick the first one (query was ordered by qtyOnHand and Att Set instance id)
              attrSetInst = lstSDResults.get(0).getAttributeSetValue();
            } else {
              // There is no stock. Use the first one found in Att Set instance table
              attrSetInst = attrSIList.get(0);
            }
            return attrSetInst;
          }
        } else {
          // return null because given values are empty
          return attrSetInst;
        }
      } catch (Exception e) {
        throw new OBException(e.getMessage(), e);
      }
    } else {
      // Product is not configured to use attributes
      log.warn("Warning: Trying to fetch an Attribute Set instance for a product which is not configured to use attributes. ("
          + product.getIdentifier() + ")");
      return null;
    }
  }

  public static String generateValidAttSetInstanceDescription(
      String receivedAttSetInstanceDescription, String productId) {
    return generateValidAttSetInstanceDescription(receivedAttSetInstanceDescription, OBDal
        .getInstance().get(Product.class, productId));
  }

  public static String generateValidAttSetInstanceDescription(
      String receivedAttSetInstanceDescription, Product product) {
    if (product.getAttributeSet() != null) {
      return AttributesUtils.generateValidAttSetInstanceDescription(
          receivedAttSetInstanceDescription, product, product.getAttributeSet());
    } else {
      log.warn("generateValidAttSetInstanceDescription method has been called using a product ("
          + product.getIdentifier() + ") without attributeSet.");
      return "";
    }
  }

  private static String generateValidAttSetInstanceDescription(
      String receivedAttSetInstanceDescription, Product product, AttributeSet attSet) {
    String result = "";
    log.debug("Att Description received: " + receivedAttSetInstanceDescription);
    String[] receivedAttSetInstanceDescription_parts = receivedAttSetInstanceDescription.split("_");

    int currentPart = 0;
    int numberOfParts = receivedAttSetInstanceDescription_parts.length;
    log.debug("Number of parts of Att Description: " + numberOfParts);

    List<Attribute> lstAttributes = getInstanciableAndMandatoryAttributesUsedByAttributeSetSortedBySeqNoAsc(attSet);
    for (Attribute att : lstAttributes) {
      if (currentPart >= numberOfParts) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("OBPOS_ProductAttrValuesNotFound"), product.getIdentifier(),
            attSet.getIdentifier(), (currentPart + 1), numberOfParts), true);
      }
      if (att.isList()) {
        log.debug("Value for attribute (with restricted list of vaules) -" + att.getIdentifier()
            + "- of attSet -" + attSet.getIdentifier() + "- used by Product -"
            + product.getIdentifier() + "- is -"
            + receivedAttSetInstanceDescription_parts[currentPart] + "-");
        String valueToValidate = receivedAttSetInstanceDescription_parts[currentPart];
        if (isValidValueForAttribute(att, valueToValidate)) {
          result += receivedAttSetInstanceDescription_parts[currentPart] + "_";
        } else {
          throw new OBException(String.format(
              OBMessageUtils.messageBD("OBPOS_ProductAttrDefinedAsList"), product.getIdentifier(),
              valueToValidate), true);
        }
      } else {
        log.debug("Value for attribute (without valid list of vaules) -" + att.getIdentifier()
            + "- of attSet -" + attSet.getIdentifier() + "- used by Product -"
            + product.getIdentifier() + "- is -"
            + receivedAttSetInstanceDescription_parts[currentPart] + "-");
        result += receivedAttSetInstanceDescription_parts[currentPart] + "_";
      }
      log.debug("AttSetInstance description for product -" + product.getIdentifier()
          + "- after an iteration of attUse is -" + result + "-");
      currentPart += 1;
    }

    if (attSet.isLot()) {
      if (currentPart >= numberOfParts) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("OBPOS_ProductAttrValuesNotFound"), product.getIdentifier(),
            attSet.getIdentifier(), (currentPart + 1), numberOfParts), true);
      }
      log.debug("Att Set (" + attSet.getIdentifier() + ") uses lot. lot value is -"
          + receivedAttSetInstanceDescription_parts[currentPart] + "-");
      result += "L" + receivedAttSetInstanceDescription_parts[currentPart] + "_";
      currentPart += 1;
    }

    if (attSet.isSerialNo()) {
      if (currentPart >= numberOfParts) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("OBPOS_ProductAttrValuesNotFound"), product.getIdentifier(),
            attSet.getIdentifier(), (currentPart + 1), numberOfParts), true);
      }
      log.debug("Att Set (" + attSet.getIdentifier()
          + ") uses serialNo. It must be the first part ("
          + receivedAttSetInstanceDescription_parts[currentPart] + ")");
      result += "#" + receivedAttSetInstanceDescription_parts[currentPart] + "_";
      currentPart += 1;
    }

    if (attSet.isExpirationDate()) {
      if (currentPart >= numberOfParts) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("OBPOS_ProductAttrValuesNotFound"), product.getIdentifier(),
            attSet.getIdentifier(), (currentPart + 1), numberOfParts), true);
      }
      String receivedExpirationDateStrValue = receivedAttSetInstanceDescription_parts[currentPart];
      Date receivedExpirationDate;

      log.debug("Att Set (" + attSet.getIdentifier()
          + ") uses expiration date. Value received for expDate is -"
          + receivedExpirationDateStrValue + "-");

      final String dateFormatForDescription = (String) OBPropertiesProvider.getInstance()
          .getOpenbravoProperties().get("dateFormat.java");
      try {
        receivedExpirationDate = new SimpleDateFormat(dateFormatForDescription)
            .parse(receivedExpirationDateStrValue);
      } catch (Exception e) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("OBPOS_ProductAttrExpirationDateFormat"),
            product.getIdentifier(), dateFormatForDescription, receivedExpirationDateStrValue),
            true);
      }
      final String validatedExpirationDateStrValue = new SimpleDateFormat(dateFormatForDescription)
          .format(receivedExpirationDate);
      log.debug("Expiration date value for product -" + product.getIdentifier() + "- is -"
          + validatedExpirationDateStrValue + "-");
      result += validatedExpirationDateStrValue + "_";
      currentPart += 1;
    }

    if (StringUtils.isEmpty(result)) {
      throw new OBException(String.format(
          OBMessageUtils.messageBD("OBPOS_ProductAttributeNotDefined"), product.getIdentifier()),
          true);
    } else {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }

  private static AttributeSetInstance createAttributeSetValue(
      String receivedAttSetInstanceDescriptionWithSymbols, Product product) {
    if (product.getAttributeSet() != null) {
      try {
        String[] receivedAttSetInstanceDescriptionWithSymbols_parts = receivedAttSetInstanceDescriptionWithSymbols
            .split("_");

        int currentPart = 0;
        int numberOfParts = receivedAttSetInstanceDescriptionWithSymbols_parts.length;

        // Create Att Set Instance
        AttributeSetInstance newAttrSetInst = OBProvider.getInstance().get(
            AttributeSetInstance.class);
        newAttrSetInst.setAttributeSet(product.getAttributeSet());
        newAttrSetInst.setDescription(receivedAttSetInstanceDescriptionWithSymbols);
        OBDal.getInstance().save(newAttrSetInst);

        // If attSet uses other attributes, Att instance must be created
        List<Attribute> lstAttributes = getInstanciableAndMandatoryAttributesUsedByAttributeSetSortedBySeqNoAsc(product
            .getAttributeSet());
        if (lstAttributes != null && lstAttributes.size() > 0) {
          // We are using attributes -> Att Instance must be created for each attribute used
          for (Attribute att : lstAttributes) {
            String attValue = "";
            attValue = receivedAttSetInstanceDescriptionWithSymbols_parts[currentPart];
            final AttributeInstance attInstance = (AttributeInstance) OBProvider.getInstance().get(
                AttributeInstance.ENTITY_NAME);
            attInstance.setAttribute(att);
            attInstance.setSearchKey(attValue);
            attInstance.setAttributeSetValue(newAttrSetInst);
            OBDal.getInstance().save(attInstance);
            currentPart += 1;
          }
        }

        if (product.getAttributeSet().isLot()) {
          if (currentPart >= numberOfParts) {
            // manage error
            // Should not happen because it has been already managed by
            // generateValidAttSetInstanceDescription
          }
          String currentLotWithSymbol = receivedAttSetInstanceDescriptionWithSymbols_parts[currentPart];
          String currentLotWithOutSymbol = currentLotWithSymbol.substring(1);
          newAttrSetInst.setLotName(currentLotWithOutSymbol);
          currentPart += 1;
        }

        if (product.getAttributeSet().isSerialNo()) {
          if (currentPart >= numberOfParts) {
            // manage error
            // Should not happen because it has been already managed by
            // generateValidAttSetInstanceDescription
          }
          String currentSerialNoWithSymbol = receivedAttSetInstanceDescriptionWithSymbols_parts[currentPart];
          String currentSerialNoWithOutSymbol = currentSerialNoWithSymbol.substring(1);
          newAttrSetInst.setSerialNo(currentSerialNoWithOutSymbol);
          currentPart += 1;
        }

        if (product.getAttributeSet().isExpirationDate()) {
          if (currentPart >= numberOfParts) {
            // manage error
            // Should not happen because it has been already managed by
            // generateValidAttSetInstanceDescription
          }
          String receivedExpirationDateStrValue = receivedAttSetInstanceDescriptionWithSymbols_parts[currentPart];
          Date receivedExpirationDate;

          final String dateFormatForDescription = (String) OBPropertiesProvider.getInstance()
              .getOpenbravoProperties().get("dateFormat.java");
          try {
            receivedExpirationDate = new SimpleDateFormat(dateFormatForDescription)
                .parse(receivedExpirationDateStrValue);
          } catch (Exception e) {
            throw new OBException(String.format(
                OBMessageUtils.messageBD("OBPOS_ProductAttrExpirationDateFormat"),
                product.getIdentifier(), dateFormatForDescription, receivedExpirationDateStrValue),
                true);
          }

          OBDal.getInstance().flush();
          newAttrSetInst.setExpirationDate(receivedExpirationDate);
          currentPart += 1;
        }

        return newAttrSetInst;
      } catch (Exception e) {
        throw new OBException(e.getMessage(), e);
      }
    } else {
      log.warn("createAttributeSetValue method has been called using a product ("
          + product.getIdentifier() + ") without attributeSet.");
      return null;
    }
  }

  private static List<Attribute> getInstanciableAndMandatoryAttributesUsedByAttributeSetSortedBySeqNoAsc(
      AttributeSet attributeSet) {
    List<Attribute> lstAttributes = new ArrayList<Attribute>();
    try {
      StringBuilder hqlQueryString = new StringBuilder();
      hqlQueryString.append("attributeSet.id = :attSetId ");
      hqlQueryString.append("ORDER BY seqNo asc");
      OBQuery<AttributeUse> attUseQuery = OBDal.getInstance().createQuery(AttributeUse.class,
          hqlQueryString.toString());
      attUseQuery.setNamedParameter("attSetId", attributeSet.getId());
      List<AttributeUse> lstAttUse = attUseQuery.list();
      for (AttributeUse attUse : lstAttUse) {
        if (attUse.getAttribute().isInstanceAttribute() && attUse.getAttribute().isMandatory()) {
          lstAttributes.add(attUse.getAttribute());
        }
      }
    } catch (Exception e) {
      throw new OBException(e.getMessage(), e);
    }
    return lstAttributes;
  }

  private static boolean isValidValueForAttribute(Attribute att, String value) {
    if (!att.isList()) {
      log.warn("isValidValueForAttribute function is being called for attribute not marked as list -"
          + att.getIdentifier() + "-");
      return true;
    }
    StringBuilder hqlQueryString = new StringBuilder();
    hqlQueryString.append("attribute.id = :attId AND ");
    hqlQueryString.append("name = :nameValue ");
    OBQuery<AttributeValue> attValueQuery = OBDal.getInstance().createQuery(AttributeValue.class,
        hqlQueryString.toString());
    attValueQuery.setNamedParameter("attId", att.getId());
    attValueQuery.setNamedParameter("nameValue", value);
    List<AttributeValue> lstAttValues = attValueQuery.list();
    if (lstAttValues != null) {
      if (lstAttValues.size() > 0) {
        return true;
      }
    }
    return false;
  }

}