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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openbravo.base.MultipartRequest;
import org.openbravo.base.VariablesBase;
import org.openbravo.data.FieldProvider;

class FileLoadData extends MultipartRequest {
  static Logger log4j = Logger.getLogger(FileLoadData.class);

  public FileLoadData() {
  }

  public FileLoadData(VariablesBase _vars, String _filename, boolean _firstLineHeads,
      String _format, FieldProvider[] _data) throws IOException {
    super(_vars, _filename, _firstLineHeads, _format, _data);
  }

  public FileLoadData(VariablesBase _vars, String _filename, boolean _firstLineHeads, String _format)
      throws IOException {
    super(_vars, _filename, _firstLineHeads, _format, null);
  }

  public FieldProvider lineSeparatorFormated(String linea) {
    if (linea == null || linea.length() < 1)
      return null;
    FileLoadData fileLoadData = new FileLoadData();
    int siguiente = 0;
    int anterior = 0;
    String texto = "";
    while (siguiente < linea.length()) {
      siguiente = linea.indexOf(format, siguiente + 1);
      if (siguiente == -1)
        siguiente = linea.length();
      texto = linea.substring(anterior, siguiente);
      if (texto.length() > 0) {
        if (texto.charAt(0) == '"') {
          texto = texto.substring(1);
          if (texto.charAt(texto.length() - 1) == '"') {
            texto = texto.substring(0, texto.length() - 1);
          } else {
            anterior = siguiente + 1;
            siguiente = linea.indexOf("\"", siguiente + 1);
            if (siguiente == -1)
              siguiente = linea.length();
            texto = texto + format + linea.substring(anterior, siguiente);
            siguiente = siguiente + 1;
          }
        }
      }
      try {
        if (log4j.isDebugEnabled())
          log4j.debug("FileLoadData - setFieldProvider - text = " + texto);
        fileLoadData.addField(texto);
      } catch (Exception e) {
        log4j.warn("File.load: " + e);
      }
      anterior = siguiente + 1;
    }
    return fileLoadData;
  }

}
