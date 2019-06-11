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
 * All portions are Copyright (C) 2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;

/** Tests CSS minification */
public class CSSMinifier {

  /** see issue #41060 */
  @Test
  public void minLowerCase() {
    //@formatter:off
    String original =".testGridContainer {\n" + 
        "        grid-template-areas: \"obUiMultiColumn-left-leftToolbar obUiMultiColumn-right-rightToolbar\";\n" + 
        "}\n" + 
        "\n" + 
        ".testGridElement {\n" + 
        "        grid-area: obUiMultiColumn-left-leftToolbar;\n" + 
        "}";
    //@formatter:on
    String minified = CSSMinimizer.formatString(original);

    String shouldBe = ".*grid-area:\\s*?obUiMultiColumn-left-leftToolbar.*";
    Pattern p = Pattern.compile(shouldBe, Pattern.DOTALL);

    assertThat("Original CSS [" + original + "] contains " + shouldBe,
        p.matcher(original).matches(), is(true));
    assertThat("Minified CSS [" + minified + "] contains " + shouldBe,
        p.matcher(minified).matches(), is(true));
  }

}
