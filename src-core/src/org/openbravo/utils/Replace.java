/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.utils;

public class Replace{ 

  public static String replace(String strInicial, String strReplaceWhat, String strReplaceWith) {
    int index = 0;
    int pos;
    if (strInicial==null || strInicial.equals("")) return strInicial;
    else if (strReplaceWhat==null || strReplaceWhat.equals("")) return strInicial;
    else if (strReplaceWith==null) strReplaceWith = "";
    StringBuffer strFinal = new StringBuffer("");
    do {
      pos = strInicial.indexOf(strReplaceWhat, index);
      if (pos != - 1) {
        strFinal.append(strInicial.substring(index, pos) + strReplaceWith);
        index = pos + strReplaceWhat.length();
      } else {
        strFinal.append(strInicial.substring(index));
      }
    } while (index < strInicial.length() && pos != -1);
    return strFinal.toString();
  }
}// End of class
