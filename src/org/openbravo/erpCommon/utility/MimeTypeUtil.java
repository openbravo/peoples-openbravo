/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SL
 * All portions are Copyright (C) 2010 Openbravo SL
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;

/**
 * Utility class to detect MIME type based on data array. This class is based on Apache Tika part of
 * the Lucene project. http://lucene.apache.org/tika/
 * 
 * @author iperdomo
 * 
 */
public class MimeTypeUtil {
  private static MimeTypes mimeTypes;
  private static MimeTypeUtil instance = new MimeTypeUtil();

  private static void init() {
    mimeTypes = TikaConfig.getDefaultConfig().getMimeRepository();
  }

  /**
   * Returns the instance of the MimeTypeUtil class
   * 
   * @return MimeTypeUtil instance
   */
  public static MimeTypeUtil getInstance() {
    if (mimeTypes == null) {
      init();
    }
    return instance;
  }

  public static void setInstace(MimeTypeUtil ins) {
    MimeTypeUtil.instance = ins;
  }

  /**
   * Returns a MimeType object based on the byte array provided as parameter
   * 
   * @param data
   *          byte array from which we want to detect the MIME type
   * @return MimeType representation
   */
  public MimeType getMimeType(byte[] data) {
    return mimeTypes.getMimeType(data);
  }

  /**
   * Returns the MIME type name, e.g. image/png based on the byte array passed as parameter. Returns
   * application/octet-stream if no better match is found.
   * 
   * @param data
   *          byte array from which we want to detect the MIME type
   * @return A MIME type name, e.g. "image/png"
   */
  public String getMimeTypeName(byte[] data) {
    return mimeTypes.getMimeType(data).getName();
  }

}
