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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.materialmgmt.utility;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.model.ad.utility.Sequence;

public class EANUtility {

  private static int controlCodeCalculator(String preEAN) {
    char[] charDigits = preEAN.toCharArray();
    int[] ean13 = { 1, 3 };
    int sum = 0;
    for (int i = 0; i < charDigits.length; i++) {
      sum += Character.getNumericValue(charDigits[i]) * ean13[i % 2];
    }
    int checksum = 10 - sum % 10;
    if (checksum == 10) {
      checksum = 0;
    }
    return checksum;
  }

  public static String generateEAN(final Sequence sequence) throws OBException {
    String ean = "";
    String prefix = sequence.getPrefix() == null ? "" : sequence.getPrefix();
    ean = prefix + StringUtils.leftPad(sequence.getNextAssignedNumber().toString(),
        12 - prefix.length(), "0");
    sequence.setNextAssignedNumber(sequence.getNextAssignedNumber() + sequence.getIncrementBy());
    ean = ean + controlCodeCalculator(ean);
    return ean;
  }
}
