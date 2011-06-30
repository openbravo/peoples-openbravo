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

package org.openbravo.base.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * An output stream writer which always does UTF-8 and uses unix line endings.
 * 
 * @author mtaal
 */
public class UnixOutputStreamWriter extends OutputStreamWriter {

  private static final char R_CHAR = '\r';
  private static final char N_CHAR = '\n';

  public UnixOutputStreamWriter(OutputStream os) throws UnsupportedEncodingException {
    super(os, "UTF-8");
  }

  public void write(char[] cbuf, int offset, int length) throws IOException {
    for (int i = offset; i < offset + length; i++) {
      if (cbuf[i] != R_CHAR || (i < cbuf.length - 1 && cbuf[i + 1] != N_CHAR)) {
        super.write(cbuf[i]);
      }
    }
  }

  public void write(int c) throws IOException {
    if (c != R_CHAR)
      super.write(c);
  }

  public void write(String str, int offset, int length) throws IOException {
    String orig = str.substring(offset, offset + length);
    String corrected = orig.replace(R_CHAR + N_CHAR + "", N_CHAR + "");
    int lengthDiff = orig.length() - corrected.length();
    if (corrected.endsWith("\r")) {
      super.write(corrected.substring(0, corrected.length() - 1), 0, length - lengthDiff - 1);
    } else {
      super.write(corrected, 0, length - lengthDiff);
    }
  }
}
