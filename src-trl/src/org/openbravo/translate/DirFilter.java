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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.translate;

import java.io.FilenameFilter;
import java.io.File;

public class DirFilter implements FilenameFilter {
  String afn;
  DirFilter(String afn) {
    this.afn = afn;
  }

  public boolean accept(File dir, String name) {
    boolean boolReturn;
    // obtain the name to compare it only with the file name and not with the whole path
    String f = new File(name).getName();
    // return true if it matches the filter or if it's a directory
    boolReturn = f.indexOf(afn, f.length() - afn.length()) != -1 || new File(dir,name).isDirectory();
    return boolReturn;
  }
}
