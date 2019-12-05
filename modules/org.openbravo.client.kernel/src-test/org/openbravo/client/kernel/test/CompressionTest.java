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
 * All portions are Copyright (C) 2009-2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel.test;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.openbravo.client.kernel.JSCompressor;

/**
 * Test the compression of a static js file.
 * 
 * @author mtaal
 */

public class CompressionTest {

  @Test
  public void testCompression() throws IOException {
    final JSCompressor compressor = new JSCompressor();
    final InputStream is = this.getClass().getResourceAsStream("test-compression.js");
    String line;
    final StringBuilder sb = new StringBuilder();
    final BufferedReader reader = new BufferedReader(
        new InputStreamReader(is, StandardCharsets.UTF_8));
    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }
    final String compressed = compressor.compress(sb.toString());
    assertThat("Original size is at least twice bigger than original", sb.length(),
        greaterThan(2 * compressed.length()));
    is.close();
  }
}
