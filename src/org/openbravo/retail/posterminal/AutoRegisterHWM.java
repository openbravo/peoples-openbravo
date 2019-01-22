/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.web.WebService;

/**
 * This class implements a web service that saves the hardware manager data just received as with
 * its dependencies
 */
public class AutoRegisterHWM implements WebService {
  private static final String STORE_SEARCH_KEY = "storeSearchKey";
  private static final String HARDWARE_URL = "hardwareUrl";
  private static final String HARDWARE_NAME = "hardwareName";
  private static final String RECEIPT_PRINTER = "receiptPrinter";
  private static final String PDF_PRINTER = "pdfPrinter";

  @Override
  public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    JSONObject parameters = getParameters(request);
    String storeSearchKey = (String) getParameter(parameters, STORE_SEARCH_KEY,
        "The organization identifier is mandatory");
    Organization org = null;
    String hardwareUrl = (String) getParameter(parameters, HARDWARE_URL,
        "The Hardware Manager url is mandatory");
    String hardwareName = (String) getParameter(parameters, HARDWARE_NAME,
        "The Hardware Manager name is mandatory");
    Boolean receiptPrinter = (Boolean) getParameter(parameters, RECEIPT_PRINTER, null);
    Boolean pdfPrinter = (Boolean) getParameter(parameters, PDF_PRINTER, null);

    OBCriteria<Organization> orgQuery = OBDal.getInstance().createCriteria(Organization.class);
    orgQuery.add(Restrictions.eq(Organization.PROPERTY_SEARCHKEY, storeSearchKey));
    orgQuery.setMaxResults(1);
    org = (Organization) orgQuery.uniqueResult();
    if (org != null) {
      syncTerminalTypes(org,
          syncHardwareManager(org, hardwareUrl, hardwareName, receiptPrinter, pdfPrinter));
      OBDal.getInstance().flush();
      final String res = OBMessageUtils.getI18NMessage("OBPOS_AutoRegisterComplete", null);
      response.setContentType("text/plain");
      response.setCharacterEncoding("utf-8");
      final Writer w = response.getWriter();
      w.write(res);
      w.close();
    } else {
      throw new IllegalArgumentException("The Organization does not exists");
    }

  }

  private Object getParameter(JSONObject parameters, String param, String error) throws Exception {
    if (parameters.has(param)) {
      return parameters.get(param);
    } else if (error != null) {
      throw new IllegalArgumentException(error);
    } else {
      return true;
    }
  }

  private HardwareManager syncHardwareManager(Organization org, String hardwareUrl,
      String hardwareName, Boolean receiptPrinter, Boolean pdfPrinter) {
    OBCriteria<HardwareManager> hardwareQuery = OBDal.getInstance().createCriteria(
        HardwareManager.class);
    hardwareQuery.add(Restrictions.eq(HardwareManager.PROPERTY_ORGANIZATION, org));
    hardwareQuery.add(Restrictions.eq(HardwareManager.PROPERTY_HARDWAREURL, hardwareUrl));
    hardwareQuery.setMaxResults(1);
    HardwareManager hardwareManager = (HardwareManager) hardwareQuery.uniqueResult();
    if (hardwareManager == null) {
      return saveHardwareManager(org, hardwareName, hardwareUrl, receiptPrinter, pdfPrinter);
    } else {
      return hardwareManager;
    }
  }

  private HardwareManager saveHardwareManager(Organization org, String hardwareName,
      String hardwareUrl, Boolean receiptPrinter, Boolean pdfPrinter) {
    HardwareManager hardwaremng = OBProvider.getInstance().get(HardwareManager.class);
    hardwaremng.setClient(org.getClient());
    hardwaremng.setOrganization(org);
    hardwaremng.setName(hardwareName);
    hardwaremng.setHardwareURL(hardwareUrl);
    hardwaremng.setAutoregister(true);
    if (receiptPrinter != null) {
      hardwaremng.setHasReceiptPrinter(receiptPrinter);
    }
    if (pdfPrinter != null) {
      hardwaremng.setHasPDFPrinter(pdfPrinter);
    }
    OBDal.getInstance().save(hardwaremng);
    OBDal.getInstance().flush();
    return hardwaremng;
  }

  private void syncTerminalTypes(Organization org, HardwareManager hardwaremng) {
    OBCriteria<TerminalType> terminaltypeQuery = OBDal.getInstance().createCriteria(
        TerminalType.class);
    terminaltypeQuery.add(Restrictions.eq(TerminalType.PROPERTY_ORGANIZATION, org));
    terminaltypeQuery.add(Restrictions.eq(TerminalType.PROPERTY_AUTOREGISTERHWMURL, true));
    final ScrollableResults terminalScroller = terminaltypeQuery.scroll(ScrollMode.FORWARD_ONLY);
    int i = 0;
    while (terminalScroller.next()) {
      final TerminalType termType = (TerminalType) terminalScroller.get()[0];
      addHardwareToTerminalType(termType, hardwaremng, org);
      if ((i % 100) == 0) {
        OBDal.getInstance().flush();
        OBDal.getInstance().getSession().clear();
      }
    }
    terminalScroller.close();
  }

  private void addHardwareToTerminalType(TerminalType termtype, HardwareManager hardwaremng,
      Organization org) {
    OBCriteria<HardwareURL> hardwareUrlQuery = OBDal.getInstance()
        .createCriteria(HardwareURL.class);
    hardwareUrlQuery.add(Restrictions.eq(HardwareURL.PROPERTY_POSTERMINALTYPE, termtype));
    hardwareUrlQuery.add(Restrictions.eq(HardwareURL.PROPERTY_OBPOSHARDWAREMNG, hardwaremng));
    hardwareUrlQuery.setMaxResults(1);
    if (hardwareUrlQuery.count() == 0) {
      saveHardwareUrl(termtype, org, hardwaremng);
    }
  }

  private void saveHardwareUrl(TerminalType termtype, Organization org, HardwareManager hardwaremng) {
    HardwareURL hardwareurl = OBProvider.getInstance().get(HardwareURL.class);
    hardwareurl.setClient(termtype.getClient());
    hardwareurl.setOrganization(termtype.getOrganization());
    hardwareurl.setPOSTerminalType(termtype);
    hardwareurl.setObposHardwaremng(hardwaremng);
    OBDal.getInstance().save(hardwareurl);
  }

  private JSONObject getParameters(HttpServletRequest request) {
    StringBuffer params = new StringBuffer();
    String line = null;
    JSONObject parameters = null;
    try {
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null)
        params.append(line);
      parameters = new JSONObject(params.toString());
    } catch (JSONException | IOException e) {
      throw new IllegalArgumentException("Parameters are mandatory");
    }
    return parameters;
  }

  @Override
  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
  }

  @Override
  public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
  }

  @Override
  public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

  }
}