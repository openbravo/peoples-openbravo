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
import java.sql.SQLException;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.service.json.JsonConstants;

public class CustomerAddrLoader extends JSONProcessSimple {

    private static final Logger log = Logger
	    .getLogger(CustomerAddrLoader.class);

    @Override
    public JSONObject exec(JSONObject jsonsent) throws JSONException,
	    ServletException {

	Object jsonCustomerAddr = jsonsent.get("customerAddr");
	JSONArray array = null;
	JSONObject result = new JSONObject();
	if (jsonCustomerAddr instanceof JSONObject) {
	    array = new JSONArray();
	    array.put(jsonCustomerAddr);
	} else if (jsonCustomerAddr instanceof String) {
	    JSONObject obj = new JSONObject((String) jsonCustomerAddr);
	    array = new JSONArray();
	    array.put(obj);
	} else if (jsonCustomerAddr instanceof JSONArray) {
	    array = (JSONArray) jsonCustomerAddr;
	}
	result = this.saveCustomerAddr(array, jsonsent.getString("terminalId"));
	return result;
    }

    public JSONObject saveCustomerAddr(JSONArray jsonarray, Object terminalId)
	    throws JSONException {
	boolean error = false;
	OBContext.setAdminMode(false);
	try {
	    if (RequestContext.get().getSessionAttribute(
		    "customerTerminalId|" + terminalId) == null) {
		RequestContext.get().setSessionAttribute(
			"customerTerminalId|" + terminalId, true);
		for (int i = 0; i < jsonarray.length(); i++) {
		    JSONObject jsonCustomerAddr = jsonarray.getJSONObject(i);
		    String posTerminalId = jsonCustomerAddr
			    .getString("posTerminal");
		    try {
			JSONObject result = saveCustomerAddr(jsonCustomerAddr);
			if (!result.get(JsonConstants.RESPONSE_STATUS).equals(
				JsonConstants.RPCREQUEST_STATUS_SUCCESS)) {
			    log.error("There was an error importing order: "
				    + jsonCustomerAddr.toString());
			    error = true;
			}
			if (i % 1 == 0) {
			    OBDal.getInstance().flush();
			    OBDal.getInstance().getConnection().commit();
			    OBDal.getInstance().getSession().clear();
			}
		    } catch (Exception e) {
			// Creation of the customer failed. We will now store
			// the customer in the import errors
			// table
			log.error(
				"An error happened when processing a customer: ",
				e);
			OBDal.getInstance().rollbackAndClose();

			OBPOSErrors errorEntry = OBProvider.getInstance().get(
				OBPOSErrors.class);
			errorEntry.setError(getErrorMessage(e));
			errorEntry.setOrderstatus("N");
			errorEntry.setJsoninfo(jsonCustomerAddr.toString());
			errorEntry.setTypeofdata("BP");
			errorEntry.setObposApplications(OBDal.getInstance()
				.get(OBPOSApplications.class, posTerminalId));
			OBDal.getInstance().save(errorEntry);
			OBDal.getInstance().flush();

			log.error("Error while loading customer", e);
			try {
			    OBDal.getInstance().getConnection().commit();
			} catch (SQLException e1) {
			    // this won't happen
			}
		    }
		}
	    }

	} finally {
	    RequestContext.get().removeSessionAttribute(
		    "customerTerminalId|" + terminalId);
	    OBContext.restorePreviousMode();
	}
	JSONObject jsonResponse = new JSONObject();
	if (error) {
	    jsonResponse.put(JsonConstants.RESPONSE_STATUS,
		    JsonConstants.RPCREQUEST_STATUS_FAILURE);
	    jsonResponse.put("result", "0");
	} else {
	    jsonResponse.put(JsonConstants.RESPONSE_STATUS,
		    JsonConstants.RPCREQUEST_STATUS_SUCCESS);
	    jsonResponse.put("result", "0");
	}
	return jsonResponse;
    }

    public JSONObject saveCustomerAddr(JSONObject jsonCustomerAddr)
	    throws Exception {
	Location location = null;

	BusinessPartner customer = OBDal.getInstance().get(
		BusinessPartner.class, jsonCustomerAddr.getString("bpartner"));
	location = getCustomerAddress(jsonCustomerAddr.getString("id"));

	if (location.getId() == null) {
	    location = createBPartnerAddr(customer, jsonCustomerAddr);
	} else {
	    location = editBPartnerAddr(customer, location, jsonCustomerAddr);
	}

	OBDal.getInstance().flush();

	final JSONObject jsonResponse = new JSONObject();
	jsonResponse.put(JsonConstants.RESPONSE_STATUS,
		JsonConstants.RPCREQUEST_STATUS_SUCCESS);
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

    protected Location createBPartnerAddr(BusinessPartner customer,
	    JSONObject jsonCustomerAddr) throws JSONException {
	try {
	    Entity locationEntity = ModelProvider.getInstance().getEntity(
		    Location.class);
	    Entity baseLocationEntity = ModelProvider.getInstance().getEntity(
		    org.openbravo.model.common.geography.Location.class);
	    final org.openbravo.model.common.geography.Location rootLocation = OBProvider
		    .getInstance()
		    .get(org.openbravo.model.common.geography.Location.class);

	    JSONPropertyToEntity.fillBobFromJSON(baseLocationEntity,
		    rootLocation, jsonCustomerAddr);

	    if (jsonCustomerAddr.has("name")
		    && jsonCustomerAddr.getString("name") != null
		    && !jsonCustomerAddr.getString("name").equals("")) {
		rootLocation
			.setAddressLine1(jsonCustomerAddr.getString("name"));
	    }
	    rootLocation
		    .setPostalCode(jsonCustomerAddr.getString("postalCode"));
	    rootLocation.setCityName(jsonCustomerAddr.getString("cityName"));
	    OBDal.getInstance().save(rootLocation);

	    Location newLocation = OBProvider.getInstance().get(Location.class);

	    JSONPropertyToEntity.fillBobFromJSON(locationEntity, newLocation,
		    jsonCustomerAddr);

	    if (jsonCustomerAddr.has("id")) {
		newLocation.setId(jsonCustomerAddr.getString("id"));
	    } else {
		String errorMessage = "Business partner Location ID is a mandatory field to create a new customer address from Web Pos";
		log.error(errorMessage);
		throw new OBException(errorMessage, null);
	    }
	    if (jsonCustomerAddr.has("name")
		    && jsonCustomerAddr.getString("name") != null
		    && !jsonCustomerAddr.getString("name").equals("")) {
		newLocation.setName(jsonCustomerAddr.getString("name"));
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

    protected Location editBPartnerAddr(BusinessPartner customer,
	    Location location, JSONObject jsonCustomerAddr)
	    throws JSONException {
	try {
	    if (location != null) {
		final org.openbravo.model.common.geography.Location rootLocation = location
			.getLocationAddress();

		if (jsonCustomerAddr.has("name")
			&& jsonCustomerAddr.getString("name") != null
			&& !jsonCustomerAddr.getString("name").equals("")) {
		    rootLocation.setAddressLine1(jsonCustomerAddr
			    .getString("name"));
		}
		rootLocation.setPostalCode(jsonCustomerAddr
			.getString("postalCode"));
		rootLocation
			.setCityName(jsonCustomerAddr.getString("cityName"));
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
