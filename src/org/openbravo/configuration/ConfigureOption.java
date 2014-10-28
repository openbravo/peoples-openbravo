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
 * All portions are Copyright (C) 2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.configuration;

import java.util.ArrayList;

import org.apache.tools.ant.Project;

/**
 * Option Class is used to the process of configuration of Openbravo.properties and others files, at
 * the beginning of the installation of OpenBravo.
 * 
 * @author inigosanchez
 * 
 */
class ConfigureOption {
  private static int TYPE_OPT_CHOOSE = 0;
  private static int TYPE_OPT_STRING = 1;
  private String askInfo, chooseString;
  private ArrayList<String> opt;
  private int choose, type;

  ConfigureOption(int typ, String info, ArrayList<String> options) {
    type = typ;
    askInfo = info;
    opt = options;
    choose = 0;
    chooseString = "";
  }

  /**
   * This function setChoose() set a choose numeric option.
   * 
   * @param num
   * @return boolean
   */
  boolean setChoose(int num) {
    if (num >= 0 && num < opt.size()) {
      choose = num;
      return true;
    } else {
      return false;
    }
  }
  /**
   * This function getMax() return number of numeric option.
   * 
   * @return int
   */
  int getMax() {
	  return opt.size();
  }

  /**
   * This function getChoose() return numeric option.
   * 
   * @return int
   */
  int getChoose() {
    return choose;
  }

  /**
   * This function setChooseString() set a choose string option.
   * 
   * @param String
   */
  void setChooseString(String line) {
    chooseString = line;
    if (type == TYPE_OPT_CHOOSE) {
      int i = 0;
      for (final String opts : opt) {
        if (opts.equals(chooseString)) {
          choose = i;
        }
        i++;
      }
    }
  }

  String getChooseString() {
    return chooseString;
  }

  int getType() {
    return type;
  }

  String getAskInfo() {
    return askInfo;
  }

  /**
   * Function getOptions(Project p) list options.
   */
  void getOptions(Project p) {
    // Choose options
    if (type == TYPE_OPT_CHOOSE) {
      int i = 0;
      for (final String opts : opt) {
        p.log("[" + i++ + "]" + opts);
      }
      p.log("\nPlease, choose an option [" + getChoose() + "]: ");
    } else if (type == TYPE_OPT_STRING) {
      p.log("\nPlease, introduce here: [" + getChooseString() + "]: ");
    }
  }

  /**
   * Function getOptionChoose() returns choose option.
   * 
   * @return option in String
   */
  String getOptionChoose() {
    String res = "";
    if (type == TYPE_OPT_CHOOSE) {
      int i = 0;
      for (final String opts : opt) {
        if (choose == i) {
          res = opts;
        }
        i++;
      }
    } else if (type == TYPE_OPT_STRING) {
      res = chooseString;
    }
    return res;
  }

}
