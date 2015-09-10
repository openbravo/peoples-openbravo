/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.actionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.retail.posterminal.POSConstants;

public class ChooseWarehouseActionHandler extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject JSONObject = null;
    JSONObject result = new JSONObject();
    JSONArray actions = new JSONArray();
    JSONObject msg = new JSONObject();
    JSONObject showMsgInView = new JSONObject();
    JSONObject refreshAction = new JSONObject();
    JSONObject selectedObject = new JSONObject();
    JSONObject selectedTed = new JSONObject();
    ArrayList<String> selectedWarehouseList = new ArrayList<String>();
    try {
      // Create refresh action
      refreshAction.put("refreshGrid", new JSONObject());
      actions.put(refreshAction);

      selectedObject = new JSONObject(content);
      Organization organization = OBDal.getInstance().get(Organization.class,
          selectedObject.getString("AD_Org_ID"));
      JSONArray selectArray = selectedObject.getJSONObject("_params").getJSONObject("select_CC")
          .getJSONArray("_selection");

      for (int i = 0; i < selectArray.length(); i++) {
        selectedTed = selectArray.getJSONObject(i);
        selectedWarehouseList.add(selectedTed.getString("id"));
      }
      OBCriteria<OrgWarehouse> orgWarehouseCrit = OBDal.getInstance().createCriteria(
          OrgWarehouse.class);
      orgWarehouseCrit.add(Restrictions.eq(OrgWarehouse.PROPERTY_ORGANIZATION, organization));
      orgWarehouseCrit.add(Restrictions.eq(OrgWarehouse.PROPERTY_WAREHOUSETYPE,
          POSConstants.CROSS_CHANNEL));

      List<OrgWarehouse> currentOrgWarehouseList = orgWarehouseCrit.list();
      boolean isInTheList = false;
      for (OrgWarehouse warehouse : currentOrgWarehouseList) {
        isInTheList = false;
        for (String warehouseSelected : selectedWarehouseList) {
          if (warehouse.getWarehouse().getId().equals(warehouseSelected)) {
            isInTheList = true;
          }
        }
        if (!isInTheList) {
          OBDal.getInstance().remove(warehouse);
        }
      }

      for (String warehouseSelected : selectedWarehouseList) {
        isInTheList = false;
        for (OrgWarehouse warehouse : currentOrgWarehouseList) {
          if (warehouse.getWarehouse().getId().equals(warehouseSelected)) {
            isInTheList = true;
          }
        }
        if (!isInTheList) {
          createWarehouseEntry(warehouseSelected, organization);
        }
      }

      OBDal.getInstance().getConnection().commit();
      result.put("responseActions", actions);
    } catch (Exception e) {
      try {
        result.put("responseActions", actions);
        return result;
      } catch (JSONException e1) {
        // won't happen
      }
    }
    return result;
  }

  private void createWarehouseEntry(String warehouseSelected, Organization organization) {
    OrgWarehouse orgWarehouse = OBProvider.getInstance().get(OrgWarehouse.class);
    orgWarehouse.setOrganization(organization);
    orgWarehouse.setWarehouseType(POSConstants.CROSS_CHANNEL);
    orgWarehouse.setWarehouse(OBDal.getInstance().get(Warehouse.class, warehouseSelected));
    OBDal.getInstance().save(orgWarehouse);
  }
}