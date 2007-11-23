/*
 ************************************************************************************
 * Copyright (C) 2001-2007 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.utils;

import javax.servlet.ServletException;
import org.apache.log4j.Logger ;

public class FormatUtilities {
  static Logger log4j = Logger.getLogger(FormatUtilities.class);

  public static String truncate(String s, int i) {
    if(s == null || s.length() == 0) return "";
    if(i < s.length()) s = s.substring(0, i) + "...";
    return s;
  }

  public static String replaceTildes(String strIni) {
    //Delete tilde characters
    return Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( strIni, "á", "a"), "é", "e"), "í", "i"), "ó", "o"), "ú", "u"), "Á", "A"), "É", "E"), "Í", "I"), "Ó", "O"), "Ú", "U");
  }

  public static String replace(String strIni) {
    //delete characters: " ","&",","
    return Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( Replace.replace( replaceTildes(strIni), "-",""), "/", ""), "#", ""), " ", ""), "&", ""), ",", ""), "(", ""), ")", "");
  }

  public static String replaceJS(String strIni) {
    return replaceJS(strIni, true);
  }

  public static String replaceJS(String strIni, boolean isUnderQuotes) {
    return Replace.replace( Replace.replace(Replace.replace(Replace.replace(strIni, "'", (isUnderQuotes?"\\'":"&#039;")), "\"", "\\\""), "\n", "\\n"), "\r", "");
  }

  public static String sha1Base64(String text) throws ServletException {
    if (text==null || text.trim().equals("")) return "";
    String result = text;
    result = CryptoSHA1BASE64.hash(text);
    return result;
  }

  public static String encryptDecrypt(String text, boolean encrypt) throws ServletException {
    if (text==null || text.trim().equals("")) return "";
    String result = text;
    if (encrypt) result = CryptoUtility.encrypt(text);
    else result = CryptoUtility.decrypt(text);
    return result;
  }
}
