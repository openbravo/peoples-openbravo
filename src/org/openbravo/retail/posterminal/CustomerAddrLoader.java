/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.geography.Country;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "BusinessPartnerLocation")
public class CustomerAddrLoader extends POSDataSynchronizationProcess {

  private static final Logger log = Logger.getLogger(CustomerAddrLoader.class);

  @Override
  public JSONObject saveRecord(JSONObject jsonCustomerAddr) throws Exception {
    OBContext.setAdminMode(false);
    try {
      Location location = null;

      BusinessPartner customer = OBDal.getInstance().get(BusinessPartner.class,
          jsonCustomerAddr.getString("bpartner"));
      location = getCustomerAddress(jsonCustomerAddr.getString("id"));

      if (location.getId() == null) {
        location = createBPartnerAddr(customer, jsonCustomerAddr);
      } else {
        location = editBPartnerAddr(customer, location, jsonCustomerAddr);
      }

      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
    final JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");

    return jsonResponse;
  }

  protected Location getCustomerAddress(String id) {
    Location location = OBDal.getInstance().get(Location.class, id);
    if (location != null) {
      return location;
    }
    return new Location();
  }

  protected Location createBPartnerAddr(BusinessPartner customer, JSONObject jsonCustomerAddr)
      throws JSONException {
    try {
      Entity locationEntity = ModelProvider.getInstance().getEntity(Location.class);
      Entity baseLocationEntity = ModelProvider.getInstance().getEntity(
          org.openbravo.model.common.geography.Location.class);
      final org.openbravo.model.common.geography.Location rootLocation = OBProvider.getInstance()
          .get(org.openbravo.model.common.geography.Location.class);

      JSONPropertyToEntity.fillBobFromJSON(baseLocationEntity, rootLocation, jsonCustomerAddr);

      if (jsonCustomerAddr.has("name") && jsonCustomerAddr.getString("name") != null) {
        if (jsonCustomerAddr.getString("name").equals("")) {
          rootLocation.setAddressLine1(null);
        } else {
          rootLocation.setAddressLine1(jsonCustomerAddr.getString("name"));
        }
      }

      if (jsonCustomerAddr.has("countryId")) {
        rootLocation.setCountry(OBDal.getInstance().get(Country.class,
            jsonCustomerAddr.getString("countryId")));
      } else {
        String errorMessage = "Country ID is a mandatory field to create a new customer address from Web Pos";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }

      rootLocation.setPostalCode(jsonCustomerAddr.getString("postalCode"));
      rootLocation.setCityName(jsonCustomerAddr.getString("cityName"));
      OBDal.getInstance().save(rootLocation);

      Location newLocation = OBProvider.getInstance().get(Location.class);

      JSONPropertyToEntity.fillBobFromJSON(locationEntity, newLocation, jsonCustomerAddr);

      if (jsonCustomerAddr.has("id")) {
        newLocation.setId(jsonCustomerAddr.getString("id"));
      } else {
        String errorMessage = "Business partner Location ID is a mandatory field to create a new customer address from Web Pos";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }
      if (jsonCustomerAddr.has("name") && jsonCustomerAddr.getString("name") != null) {
        if (!jsonCustomerAddr.getString("name").equals("")) {
          newLocation.setName(jsonCustomerAddr.getString("name"));
        } else {
          String posibleName = jsonCustomerAddr.getString("customerName")
              + jsonCustomerAddr.getString("id").trim();
          if (posibleName.length() > 59) {
            posibleName = posibleName.substring(0, 59);
          }
          newLocation.setName(posibleName);
        }
      } else {
        String errorMessage = "Business partner Location Name is a mandatory field to create a new customer address from Web Pos";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }
      // don't set phone of location, the phone is set in contact
      newLocation.setPhone(null);

      newLocation.setBusinessPartner(customer);
      newLocation.setLocationAddress(rootLocation);
      newLocation.setNewOBObject(true);
      OBDal.getInstance().save(newLocation);

    } catch (final Exception e) {
      log.error("Exception while creating BPartner Address", e);
    }

    return null;
  }

  protected Location editBPartnerAddr(BusinessPartner customer, Location location,
      JSONObject jsonCustomerAddr) throws JSONException {
    try {
      if (location != null) {
        final org.openbravo.model.common.geography.Location rootLocation = location
            .getLocationAddress();

        if (jsonCustomerAddr.has("name") && jsonCustomerAddr.getString("name") != null) {
          if (jsonCustomerAddr.getString("name").equals("")) {
            rootLocation.setAddressLine1(null);
          } else {
            rootLocation.setAddressLine1(jsonCustomerAddr.getString("name"));
          }

        }
        rootLocation.setPostalCode(jsonCustomerAddr.getString("postalCode"));
        rootLocation.setCityName(jsonCustomerAddr.getString("cityName"));
        OBDal.getInstance().save(rootLocation);
      }
    } catch (final Exception e) {
      log.error("Exception while updating BPartner Address", e);
    }
    return null;
  }

  public static String getErrorMessage(Exception e) {
    StringWriter sb = new StringWriter();
    e.printStackTrace(new PrintWriter(sb));
    return sb.toString();
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.customers";
  }
}
