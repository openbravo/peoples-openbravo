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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.javascript;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.openbravo.javascript.ErrorReporter;
import org.openbravo.javascript.EvaluatorException;
import org.openbravo.test.base.BaseTest;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Test the compression of a static js file.
 * 
 * @author mtaal
 */

public class CompressionTest extends BaseTest {
  private static final Logger log = Logger.getLogger(JavaScriptAntTest.class);

  public void testCompression() throws Exception {
    final LocalJSCompressor compressor = new LocalJSCompressor();
    final InputStream is = this.getClass().getResourceAsStream("test-compression.js");
    String line;
    final StringBuilder sb = new StringBuilder();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }
    final String compressed = compressor.compress(sb.toString());
    assertNotNull(compressed);
    is.close();
  }

  private static class LocalJSCompressor {

    public String compress(String javascript) throws Exception {
      final StringReader reader = new StringReader(javascript);
      final JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new ErrorReporter() {

        public void warning(String message, String sourceName, int line, String lineSource,
            int lineOffset) {
          if (line < 0) {
            log.warn(message);
          } else {
            log.warn(line + ':' + lineOffset + ':' + message);
          }
        }

        public void error(String message, String sourceName, int line, String lineSource,
            int lineOffset) {
          if (line < 0) {
            log.error(message);
          } else {
            log.error(line + ':' + lineOffset + ':' + message);
          }
        }

        public EvaluatorException runtimeError(String message, String sourceName, int line,
            String lineSource, int lineOffset) {
          error(message, sourceName, line, lineSource, lineOffset);
          return new EvaluatorException(message);
        }
      });

      final StringWriter writer = new StringWriter();
      final boolean munge = false;
      final boolean verbose = true;
      boolean preserveAllSemiColons = true;
      boolean disableOptimizations = true;

      compressor.compress(writer, -1, munge, verbose, preserveAllSemiColons, disableOptimizations);
      return writer.toString();
    }

  }

}
