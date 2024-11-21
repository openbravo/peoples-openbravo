/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at https://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.authentication;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.openbravo.dal.core.OBContext;
import org.openbravo.email.EmailEventContentGenerator;
import org.openbravo.erpCommon.utility.OBMessageUtils;

/**
 * Email generator for {@link ForgotPasswordService#EVT_FORGOT_PASSWORD} event which is triggered
 * when a user is granted with portal privileges.
 * 
 * @author asier
 * 
 */
public class ForgotPasswordEmailGenerator implements EmailEventContentGenerator {

  @Inject
  private ForgotPasswordEmailBody body;

  @Override
  public String getSubject(Object data, String event) {
    String msg = "Subject_ForgotPassword";
    return OBMessageUtils.getI18NMessage(msg,
        new String[] { OBContext.getOBContext().getCurrentClient().getName() });
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getBody(Object data, String event) {
    body.setData((Map<String, Object>) data);
    return body.generate();
  }

  @Override
  public String getContentType() {
    return "text/html; charset=utf-8";
  }

  @Override
  public boolean isValidEvent(String event, Object data) {
    return ForgotPasswordService.EVT_FORGOT_PASSWORD.equals(event);
  }

  @Override
  public int getPriority() {
    return 100;
  }

  @Override
  public boolean preventsOthersExecution() {
    return false;
  }

  @Override
  public boolean isAsynchronous() {
    return false;
  }

  @Override
  public List<File> getAttachments(Object data, String event) {
    return null;
  }
}
