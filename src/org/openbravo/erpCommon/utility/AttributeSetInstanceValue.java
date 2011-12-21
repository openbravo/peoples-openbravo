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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.sql.Connection;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.Replace;

public class AttributeSetInstanceValue {

  private String lot = "";
  private String serno = "";
  private String guaranteedate = "";
  private String locked = "N";
  private String lockDescription = "";
  private String attSetInstanceId = "";

  protected Logger log4j = Logger.getLogger(this.getClass());

  public AttributeSetInstanceValue() {
  }

  public AttributeSetInstanceValue(String strlot, String strserno, String strguaranteedate,
      String strlocked, String strlockDescription) {
    this.lot = strlot;
    this.serno = strserno;
    this.guaranteedate = strguaranteedate;
    this.locked = strlocked;
    this.lockDescription = strlockDescription;

  }

  public void setLot(String _data) {
    if (_data == null)
      _data = "";
    this.lot = _data;
  }

  public String getLot() {
    return ((this.lot == null) ? "" : this.lot);
  }

  public void setSerialNumber(String _data) {
    if (_data == null)
      _data = "";
    this.serno = _data;
  }

  public String getSerialNumber() {
    return ((this.serno == null) ? "" : this.serno);
  }

  public void setGuaranteeDate(String _data) {
    if (_data == null)
      _data = "";
    this.guaranteedate = _data;
  }

  public String getGuaranteeDate() {
    return ((this.guaranteedate == null) ? "" : this.guaranteedate);
  }

  public void setLockDescription(String _data) {
    if (_data == null)
      _data = "";
    this.lockDescription = _data;
  }

  public String getLockDescription() {
    return ((this.lockDescription == null) ? "" : this.lockDescription);
  }

  public void setLocked(String _data) {
    if (_data == null)
      _data = "";
    this.locked = _data;
  }

  public String getLocked() {
    return ((this.locked == null) ? "" : this.locked);
  }

  public String getAttSetInstanceId() {
    return ((this.attSetInstanceId == null) ? "" : this.attSetInstanceId);
  }

  public OBError setAttributeInstance(ConnectionProvider conProv, VariablesSecureApp vars,
      AttributeSetInstanceValueData[] data, String strAttributeSet, String strInstance,
      String strWindow, String strIsSOTrx, String strProduct,
      HashMap<String, String> attributeValues) throws ServletException {

    String strNewInstance = "";

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    myMessage.setType("Success");
    myMessage.setMessage(Utility.messageBD(conProv, "Success", vars.getLanguage()));
    if (data == null || data.length == 0) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(conProv, "FindZeroRecords", vars.getLanguage()));
      // Utility.messageBD(this, "FindZeroRecords", vars.getLanguage());
      return myMessage;
    }

    boolean isinstance = !AttributeSetInstanceValueData.isInstanceAttribute(conProv,
        strAttributeSet).equals("0");
    String strDescription = getDescription(conProv, vars, data, strIsSOTrx, strWindow,
        attributeValues);
    Connection conn = null;
    try {
      conn = conProv.getTransactionConnection();
      String description = "", description_first = "";
      if (data[0].islot.equals("Y")) {
        if (!data[0].mLotctlId.equals("") && (strIsSOTrx.equals("N") || strWindow.equals("191"))) {
          lot = AttributeSetInstanceValueData.selectNextLot(conProv, data[0].mLotctlId);
          AttributeSetInstanceValueData.updateLotSequence(conn, conProv, vars.getUser(),
              data[0].mLotctlId);
          description_first += (description_first.equals("") ? "" : "_") + lot;// esto
        } else {
          description_first += (description_first.equals("") ? "" : "_") + "L" + lot;
        }
      }
      if (data[0].isserno.equals("Y")) {
        if (!data[0].mSernoctlId.equals("") && (strIsSOTrx.equals("N") || strWindow.equals("191"))) {
          serno = AttributeSetInstanceValueData.selectNextSerNo(conn, conProv, data[0].mSernoctlId);
          AttributeSetInstanceValueData.updateSerNoSequence(conn, conProv, vars.getUser(),
              data[0].mSernoctlId);
          description_first += (description_first.equals("") ? "" : "_") + serno;
        } else {
          description_first += (description_first.equals("") ? "" : "_") + "#" + serno;
        }
      }
      if (data[0].isguaranteedate.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + guaranteedate;
      }
      if (data[0].islockable.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + lockDescription;
      }
      if (!isinstance) {
        strNewInstance = AttributeSetInstanceValueData.hasIdentical(conProv, strDescription,
            data[0].mAttributesetId);
      }
      boolean hasToUpdate = false;
      if ((!strInstance.equals("")) && (isinstance)) {
        // Si if it's existant and requestable, it edits it
        hasToUpdate = true;
        if (AttributeSetInstanceValueData.updateHeader(conn, conProv, vars.getUser(),
            data[0].mAttributesetId, serno, lot, guaranteedate, "", locked, lockDescription,
            strInstance) == 0) {
          AttributeSetInstanceValueData.insertHeader(conn, conProv, strInstance, vars.getClient(),
              vars.getOrg(), vars.getUser(), data[0].mAttributesetId, serno, lot, guaranteedate,
              "", locked, lockDescription);
        }
      } else if ((isinstance) || (strNewInstance.equals(""))) { // New or
        // editable,if it's requestable or doesn't exist the identic, then it inserts a new one
        hasToUpdate = true;
        strNewInstance = SequenceIdData.getUUID();
        AttributeSetInstanceValueData.insertHeader(conn, conProv, strNewInstance, vars.getClient(),
            vars.getOrg(), vars.getUser(), data[0].mAttributesetId, serno, lot, guaranteedate, "",
            locked, lockDescription);
      }
      if (hasToUpdate) {
        if (!data[0].elementname.equals("")) {
          for (int i = 0; i < data.length; i++) {
            String strValue = attributeValues.get(replace(data[i].elementname));
            if ((strValue == null || strValue.equals("")) && data[i].ismandatory.equals("Y")) {
              throw new ServletException("Request parameter required: "
                  + replace(data[i].elementname));
            }
            if (strValue == null)
              strValue = "";
            String strDescValue = strValue;
            if (data[i].islist.equals("Y"))
              strDescValue = AttributeSetInstanceValueData.selectAttributeValue(conProv, strValue);
            if (!strNewInstance.equals("")) {
              if (AttributeSetInstanceValueData.update(conn, conProv, vars.getUser(),
                  (data[i].islist.equals("Y") ? strValue : ""), strDescValue, strNewInstance,
                  data[i].mAttributeId) == 0) {
                String strNewAttrInstance = SequenceIdData.getUUID();
                AttributeSetInstanceValueData.insert(conn, conProv, strNewAttrInstance,
                    strNewInstance, data[i].mAttributeId, vars.getClient(), vars.getOrg(),
                    vars.getUser(), (data[i].islist.equals("Y") ? strValue : ""), strDescValue);
              }
            } else {
              if (AttributeSetInstanceValueData.update(conn, conProv, vars.getUser(),
                  (data[i].islist.equals("Y") ? strValue : ""), strDescValue, strInstance,
                  data[i].mAttributeId) == 0) {
                String strNewAttrInstance = SequenceIdData.getUUID();
                AttributeSetInstanceValueData.insert(conn, conProv, strNewAttrInstance,
                    strInstance, data[i].mAttributeId, vars.getClient(), vars.getOrg(),
                    vars.getUser(), (data[i].islist.equals("Y") ? strValue : ""), strDescValue);
              }
            }
            description += (description.equals("") ? "" : "_") + strDescValue;
          }
        }
        if (!description_first.equals(""))
          description += (description.equals("") ? "" : "_") + description_first;
        AttributeSetInstanceValueData.updateHeaderDescription(conn, conProv, vars.getUser(),
            description, (strNewInstance.equals("") ? strInstance : strNewInstance));
      }
      conProv.releaseCommitConnection(conn);
      this.attSetInstanceId = (strNewInstance.equals("") ? strInstance : strNewInstance);
    } catch (Exception e) {
      try {
        conProv.releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      log4j.error("Rollback in transaction: " + e);
    }

    return myMessage;
  }

  private String replace(String strIni) {
    // delete characters: " ","&",","
    return Replace.replace(Replace.replace(Replace.replace(
        Replace.replace(Replace.replace(Replace.replace(strIni, "#", ""), " ", ""), "&", ""), ",",
        ""), "(", ""), ")", "");
  }

  private String getDescription(ConnectionProvider conProv, VariablesSecureApp vars,
      AttributeSetInstanceValueData[] data, String strIsSOTrx, String strWindowId,
      HashMap<String, String> attributeValues) {
    if (data == null || data.length == 0)
      return "";
    String description = "";
    try {
      // AttributeSet header
      String description_first = "";
      if (data[0].islot.equals("Y")) {
        if (!data[0].mLotctlId.equals("") && (strIsSOTrx.equals("N") || strWindowId.equals("191"))) {
          description_first += (description_first.equals("") ? "" : "_") + lot;// esto
        } else
          description_first += (description_first.equals("") ? "" : "_") + "L" + lot;
      }
      if (data[0].isserno.equals("Y")) {
        if (!data[0].mSernoctlId.equals("")
            && (strIsSOTrx.equals("N") || strWindowId.equals("191"))) {
          description_first += (description_first.equals("") ? "" : "_") + serno;
        } else
          description_first += (description_first.equals("") ? "" : "_") + "#" + serno;
      }
      if (data[0].isguaranteedate.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + guaranteedate;
      }
      if (data[0].islockable.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + lockDescription;
      }

      if (!data[0].elementname.equals("")) {
        for (int i = 0; i < data.length; i++) {
          String strValue = attributeValues.get(replace(data[i].elementname));
          if ((strValue == null || strValue.equals("")) && data[i].ismandatory.equals("Y")) {
            throw new ServletException("Request parameter required: "
                + replace(data[i].elementname));
          }
          if (strValue == null)
            strValue = "";
          String strDescValue = strValue;
          if (data[i].islist.equals("Y"))
            strDescValue = AttributeSetInstanceValueData.selectAttributeValue(conProv, strValue);
          description += (description.equals("") ? "" : "_") + strDescValue;
        }
      }
      if (!description_first.equals(""))
        description += (description.equals("") ? "" : "_") + description_first;
    } catch (ServletException e) {
      return "";
    }
    return description;
  }

}
