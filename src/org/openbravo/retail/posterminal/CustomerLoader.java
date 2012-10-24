/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.service.json.JsonConstants;

public class CustomerLoader extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(CustomerLoader.class);

  private static final BigDecimal NEGATIVE_ONE = new BigDecimal(-1);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    Object jsonCustomer = jsonsent.get("customer");

    JSONArray array = null;
    if (jsonCustomer instanceof JSONObject) {
      array = new JSONArray();
      array.put(jsonCustomer);
    } else if (jsonCustomer instanceof String) {
      JSONObject obj = new JSONObject((String) jsonCustomer);
      array = new JSONArray();
      array.put(obj);
    } else if (jsonCustomer instanceof JSONArray) {
      array = (JSONArray) jsonCustomer;
    }

    JSONObject result = this.saveCustomer(array);
    return result;
  }

  public JSONObject saveCustomer(JSONArray jsonarray) throws JSONException {
    boolean error = false;
    Exception errorException = null;
    OBContext.setAdminMode(true);
    try {
      for (int i = 0; i < jsonarray.length(); i++) {
        JSONObject jsonCustomer = jsonarray.getJSONObject(i);
        JSONObject result = saveCustomer(jsonCustomer);
        if (!result.get(JsonConstants.RESPONSE_STATUS).equals(
            JsonConstants.RPCREQUEST_STATUS_SUCCESS)) {
          log.error("There was an error importing customer: " + jsonCustomer.toString());
          error = true;
        }
        if (i % 1 == 0) {
          OBDal.getInstance().flush();
        }
      }
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().getSession().clear();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      error = true;
      errorException = e;
    } finally {
      OBContext.restorePreviousMode();
    }
    JSONObject jsonResponse = new JSONObject();
    if (error) {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      final JSONObject jsonError = new JSONObject();
      jsonError.put("message", errorException.getMessage());
      jsonError.put("type", errorException.getClass().getName());
      jsonResponse.put(JsonConstants.RESPONSE_ERROR, jsonError);
    } else {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", "0");
    }
    return jsonResponse;
  }

  public JSONObject saveCustomer(JSONObject jsoncustomer) throws Exception {
    BusinessPartner customer = null;

    try {
      customer = getCustomer(jsoncustomer.getString("id"));
      if (customer.getId() == null) {
        customer = createBPartner(jsoncustomer);
      } else {
        customer = editBPartner(customer, jsoncustomer);
      }

      editLocation(customer, jsoncustomer);
      editBPartnerContact(customer, jsoncustomer);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      throw e;
    }

    final JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    jsonResponse.put("data", jsoncustomer);

    return jsonResponse;
  }

  protected BusinessPartner getCustomer(String id) {
    BusinessPartner customer = OBDal.getInstance().get(BusinessPartner.class, id);
    if (customer != null) {
      return customer;
    }
    return new BusinessPartner();
  }

  protected BusinessPartner createBPartner(JSONObject jsonCustomer) throws JSONException {
    BusinessPartner customer = OBProvider.getInstance().get(BusinessPartner.class);

    customer.setClient(OBDal.getInstance().get(Client.class, jsonCustomer.getString("client")));
    // BP org (required)
    if (jsonCustomer.has("organization") && !jsonCustomer.getString("organization").equals("null")) {
      customer.setOrganization(OBDal.getInstance().get(Organization.class,
          jsonCustomer.getString("organization")));
    } else {
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
    if (jsonCustomer.has("businessPartnerCategory")
        && !jsonCustomer.getString("businessPartnerCategory").equals("null")) {
      customer.setBusinessPartnerCategory(OBDal.getInstance().get(Category.class,
          jsonCustomer.getString("businessPartnerCategory")));
    } else {
      String errorMessage = "Business partner category is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    // BP search key (required)
    if (jsonCustomer.has("searchKey") && !jsonCustomer.getString("searchKey").equals("null")) {
      customer.setSearchKey(jsonCustomer.getString("searchKey"));
    } else {
      String errorMessage = "Business partner search key is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    // BP name (required)
    if (jsonCustomer.has("name") && !jsonCustomer.getString("name").equals("null")) {
      customer.setName(jsonCustomer.getString("name"));
    } else {
      String errorMessage = "Business partner name is a mandatory field to create a new customer from Web Pos";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    // BP description
    if (jsonCustomer.has("description") && !jsonCustomer.getString("description").equals("null")) {
      customer.setDescription(jsonCustomer.getString("description"));
    }
    // BP tax id
    if (jsonCustomer.has("taxId") && !jsonCustomer.getString("taxId").equals("null")) {
      customer.setTaxID(jsonCustomer.getString("taxId"));
    }
    // BP Tax category
    if (jsonCustomer.has("taxCategory") && !jsonCustomer.getString("taxCategory").equals("null")) {
      final org.openbravo.model.common.businesspartner.TaxCategory taxCat = OBDal.getInstance()
          .get(org.openbravo.model.common.businesspartner.TaxCategory.class,
              jsonCustomer.getString("taxCategory"));
      customer.setTaxCategory(taxCat);
    }

    // customer tab
    customer.setCustomer(true);
    customer.setCreditLimit(BigDecimal.ZERO);

    if (jsonCustomer.has("paymentMethod")
        && !jsonCustomer.getString("paymentMethod").equals("null")) {
      final FIN_PaymentMethod payMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          jsonCustomer.getString("paymentMethod"));
      customer.setPaymentMethod(payMethod);
    } else {
      String errorMessage = "Customer payment method is a mandatory field to create a new customer from Web Pos.  In order to set a default value for this field go to retail configuration section of the organization window";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }

    if (jsonCustomer.has("paymentTerms") && !jsonCustomer.getString("paymentTerms").equals("null")) {
      final PaymentTerm payTerms = OBDal.getInstance().get(PaymentTerm.class,
          jsonCustomer.getString("paymentTerms"));
      customer.setPaymentTerms(payTerms);
    } else {
      String errorMessage = "Customer payment terms is a mandatory field to create a new customer from Web Pos. In order to set a default value for this field go to retail configuration section of the organization window";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }

    if (jsonCustomer.has("invoiceTerms") && !jsonCustomer.getString("invoiceTerms").equals("null")) {
      customer.setInvoiceTerms(jsonCustomer.getString("invoiceTerms"));
    } else {
      String errorMessage = "Customer invoice terms is a mandatory field to create a new customer from Web Pos. In order to set a default value for this field go to retail configuration section of the organization window";
      log.error(errorMessage);
      throw new OBException(errorMessage, null);
    }
    customer.setNewOBObject(true);
    OBDal.getInstance().save(customer);
    return customer;
  }

  protected BusinessPartner editBPartner(BusinessPartner customer, JSONObject jsonCustomer)
      throws JSONException {
    customer.setCustomer(true);
    if (jsonCustomer.has("name") && !jsonCustomer.getString("name").equals("null")) {
      customer.setName(jsonCustomer.getString("name"));
    }
    if (jsonCustomer.has("description") && !jsonCustomer.getString("description").equals("null")) {
      customer.setDescription(jsonCustomer.getString("description"));
    }
    if (jsonCustomer.has("taxId") && !jsonCustomer.getString("taxId").equals("null")) {
      customer.setTaxID(jsonCustomer.getString("taxId"));
    }
    if (jsonCustomer.has("taxCategory") && !jsonCustomer.getString("taxCategory").equals("null")) {
      final org.openbravo.model.common.businesspartner.TaxCategory taxCat = OBDal.getInstance()
          .get(org.openbravo.model.common.businesspartner.TaxCategory.class,
              jsonCustomer.getString("taxCategory"));
      customer.setTaxCategory(taxCat);
    }
    if (jsonCustomer.has("paymentMethod")
        && !jsonCustomer.getString("paymentMethod").equals("null")) {
      final FIN_PaymentMethod payMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          jsonCustomer.getString("paymentMethod"));
      customer.setPaymentMethod(payMethod);
    }
    if (jsonCustomer.has("paymentTerms") && !jsonCustomer.getString("paymentTerms").equals("null")) {
      final PaymentTerm payTerms = OBDal.getInstance().get(PaymentTerm.class,
          jsonCustomer.getString("paymentTerms"));
      customer.setPaymentTerms(payTerms);
    }
    if (jsonCustomer.has("invoiceTerms") && !jsonCustomer.getString("invoiceTerms").equals("null")) {
      customer.setInvoiceTerms(jsonCustomer.getString("invoiceTerms"));
    }
    OBDal.getInstance().save(customer);
    return customer;
  }

  protected void editBPartnerContact(BusinessPartner customer, JSONObject jsonCustomer)
      throws JSONException {
    final OBCriteria<org.openbravo.model.ad.access.User> criteria = OBDal.getInstance()
        .createCriteria(org.openbravo.model.ad.access.User.class);
    criteria.add(Restrictions.eq("businessPartner.id", jsonCustomer.getString("id")));
    criteria.addOrderBy("name", false);
    final List<org.openbravo.model.ad.access.User> colUsers = criteria.list();
    if (colUsers.size() > 0) {
      // Contact exist > modify it
      if (jsonCustomer.has("email")) {
        colUsers.get(0).setEmail(jsonCustomer.getString("email"));
      }
      if (jsonCustomer.has("phone")) {
        colUsers.get(0).setPhone(jsonCustomer.getString("phone"));
      }
      OBDal.getInstance().save(colUsers.get(0));
    } else {
      // Contact doesn't exists > create it - create user linked to BP
      final org.openbravo.model.ad.access.User usr = OBProvider.getInstance().get(
          org.openbravo.model.ad.access.User.class);

      usr.setOrganization(customer.getOrganization());
      usr.setName(jsonCustomer.getString("name"));
      usr.setUsername(jsonCustomer.getString("name").trim());
      usr.setFirstName(jsonCustomer.getString("name").trim());

      if (jsonCustomer.has("email")) {
        usr.setEmail(jsonCustomer.getString("email"));
      }
      if (jsonCustomer.has("phone")) {
        usr.setPhone(jsonCustomer.getString("phone"));
      }
      usr.setBusinessPartner(customer);
    }
  }

  protected void editLocation(BusinessPartner customer, JSONObject jsonCustomer)
      throws JSONException {
    final OBCriteria<Location> criteria = OBDal.getInstance().createCriteria(Location.class);
    criteria.add(Restrictions.eq("businessPartner.id", jsonCustomer.getString("id")));
    criteria.addOrderBy("name", false);
    final List<Location> colLocations = criteria.list();
    if (colLocations != null && colLocations.size() > 0) {
      // location exist > modify it
      final org.openbravo.model.common.geography.Location rootLocation = colLocations.get(0)
          .getLocationAddress();
      if (jsonCustomer.has("locName")) {
        rootLocation.setAddressLine1(jsonCustomer.getString("locName"));
      }
      if (jsonCustomer.has("postalcode")) {
        rootLocation.setPostalCode(jsonCustomer.getString("postalcode"));
      }
      if (jsonCustomer.has("city")) {
        rootLocation.setCityName(jsonCustomer.getString("city"));
      }
      OBDal.getInstance().save(rootLocation);
    } else {
      // location not exists > create location and bplocation
      final org.openbravo.model.common.geography.Location rootLocation = OBProvider.getInstance()
          .get(org.openbravo.model.common.geography.Location.class);

      rootLocation.setOrganization(customer.getOrganization());

      if (jsonCustomer.has("locName")) {
        rootLocation.setAddressLine1(jsonCustomer.getString("locName"));
      }
      if (jsonCustomer.has("postalcode")) {
        rootLocation.setPostalCode(jsonCustomer.getString("postalcode"));
      }
      if (jsonCustomer.has("city")) {
        rootLocation.setCityName(jsonCustomer.getString("city"));
      }
      if (jsonCustomer.has("country")) {
        rootLocation.setCountry(OBDal.getInstance().get(Country.class,
            jsonCustomer.getString("country")));
      }

      OBDal.getInstance().save(rootLocation);

      Location newLocation = OBProvider.getInstance().get(Location.class);
      if (jsonCustomer.has("locId")) {
        newLocation.setId(jsonCustomer.getString("locId"));
      } else {
        String errorMessage = "Business partner Location ID is a mandatory field to create a new customer from Web Pos";
        log.error(errorMessage);
        throw new OBException(errorMessage, null);
      }
      if (jsonCustomer.has("locName")) {
        newLocation.setName(jsonCustomer.getString("locName"));
      }

      newLocation.setOrganization(customer.getOrganization());

      newLocation.setBusinessPartner(customer);
      newLocation.setLocationAddress(rootLocation);
      newLocation.setNewOBObject(true);
      OBDal.getInstance().save(newLocation);
    }
  }
}
