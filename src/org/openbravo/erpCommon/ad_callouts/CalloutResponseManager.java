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

import java.util.ArrayList;

import org.mozilla.javascript.NativeArray;

/**
 * CalloutResponseManager provides the information that is used to populate the messages,
 * comboEntries,etc updated by a SimpleCallout.
 * 
 * @author inigo.sanchez
 *
 */
public class CalloutResponseManager implements CalloutInformationProvider {

  ArrayList<NativeArray> returnedArray;

  public CalloutResponseManager(ArrayList<NativeArray> nativeArray) {
    returnedArray = nativeArray;
  }

  public ArrayList<NativeArray> getNativeArray() {
    return returnedArray;
  }

  @Override
  public Object getNameElement(Object values) {
    NativeArray element = (NativeArray) values;
    return element.get(0, null);
  }

  public Object getValue(Object values, int position) {
    NativeArray element = (NativeArray) values;
    return element.get(position, null);
  }

  public int getNativeArraySize() {
    return returnedArray.size();
  }

  @Override
  public Boolean isComboData(Object values) {
    Boolean isCombo = false;
    NativeArray element = (NativeArray) values;
    if (element.get(1, null) instanceof NativeArray) {
      isCombo = true;
    }
    return isCombo;
  }

  @Override
  public void manageComboData() {

  }
}
