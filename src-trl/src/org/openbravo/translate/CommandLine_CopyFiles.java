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
 * All portions are Copyright (C) 2001-2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.translate;

import org.openbravo.utils.FileUtility;
import org.openbravo.utils.DirFilter;
import org.openbravo.database.CPStandAlone;

import java.io.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

class CommandLine_CopyFiles {
  static Logger log4j = Logger.getLogger(CommandLine_CopyFiles.class);

  public static void main(String argv[]) throws Exception {
    PropertyConfigurator.configure("log4j.lcf");
    String connectionFile;
    String source;
    String destiny;
    DirFilter filter = null;
    boolean overwrite = false;
    boolean discardHidden = false;
    String language = "";

    if (argv.length < 6) {
      log4j.error("Usage: java org.openbravo.erpCommon.utility.CommandLine_CopyFiles XmlPool.xml source destiny filter discard_hidden overwrite [language]");
      return;
    }

    connectionFile = argv[0];
    source = argv[1];
    destiny = argv[2];
    filter = new DirFilter(argv[3]);
    if (argv[4].equals("true")) discardHidden = true;
    if (argv[5].equals("true")) overwrite = true;
    if (argv.length > 6) language = argv[6];

    File fileSource = new File(source);
    if (!fileSource.exists()) throw new Exception("Source directory doesn't exists: " + source);

    CPStandAlone conn = null;
    try {
      conn = new CPStandAlone(connectionFile);
      LanguageComboData[] data = LanguageComboData.selectOthers(conn, language);
      if (data!=null && data.length>0) {
        for (int i=0;i<data.length;i++) {
          File fileLang = new File(destiny, data[i].adLanguage);
          fileLang.mkdir();
          log4j.info("files copied to " + data[i].adLanguage + ": " + FileUtility.copy(fileSource, fileLang, filter, discardHidden, overwrite));
        }
      }

      conn.destroy();
    } catch (Exception ex) {
      if (conn!=null) conn.destroy();
      throw ex;
    }
  }
}
