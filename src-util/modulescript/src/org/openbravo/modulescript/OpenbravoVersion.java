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

package org.openbravo.modulescript;

/**
 * 
 * @author adrian
 */
public class OpenbravoVersion implements Comparable<OpenbravoVersion> {

  private int mayor;
  private int minor;
  private int build;

  public OpenbravoVersion(int mayor, int minor, int build) {
    this.mayor = mayor;
    this.minor = minor;
    this.build = build;
  }

  public OpenbravoVersion(String version) {

    String[] numbers = version.split("\\.");

    if (numbers.length != 3) {
      throw new IllegalArgumentException("Version must consist in three numbers separated by .");
    }
    this.mayor = Integer.valueOf(numbers[0]);
    this.minor = Integer.valueOf(numbers[1]);
    this.build = Integer.valueOf(numbers[2]);
  }

  public int getMayor() {
    return mayor;
  }

  public int getMinor() {
    return minor;
  }

  public int getBuild() {
    return build;
  }

  public int compareTo(OpenbravoVersion o) {
    if (mayor == o.mayor) {
      if (minor == o.minor) {
        return (build < o.build ? -1 : (build == o.build ? 0 : 1));
      } else {
        return minor < o.minor ? -1 : 1;
      }
    } else {
      return mayor < o.mayor ? -1 : 1;
    }
  }

  @Override
  public String toString() {
    return Integer.toString(mayor) + "." + Integer.toString(minor) + "." + Integer.toString(build);
  }
}
