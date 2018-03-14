/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.mobile.core.process.OutDatedDataChangeException;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.geography.Country;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "BusinessPartner")
public class CustomerLoader extends POSDataSynchronizationProcess implements
    DataSynchronizationImportProcess {

  private static final Logger log = Logger.getLogger(CustomerLoader.class);

  @Inject
  @Any
  private Instance<CustomerLoaderHook> customerCreations;

  @Inject
  @Any
  private Instance<CustomerAddrCreationHook> customerAddrCreations;

  protected String getImportQualifier() {
    return "BusinessPartner";
  }

  public JSONObject saveRecord(JSONObject jsoncustomer) throws Exception {
    BusinessPartner customer = null;
    User user = null;
    OBContext.setAdminMode(false);
    try {
      customer = getCustomer(jsoncustomer.getString("id"));
      if (customer.getId() == null) {
        customer = createBPartner(jsoncustomer);
      } else {
        final Date loaded = OBMOBCUtils.calculateClientDatetime(jsoncustomer.getString("loaded"),
            Long.parseLong(jsoncustomer.getString("timezoneOffset")));
        if (jsoncustomer.has("contactId")) {
          user = OBDal.getInstance().get(User.class, jsoncustomer.getString("contactId"));
        }

        if (!(loaded.compareTo(customer.getUpdated()) >= 0)
            || !(user != null && (loaded.compareTo(user.getUpdated()) >= 0))) {
          throw new OutDatedDataChangeException(Utility.messageBD(new DalConnectionProvider(false),
              "OBPOS_outdatedbp", OBContext.getOBContext().getLanguage().getLanguage()));
        }

        customer = editBPartner(customer, jsoncustomer);
      }

      // Call all customerCreations injected.
      executeHooks(customerCreations, jsoncustomer, customer);

      editLocation(customer, jsoncustomer);
      editBPartnerContact(customer, jsoncustomer);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
    final JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");

    return jsonResponse;
  }

  private BusinessPartner getCustomer(String id) {
    BusinessPartner customer = OBDal.getInstance().get(BusinessPartner.class, id);
    if (customer != null) {
      return customer;
    }
    return new BusinessPartner();
  }

  private BusinessPartner createBPartner(JSONObject jsonCustomer) throws JSONException {
    BusinessPartner customer = OBProvider.getInstance().get(BusinessPartner.class);
    Entity BusinessPartnerEntity = ModelProvider.getInstance().getEntity(BusinessPartner.class);
    JSONPropertyToEntity.fillBobFromJSON(BusinessPartnerEntity, customer, jsonCustomer);

    // customer.setClient(OBDal.getInstance().get(Client.class, jsonCustomer.getString("client")));
    // BP org (required)
    if (!jsonCustomer.has("organization") || "null".equals(jsonCustomer.getString("organization"))) {
      String errorMessage = "Business partner organization is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    // BP id (required)
    if (jsonCustomer.has("id") && !jsonCustomer.getString("id").equals("null")) {
      customer.setId(jsonCustomer.getString("id"));
    } else {
      String errorMessage = "Business partner id is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    // BP category (required)
    if (!jsonCustomer.has("businessPartnerCategory")
        || "null".equals(jsonCustomer.getString("businessPartnerCategory"))) {
      String errorMessage = "Business partner category is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    // BP search key (required)
    if (!jsonCustomer.has("searchKey") || "null".equals(jsonCustomer.getString("searchKey"))) {
      String errorMessage = "Business partner search key is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    } else {
      String possibleSK = jsonCustomer.getString("searchKey").trim(), finalSK = null, searchKeyValue = StringUtils
          .substring(possibleSK, 0, 35);

      for (int i = 0; i < 10000; i++) {
        final OBCriteria<BusinessPartner> bpCriteria = OBDal.getInstance().createCriteria(
            BusinessPartner.class);
        bpCriteria.setFilterOnActive(false);
        bpCriteria.setFilterOnReadableOrganization(false);
        bpCriteria.add(Restrictions.eq("searchKey", possibleSK));
        bpCriteria.setMaxResults(1);
        if (bpCriteria.count() > 0) {
          // SK exist -> make it unique
          possibleSK = searchKeyValue + "_" + String.format("%04d", new Random().nextInt(10000));
        } else {
          // we can use this SK
          finalSK = possibleSK;
          break;
        }
      }
      if (finalSK == null) {
        String errorMessage = "Business partner search key already exists in system";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }
      customer.setSearchKey(finalSK);
    }
    // BP name (required)
    if (!jsonCustomer.has("name") || "null".equals(jsonCustomer.getString("name"))) {
      String errorMessage = "Business partner name is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }

    // customer tab
    customer.setCustomer(true);
    customer.setCreditLimit(BigDecimal.ZERO);

    customer.setNewOBObject(true);
    OBDal.getInstance().save(customer);
    return customer;
  }

  private BusinessPartner editBPartner(BusinessPartner customer, JSONObject jsonCustomer)
      throws JSONException {
    String previousSK = customer.getSearchKey();
    BigDecimal previousCL = customer.getCreditLimit();
    Entity BusinessPartnerEntity = ModelProvider.getInstance().getEntity(BusinessPartner.class);
    JSONPropertyToEntity.fillBobFromJSON(BusinessPartnerEntity, customer, jsonCustomer);

    // Don't change SK when BP is modified
    customer.setSearchKey(previousSK);
    // customer tab
    customer.setCustomer(true);
    // security
    customer.setCreditLimit(previousCL);

    // Fixed birth date issue(-1 day) when converted to UTC from client time zone
    if (jsonCustomer.has("birthDay") && jsonCustomer.get("birthDay") != null
        && !jsonCustomer.getString("birthDay").isEmpty()) {
      final long timezoneOffset;
      if (jsonCustomer.has("timezoneOffset")) {
        timezoneOffset = (long) Double.parseDouble(jsonCustomer.getString("timezoneOffset"));
      } else {
        // Using the current timezoneOffset
        timezoneOffset = -((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar
            .getInstance().get(Calendar.DST_OFFSET)) / (60 * 1000));
      }

      customer.setBirthDay(OBMOBCUtils.calculateServerDate((String) jsonCustomer.get("birthDay"),
          timezoneOffset));
    }

    OBDal.getInstance().save(customer);
    return customer;
  }

  private void editBPartnerContact(BusinessPartner customer, JSONObject jsonCustomer)
      throws JSONException {
    Entity userEntity = ModelProvider.getInstance().getEntity(
        org.openbravo.model.ad.access.User.class);
    final org.openbravo.model.ad.access.User user = OBDal.getInstance().get(
        org.openbravo.model.ad.access.User.class, jsonCustomer.getString("contactId"));
    if (user != null) {

      JSONPropertyToEntity.fillBobFromJSON(userEntity, user, jsonCustomer);
      String name = jsonCustomer.getString("name");
      if (name.length() > 60) {
        name = name.substring(0, 60);
      }
      user.setName(name);

      // Contact exist > modify it. The username is not modified
      OBDal.getInstance().save(user);
    } else {
      // Contact doesn't exists > create it - create user linked to BP

      // create the user
      final org.openbravo.model.ad.access.User usr = OBProvider.getInstance().get(
          org.openbravo.model.ad.access.User.class);
      JSONPropertyToEntity.fillBobFromJSON(userEntity, usr, jsonCustomer);

      if (jsonCustomer.has("contactId")) {
        usr.setId(jsonCustomer.getString("contactId"));
      } else {
        String errorMessage = "Business partner user ID is a mandatory field to create a new customer from Web Pos";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }

      String name = StringUtils.substring(jsonCustomer.getString("name").trim(), 0, 60), possibleUsername = name, finalUsername = null, userName = StringUtils
          .substring(name, 0, 55);

      for (int i = 0; i < 10000; i++) {
        final OBCriteria<org.openbravo.model.ad.access.User> userCriteria = OBDal.getInstance()
            .createCriteria(org.openbravo.model.ad.access.User.class);
        userCriteria.add(Restrictions.eq("username", possibleUsername));
        userCriteria.setFilterOnReadableClients(false);
        userCriteria.setFilterOnReadableOrganization(false);
        userCriteria.setFilterOnActive(false);
        userCriteria.setMaxResults(1);
        if (userCriteria.count() > 0) {
          // username exist -> make it unique
          possibleUsername = userName + "_" + String.format("%04d", new Random().nextInt(10000));
        } else {
          // we can use this username
          finalUsername = possibleUsername;
          break;
        }
      }
      if (finalUsername == null) {
        String errorMessage = "User username already exists in system";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }

      usr.setUsername(finalUsername);
      usr.setName(name);
      usr.setBusinessPartner(customer);
      usr.setNewOBObject(true);
      OBDal.getInstance().save(usr);
    }
  }

  private void editLocation(BusinessPartner customer, JSONObject jsonCustomer) throws Exception {
    if (jsonCustomer.has("locId")) {
      final Location location = OBDal.getInstance().get(Location.class,
          jsonCustomer.getString("locId"));
      if (location == null) {
        // location doesn't exist > create location(s) and bplocation(s)
        final org.openbravo.model.common.geography.Location rootLocation = OBProvider.getInstance()
            .get(org.openbravo.model.common.geography.Location.class);
        final org.openbravo.model.common.geography.Location rootShippingLocation = OBProvider
            .getInstance().get(org.openbravo.model.common.geography.Location.class);

        if (!jsonCustomer.has("useSameAddrForShipAndInv")
            || (jsonCustomer.has("useSameAddrForShipAndInv") && jsonCustomer
                .getBoolean("useSameAddrForShipAndInv"))) {
          createBPLoc(rootLocation, customer, jsonCustomer, true, true);
        } else {
          createBPLoc(rootLocation, customer, jsonCustomer, true, false);
          createBPLoc(rootShippingLocation, customer, jsonCustomer, false, true);
        }
      }
    }
  }

  private void createBPLoc(org.openbravo.model.common.geography.Location location,
      BusinessPartner customer, JSONObject jsonCustomer, Boolean isInvoicing, Boolean isShipping)
      throws Exception {
    Entity baseLocationEntity = ModelProvider.getInstance().getEntity(
        org.openbravo.model.common.geography.Location.class);
    Entity locationEntity = ModelProvider.getInstance().getEntity(Location.class);
    // Field mapping:
    String locName = (isInvoicing ? "locName" : "shipLocName");
    String locId = (isInvoicing ? "locId" : "shipLocId");
    String postalCode = (isInvoicing ? "postalCode" : "shipPostalCode");
    String cityName = (isInvoicing ? "cityName" : "shipCityName");
    String country = (isInvoicing ? "countryId" : "shipCountryId");

    JSONPropertyToEntity.fillBobFromJSON(baseLocationEntity, location, jsonCustomer);

    if (jsonCustomer.has(locName) && jsonCustomer.getString(locName) != null
        && !jsonCustomer.getString(locName).equals("")) {
      location.setAddressLine1(jsonCustomer.getString(locName));
    }

    if (jsonCustomer.has(postalCode) && jsonCustomer.getString(postalCode) != null
        && !jsonCustomer.getString(postalCode).equals("")) {
      location.setPostalCode(jsonCustomer.getString(postalCode));
    }

    if (jsonCustomer.has(cityName) && jsonCustomer.getString(cityName) != null
        && !jsonCustomer.getString(cityName).equals("")) {
      location.setCityName(jsonCustomer.getString(cityName));
    }

    location.setCountry(OBDal.getInstance().get(Country.class, jsonCustomer.getString(country)));

    OBDal.getInstance().save(location);

    Location newLocation = OBProvider.getInstance().get(Location.class);

    JSONPropertyToEntity.fillBobFromJSON(locationEntity, newLocation, jsonCustomer);

    if (jsonCustomer.has(locId)) {
      newLocation.setId(jsonCustomer.getString(locId));
    } else {
      String errorMessage = "Business partner Location ID is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    if (jsonCustomer.has(locName) && jsonCustomer.getString(locName) != null
        && !jsonCustomer.getString(locName).equals("")) {
      newLocation.setName(jsonCustomer.getString(locName));
    } else {
      newLocation.setName(jsonCustomer.getString("searchKey"));
    }

    // don't set phone of location, the phone is set in contact
    newLocation.setPhone(null);

    newLocation.setInvoiceToAddress(isInvoicing);
    newLocation.setShipToAddress(isShipping);

    newLocation.setBusinessPartner(customer);
    newLocation.setLocationAddress(location);
    newLocation.setNewOBObject(true);
    OBDal.getInstance().save(newLocation);

    executeAddrHooks(customerAddrCreations, jsonCustomer, customer, newLocation);

  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.customers";
  }

  private void executeHooks(Instance<CustomerLoaderHook> hooks, JSONObject jsonCustomer,
      BusinessPartner customer) throws Exception {
    for (Iterator<CustomerLoaderHook> procIter = hooks.iterator(); procIter.hasNext();) {
      CustomerLoaderHook proc = procIter.next();
      proc.exec(jsonCustomer, customer);
    }
  }

  private void executeAddrHooks(Instance<CustomerAddrCreationHook> hooks, JSONObject jsonCustomer,
      BusinessPartner customer, Location location) throws Exception {
    for (Iterator<CustomerAddrCreationHook> procIter = hooks.iterator(); procIter.hasNext();) {
      CustomerAddrCreationHook proc = procIter.next();
      proc.exec(jsonCustomer, customer, location);
    }
  }
}