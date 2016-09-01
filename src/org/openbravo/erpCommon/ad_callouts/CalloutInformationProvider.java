/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import org.apache.log4j.Logger;

/**
 * CalloutInformationProvider will provide the information that is used to populate the messages,
 * comboEntries etc... for Callouts.
 * 
 * @author inigo.sanchez
 *
 */
public interface CalloutInformationProvider {

  static final Logger log = Logger.getLogger(CalloutInformationProvider.class);

  public Object getNameElement(Object values);

  public Object getValue(Object values, int pos);

  public Boolean isComboData(Object values);

  public void manageComboData();
}
