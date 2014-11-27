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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;

/**
 * ConfigurationApp configure Openbravo.properties. Besides creates some files from their respective
 * templates. If this process ends successfully and all the details are correct, the environment
 * will be ready for the ant install.source. *execute() method is invoke by ant setup task.
 * 
 * @author inigosanchez
 * 
 */
public class ConfigurationApp extends org.apache.tools.ant.Task {

  private static HashMap<Integer, ConfigureOption> optionLast = new HashMap<Integer, ConfigureOption>();
  private static HashMap<Integer, ConfigureOption> optionOracle = new HashMap<Integer, ConfigureOption>();
  private static HashMap<Integer, ConfigureOption> optionPostgreSQL = new HashMap<Integer, ConfigureOption>();
  private static HashMap<Integer, ConfigureOption> optionFirst = new HashMap<Integer, ConfigureOption>();
  private static HashMap<String, String> replaceProperties = new HashMap<String, String>();

  private final static String BASEDIR = System.getProperty("user.dir");
  private final static String BASEDIR_CONFIG = BASEDIR + "/config/";

  private final static String OPENBRAVO_PROPERTIES_TEMPLATE = BASEDIR_CONFIG
      + "Openbravo.properties.template";
  private final static String OPENBRAVO_PROPERTIES = BASEDIR_CONFIG + "Openbravo.properties";
  private final static String OPENBRAVO_PROPERTIES_AUX = BASEDIR_CONFIG
      + "Openbravo.properties.aux";
  private final static String FORMAT_XML_TEMPLATE = BASEDIR_CONFIG + "Format.xml.template";
  private final static String FORMAT_XML = BASEDIR_CONFIG + "Format.xml";
  private final static String LOG4J_LCF_TAMPLATE = BASEDIR_CONFIG + "log4j.lcf.template";
  private final static String LOG4J_LCF = BASEDIR_CONFIG + "log4j.lcf";
  private final static String USERCONFIG_XML_TEMPLATE = BASEDIR_CONFIG + "userconfig.xml.template";
  private final static String USERCONFIG_XML = BASEDIR_CONFIG + "userconfig.xml";
  private final static String COMMON_COMPONENT_TEMPLATE = ".settings/org.eclipse.wst.common.component.template";
  private final static String COMMON_COMPONENT = ".settings/org.eclipse.wst.common.component";
  private final static String CLASSPATH_TEMPLATE = ".classpath.template";
  private final static String CLASSPATH = ".classpath";

  private final static String OPENBRAVO_LICENSE = BASEDIR + "/legal/Licensing.txt";
  private static final int LINES_SHOWING_LICENSE = 50;

  public void execute() {
    Scanner licenseIn = new Scanner(System.in);
    Scanner inp = new Scanner(System.in);
    int option, menuOption = 0, optionMod = 0, numOptionsDDBB = 0, mainOption = 0, optionConfigure = 0;
    String optionString = "", optionS, input, menuOptionS;
    boolean numberOk, isChange, isChangeO, isChangeL, menuOptionOk;
    Project p = getProject();

    // Copy templates and rename files
    fileCopyTemplate(FORMAT_XML_TEMPLATE, FORMAT_XML, p);
    fileCopyTemplate(LOG4J_LCF_TAMPLATE, LOG4J_LCF, p);
    fileCopyTemplate(USERCONFIG_XML_TEMPLATE, USERCONFIG_XML, p);
    fileCopyTemplate(OPENBRAVO_PROPERTIES_TEMPLATE, OPENBRAVO_PROPERTIES, p);
    fileCopyTemplate(COMMON_COMPONENT_TEMPLATE, COMMON_COMPONENT, p);
    fileCopyTemplate(CLASSPATH_TEMPLATE, CLASSPATH, p);

    while (mainOption != -1) {
      switch (mainOption) {
      case 0:
        showWelcome(p);
        try {
          readLicense(p);
        } catch (IOException e) {
          e.printStackTrace();
        }
        p.log("Do you accept this license? [y/n]: ");
        input = licenseIn.nextLine();
        while (!("Y".equalsIgnoreCase(input) || "N".equalsIgnoreCase(input))) {
          p.log("Please, introduce a correct option. Do you accept this license? [y/n]: ");
          input = licenseIn.nextLine();
        }
        if ("Y".equalsIgnoreCase(input)) {
          mainOption++;
        } else if ("N".equalsIgnoreCase(input)) {
          p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
          mainOption = -1;
        }
        break;
      case 1:
        p.log("---------------------------------------------------------------------------- \n Please choose one option. \n----------------------------------------------------------------------------");
        p.log("[1]. Step-by-step configuration.");
        p.log("[2]. Default configuration.");
        p.log("[3]. Exit without saving.");
        p.log("Choose an option: ");
        mainOption++;
        break;
      case 2:
        menuOptionOk = false;
        do {
          menuOptionS = inp.nextLine();
          try {
            menuOption = Integer.parseInt(menuOptionS);
            menuOptionOk = true;
          } catch (NumberFormatException e) {
            p.log("Please, introduce a correct option: ");
          }
        } while (!menuOptionOk);
        // Create options one-by-one
        if (menuOption == 1) {
          if (optionFirst.isEmpty()) {
            optionFirst = createOP(p);
          }
          // Create optionsDDBB
          // Oracle or Postgresql options
          String keyValueS = "";
          Iterator<Integer> optionBBDD = optionFirst.keySet().iterator();
          do {
            keyValueS = optionFirst.get(optionBBDD.next()).getChooseString();
          } while (!(keyValueS.equals("PostgreSQL") || keyValueS.equals("Oracle")));
          if (keyValueS.equals("Oracle")) {
            optionOracle = createOPOracle(p);
            numOptionsDDBB = optionOracle.size();
          } else if (keyValueS.equals("PostgreSQL")) {
            optionPostgreSQL = createOPPostgreSQL(p);
            numOptionsDDBB = optionPostgreSQL.size();
          }
          mainOption = 3;
          // Create all options by default.
        } else if (menuOption == 2) {
          if (optionFirst.isEmpty()) {
            optionFirst = createOP(p);
          }
          // Oracle or Postgresql options
          String keyValue = "";
          Iterator<Integer> optionBBDD = optionFirst.keySet().iterator();
          do {
            keyValue = optionFirst.get(optionBBDD.next()).getChooseString();
          } while (!(keyValue.equals("PostgreSQL") || keyValue.equals("Oracle")));
          if (keyValue.equals("Oracle")) {
            optionOracle = createOPOracle(p);
            numOptionsDDBB = optionOracle.size();
          } else if (keyValue.equals("PostgreSQL")) {
            optionPostgreSQL = createOPPostgreSQL(p);
            numOptionsDDBB = optionPostgreSQL.size();
          }
          if (optionLast.isEmpty()) {
            optionLast = createOP2(p);
          }
          // Go to preview options configurate by default
          mainOption = 4;
        } else if (menuOption == 3) {
          mainOption = 22;
        } else {
          p.log("Please, introduce a correct option: ");
        }
        break;
      case 3:
        String typeDDBB = "";
        Map<Integer, ConfigureOption> treeMap = new TreeMap<Integer, ConfigureOption>(optionFirst);
        for (Map.Entry<Integer, ConfigureOption> entry : treeMap.entrySet()) {
          if (entry.getValue().getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
            p.log("Please select " + entry.getValue().getAskInfo());
            entry.getValue().getOptions(p);
            numberOk = false;
            do {
              optionS = inp.nextLine();
              try {
                option = Integer.parseInt(optionS);
                if (option >= 0 && option < entry.getValue().getMax()) {
                  entry.getValue().setChoose(option);
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              } catch (NumberFormatException e) {
                if (optionS.equals("")) {
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              }
            } while (!numberOk);
          } else if (entry.getValue().getType() == ConfigureOption.TYPE_OPT_STRING) {
            p.log("\nPlease introduce " + entry.getValue().getAskInfo());
            entry.getValue().getOptions(p);
            optionString = inp.nextLine();
            if (!optionString.equals("")) {
              entry.getValue().setChooseString(optionString);
            }
          }
          // review
          optionFirst.put(entry.getKey(), entry.getValue());
          typeDDBB = entry.getValue().getOptionChoose();
          p.log("\n-------------------------\nYour choice " + typeDDBB
              + "\n-------------------------\n\n");
        }
        // Select Oracle or PostgreSQL
        if (typeDDBB.equals("Oracle")) {
          if (optionOracle.isEmpty()) {
            optionOracle = createOPOracle(p);
            numOptionsDDBB = optionOracle.size();
          }
          if (!optionPostgreSQL.isEmpty()) {
            optionPostgreSQL.clear();
          }
          mainOption = 7;
        } else if (typeDDBB.equals("PostgreSQL")) {
          if (optionPostgreSQL.isEmpty()) {
            optionPostgreSQL = createOPPostgreSQL(p);
            numOptionsDDBB = optionPostgreSQL.size();
          }

          if (!optionOracle.isEmpty()) {
            optionOracle.clear();
          }
          mainOption = 8;
        }
        break;
      // Preview openBravo options
      case 4:
        p.log("---------------------------------------------------------------------------- \n Preview Openbravo ERP configuration \n----------------------------------------------------------------------------");
        // TreeMap for show questions in order for get user parameters.
        int numberOption = 1;
        Map<Integer, ConfigureOption> previewOptions1 = new TreeMap<Integer, ConfigureOption>(
            optionFirst);
        Map<Integer, ConfigureOption> previewOptions2;
        Map<Integer, ConfigureOption> previewOptions3 = new TreeMap<Integer, ConfigureOption>(
            optionLast);
        if (optionPostgreSQL.isEmpty()) {
          previewOptions2 = new TreeMap<Integer, ConfigureOption>(optionOracle);
        } else if (optionOracle.isEmpty()) {
          previewOptions2 = new TreeMap<Integer, ConfigureOption>(optionPostgreSQL);
        } else {
          previewOptions2 = new TreeMap<Integer, ConfigureOption>(optionPostgreSQL);
        }
        // Show all options by order asc
        for (Map.Entry<Integer, ConfigureOption> entry : previewOptions1.entrySet()) {
          p.log("[" + numberOption + "] " + entry.getValue().getAskInfo() + " "
              + entry.getValue().getOptionChoose());
          numberOption = numberOption + 1;
        }
        for (Map.Entry<Integer, ConfigureOption> entry : previewOptions2.entrySet()) {
          p.log("[" + numberOption + "] " + entry.getValue().getAskInfo() + " "
              + entry.getValue().getOptionChoose());
          numberOption = numberOption + 1;
        }
        for (Map.Entry<Integer, ConfigureOption> entry : previewOptions3.entrySet()) {
          p.log("[" + numberOption + "] " + entry.getValue().getAskInfo() + " "
              + entry.getValue().getOptionChoose());
          numberOption = numberOption + 1;
        }
        mainOption = 5;
        break;
      // It can be changed an option...
      case 5:
        p.log("---------------------------------------------------------------------------- \n Do you change any option? \n----------------------------------------------------------------------------");
        p.log("Choose [0] for continue with configuration or a number option for modify: ");
        menuOptionOk = false;
        do {
          menuOptionS = inp.nextLine();
          try {
            optionMod = Integer.parseInt(menuOptionS);
            if (optionMod >= 0
                && optionMod <= optionFirst.size() + optionLast.size() + numOptionsDDBB) {
              menuOptionOk = true;
            } else {
              p.log("Choose a real option: ");
            }
          } catch (NumberFormatException e) {
            p.log("Choose a real option: ");
          }
        } while (!menuOptionOk);
        // Accept all configuration
        if (optionMod == 0) {
          mainOption = 10;
          // Options 0 to numberLastOptions + NUM_OPTIONS_LAST, change a particular option
        } else if (optionMod > 0 && optionMod <= optionFirst.size()) {
          mainOption = 6;
        } else if (optionMod > optionFirst.size()
            && optionMod <= optionFirst.size() + numOptionsDDBB) {
          mainOption = 11;
        } else if (optionMod > optionFirst.size() + numOptionsDDBB
            && optionMod <= optionFirst.size() + optionLast.size() + numOptionsDDBB) {
          mainOption = 12;
        }
        break;
      // Change a option in optionFirst[]...
      case 6:
        int keyOpt = 0;
        isChange = false;
        Iterator<Integer> optionBBDD1 = optionFirst.keySet().iterator();
        while ((optionBBDD1.hasNext() && !isChange)) {
          keyOpt = optionBBDD1.next();
          if (keyOpt == optionMod - 1) {
            ConfigureOption optionChange = optionFirst.get(keyOpt);
            if (optionChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
              p.log("Please select " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              numberOk = false;
              do {
                optionS = inp.nextLine();
                try {
                  option = Integer.parseInt(optionS);
                  if (option >= 0 && option < optionChange.getMax()) {
                    optionChange.setChoose(option);
                    optionChange.setChooseString(optionChange.getOptionChoose());
                    numberOk = true;
                  } else {
                    p.log("Please, introduce a correct option: ");
                  }
                } catch (NumberFormatException e) {
                  if (optionS.equals("")) {
                    numberOk = true;
                  } else {
                    p.log("Please, introduce a correct option: ");
                  }
                }
              } while (!numberOk);
            } else if (optionChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
              p.log("\nPlease introduce " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              optionString = inp.nextLine();
              if (!optionString.equals("")) {
                optionChange.setChooseString(optionString);
              }
            }
            optionFirst.put(optionMod - 1, optionChange);
            p.log("\n-------------------------\nYour choice " + optionChange.getOptionChoose()
                + "\n-------------------------\n\n");
            isChange = true;
            // Check a change in type of database
            File fileO = new File(OPENBRAVO_PROPERTIES);
            if (optionChange.getOptionChoose().equals("Oracle")) {
              if (searchOptionsProperties(fileO, "bbdd.rdbms", p).equals("POSTGRE")) {
                if (optionOracle.isEmpty()) {
                  optionOracle = createOPOracle(p);
                  numOptionsDDBB = optionOracle.size();
                }
                if (!optionPostgreSQL.isEmpty()) {
                  optionPostgreSQL.clear();
                }
              }
            } else if (optionChange.getOptionChoose().equals("PostgreSQL")) {
              if (searchOptionsProperties(fileO, "bbdd.rdbms", p).equals("ORACLE")) {
                if (optionPostgreSQL.isEmpty()) {
                  optionPostgreSQL = createOPPostgreSQL(p);
                  numOptionsDDBB = optionPostgreSQL.size();
                }
                if (!optionOracle.isEmpty()) {
                  optionOracle.clear();
                }
              }
            }
          }
        }
        mainOption = 4;
        break;
      // Change ALL options in optionesOracle[]...
      case 7:
        Map<Integer, ConfigureOption> treeMapO = new TreeMap<Integer, ConfigureOption>(optionOracle);
        for (Map.Entry<Integer, ConfigureOption> entryO : treeMapO.entrySet()) {
          if (entryO.getValue().getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
            p.log("Please select " + entryO.getValue().getAskInfo());
            entryO.getValue().getOptions(p);
            numberOk = false;
            do {
              optionS = inp.nextLine();
              try {
                option = Integer.parseInt(optionS);
                if (option >= 0 && option < entryO.getValue().getMax()) {
                  entryO.getValue().setChoose(option);
                  entryO.getValue().setChooseString(entryO.getValue().getOptionChoose());
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              } catch (NumberFormatException e) {
                if (optionS.equals("")) {
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              }
            } while (!numberOk);
          } else if (entryO.getValue().getType() == ConfigureOption.TYPE_OPT_STRING) {
            p.log("\nPlease introduce " + entryO.getValue().getAskInfo());
            entryO.getValue().getOptions(p);
            optionString = inp.nextLine();
            if (!optionString.equals("")) {
              entryO.getValue().setChooseString(optionString);
            }
          }
          optionPostgreSQL.put(entryO.getKey(), entryO.getValue());
          p.log("\n-------------------------\nYour choice " + entryO.getValue().getOptionChoose()
              + "\n-------------------------\n\n");
        }
        // All information are introduced. Configure now last options
        if (optionLast.isEmpty()) {
          optionLast = createOP2(p);
        }
        mainOption = 9;
        break;
      // Change ALL optionS in optionesPostgreSQL[]...
      case 8:
        Map<Integer, ConfigureOption> treeMapP = new TreeMap<Integer, ConfigureOption>(
            optionPostgreSQL);
        for (Map.Entry<Integer, ConfigureOption> entryP : treeMapP.entrySet()) {
          if (entryP.getValue().getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
            p.log("Please select " + entryP.getValue().getAskInfo());
            entryP.getValue().getOptions(p);
            numberOk = false;
            do {
              optionS = inp.nextLine();
              try {
                option = Integer.parseInt(optionS);
                if (option >= 0 && option < entryP.getValue().getMax()) {
                  entryP.getValue().setChoose(option);
                  entryP.getValue().setChooseString(entryP.getValue().getOptionChoose());
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              } catch (NumberFormatException e) {
                if (optionS.equals("")) {
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              }
            } while (!numberOk);
          } else if (entryP.getValue().getType() == ConfigureOption.TYPE_OPT_STRING) {
            p.log("\nPlease introduce " + entryP.getValue().getAskInfo());
            entryP.getValue().getOptions(p);
            optionString = inp.nextLine();
            if (!optionString.equals("")) {
              entryP.getValue().setChooseString(optionString);
            }
          }
          optionPostgreSQL.put(entryP.getKey(), entryP.getValue());
          p.log("\n-------------------------\nYour choice " + entryP.getValue().getOptionChoose()
              + "\n-------------------------\n\n");
        }
        // All information are introduced. Configure now last options
        if (optionLast.isEmpty()) {
          optionLast = createOP2(p);
        }
        mainOption = 9;
        break;
      case 9:
        Map<Integer, ConfigureOption> treeMapL = new TreeMap<Integer, ConfigureOption>(optionLast);
        for (Map.Entry<Integer, ConfigureOption> entryL : treeMapL.entrySet()) {
          if (entryL.getValue().getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
            p.log("Please select " + entryL.getValue().getAskInfo());
            entryL.getValue().getOptions(p);
            numberOk = false;
            do {
              optionS = inp.nextLine();
              try {
                option = Integer.parseInt(optionS);
                if (option >= 0 && option < entryL.getValue().getMax()) {
                  entryL.getValue().setChoose(option);
                  entryL.getValue().setChooseString(entryL.getValue().getOptionChoose());
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              } catch (NumberFormatException e) {
                if (optionS.equals("")) {
                  numberOk = true;
                } else {
                  p.log("Please, introduce a correct option: ");
                }
              }
            } while (!numberOk);
          } else if (entryL.getValue().getType() == ConfigureOption.TYPE_OPT_STRING) {
            p.log("\nPlease introduce " + entryL.getValue().getAskInfo());
            entryL.getValue().getOptions(p);
            optionString = inp.nextLine();
            if (!optionString.equals("")) {
              entryL.getValue().setChooseString(optionString);
            }
          }
          optionLast.put(entryL.getKey(), entryL.getValue());
          p.log("\n-------------------------\nYour choice " + entryL.getValue().getOptionChoose()
              + "\n-------------------------\n\n");
        }
        mainOption = 10;
        break;
      case 10:
        p.log("---------------------------------------------------------------------------- \n Are you agree with all options that you configure? \n----------------------------------------------------------------------------");
        p.log("[1]. Accept.");
        p.log("[2]. Back to preview configuration.");
        p.log("[3]. Exit without saving.");
        p.log("Choose an option: ");
        menuOptionOk = false;
        do {
          menuOptionS = inp.nextLine();
          try {
            optionConfigure = Integer.parseInt(menuOptionS);
            menuOptionOk = true;
          } catch (NumberFormatException e) {
            p.log("Choose a real option: ");
          }
        } while (!menuOptionOk);
        switch (optionConfigure) {
        case 1:
          mainOption = 20;
          break;
        case 2:
          mainOption = 4;
          break;
        case 3:
          mainOption = 22;
          break;
        default:
          p.log("Choose a real option: ");
        }
        break;
      // Change a option in optionesOracle or optionesPostgreSQL...
      case 11:
        if (!optionOracle.isEmpty()) {
          int keyOracle = 0;
          isChangeO = false;
          Iterator<Integer> optionBBDDoracle = optionOracle.keySet().iterator();
          optionMod = optionMod - optionFirst.size();
          while ((optionBBDDoracle.hasNext() && !isChangeO)) {
            keyOracle = optionBBDDoracle.next();
            if (keyOracle == optionMod - 1) {
              ConfigureOption optionChange = optionOracle.get(keyOracle);
              if (optionChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
                p.log("Please select " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                numberOk = false;
                do {
                  optionS = inp.nextLine();
                  try {
                    option = Integer.parseInt(optionS);
                    if (option >= 0 && option < optionChange.getMax()) {
                      optionChange.setChoose(option);
                      optionChange.setChooseString(optionChange.getOptionChoose());
                      numberOk = true;
                    } else {
                      p.log("Please, introduce a correct option: ");
                    }
                  } catch (NumberFormatException e) {
                    if (optionS.equals("")) {
                      numberOk = true;
                    } else {
                      p.log("Please, introduce a correct option: ");
                    }
                  }
                } while (!numberOk);
              } else if (optionChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
                p.log("\nPlease introduce " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                optionString = inp.nextLine();
                if (!optionString.equals("")) {
                  optionChange.setChooseString(optionString);
                }
              }
              optionOracle.put(optionMod - 1, optionChange);
              p.log("\n-------------------------\nYour choice " + optionChange.getOptionChoose()
                  + "\n-------------------------\n\n");
              isChangeO = true;
            }
          }
        } else if (!optionPostgreSQL.isEmpty()) {
          int keyPostgre = 0;
          boolean isChangeP = false;
          Iterator<Integer> optionBBDDpostgre = optionPostgreSQL.keySet().iterator();
          optionMod = optionMod - optionFirst.size();
          while ((optionBBDDpostgre.hasNext() && !isChangeP)) {
            keyPostgre = optionBBDDpostgre.next();
            if (keyPostgre == optionMod - 1) {
              ConfigureOption optionChange = optionPostgreSQL.get(keyPostgre);
              if (optionChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
                p.log("Please select " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                numberOk = false;
                do {
                  optionS = inp.nextLine();
                  try {
                    option = Integer.parseInt(optionS);
                    if (option >= 0 && option < optionChange.getMax()) {
                      optionChange.setChoose(option);
                      optionChange.setChooseString(optionChange.getOptionChoose());
                      numberOk = true;
                    } else {
                      p.log("Please, introduce a correct option: ");
                    }
                  } catch (NumberFormatException e) {
                    if (optionS.equals("")) {
                      numberOk = true;
                    } else {
                      p.log("Please, introduce a correct option: ");
                    }
                  }
                } while (!numberOk);
              } else if (optionChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
                p.log("\nPlease introduce " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                optionString = inp.nextLine();
                if (!optionString.equals("")) {
                  optionChange.setChooseString(optionString);
                }
              }
              optionPostgreSQL.put(optionMod - 1, optionChange);
              p.log("\n-------------------------\nYour choice " + optionChange.getOptionChoose()
                  + "\n-------------------------\n\n");
              isChangeP = true;
            }
          }
        }
        mainOption = 4;
        break;
      // Change a option in optionesLast[]...
      case 12:
        int keyLast = 0;
        isChangeL = false;
        Iterator<Integer> optionBBDDlast = optionLast.keySet().iterator();
        optionMod = optionMod - optionFirst.size() - numOptionsDDBB;
        while ((optionBBDDlast.hasNext() && !isChangeL)) {
          keyLast = optionBBDDlast.next();
          if (keyLast == optionMod - 1) {
            ConfigureOption optionChange = optionLast.get(keyLast);
            if (optionChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
              p.log("Please select " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              numberOk = false;
              do {
                optionS = inp.nextLine();
                try {
                  option = Integer.parseInt(optionS);
                  if (option >= 0 && option < optionChange.getMax()) {
                    optionChange.setChoose(option);
                    optionChange.setChooseString(optionChange.getOptionChoose());
                    numberOk = true;
                  } else {
                    p.log("Please, introduce a correct option: ");
                  }
                } catch (NumberFormatException e) {
                  if (optionS.equals("")) {
                    numberOk = true;
                  } else {
                    p.log("Please, introduce a correct option: ");
                  }
                }
              } while (!numberOk);
            } else if (optionChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
              p.log("\nPlease introduce " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              optionString = inp.nextLine();
              if (!optionString.equals("")) {
                optionChange.setChooseString(optionString);
              }
            }
            optionLast.put(optionMod - 1, optionChange);
            p.log("\n-------------------------\nYour choice " + optionChange.getOptionChoose()
                + "\n-------------------------\n\n");
            isChangeL = true;
          }
        }
        mainOption = 4;
        break;
      // All options have been selected... configure Openbravo.properties file.
      case 20:
        setValuesProperties();
        Iterator<String> keySetIterator = replaceProperties.keySet().iterator();
        while (keySetIterator.hasNext()) {
          String keyForFile = keySetIterator.next();
          replaceOptionsProperties(keyForFile + "=", replaceProperties.get(keyForFile), p);
        }
        mainOption = 21;
        break;
      // configuration done
      case 21:
        p.log("---------------------------------------------------------------------------- \n Configuration complete. \n----------------------------------------------------------------------------");
        mainOption = -1;
        break;
      // re-confirm exit
      case 22:
        p.log("Do you want to exit this program? [y/n]: ");
        input = licenseIn.nextLine();
        while (!("Y".equalsIgnoreCase(input) || "N".equalsIgnoreCase(input))) {
          p.log("Please, introduce a correct option. Do you want to exit this program? [y/n]: ");
          input = licenseIn.nextLine();
        }
        if ("Y".equalsIgnoreCase(input)) {
          p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
          mainOption = -1;
        } else if ("N".equalsIgnoreCase(input)) {
          if (optionFirst.isEmpty()) {
            mainOption = 1;
          } else {
            mainOption = 10;
          }
        }
      }
    }
    inp.close();
    licenseIn.close();
    p.log("---------------------------------------------------------------------------- \n Thanks for use Openbravo ERP Setup. \n----------------------------------------------------------------------------");
  }

  /**
   * This function showWelcome() show a welcome to install application.
   * 
   * @param p1
   *          : Project
   */
  private static void showWelcome(Project p1) {
    Scanner inp = new Scanner(System.in);
    p1.log("---------------------------------------------------------------------------- \n Welcome to the Openbravo ERP Setup Wizard. \n----------------------------------------------------------------------------");
    p1.log("Please read the following License Agreement. You must accept the terms of this\n agreement before continuing with the installation.");
    p1.log("Press [Enter] to continue:");
    inp.nextLine();
  }

  /**
   * This function setValuesProperties() use all information asking to user for configurate
   * Openbravo.properties file.
   */
  private static void setValuesProperties() {

    String timeSeparator = "", dateSeparator = "", timeFormat = "", dateFormat = "", database = "";
    // Get important data for building all the options.
    Map<Integer, ConfigureOption> treeMapSet = new TreeMap<Integer, ConfigureOption>(optionFirst);
    for (Map.Entry<Integer, ConfigureOption> entrySet : treeMapSet.entrySet()) {
      if (entrySet.getValue().getAskInfo().equals("date separator: ")) {
        dateSeparator = entrySet.getValue().getOptionChoose();
      } else if (entrySet.getValue().getAskInfo().equals("time separator: ")) {
        timeSeparator = entrySet.getValue().getOptionChoose();
      } else if (entrySet.getValue().getAskInfo().equals("date format: ")) {
        dateFormat = entrySet.getValue().getOptionChoose();
      } else if (entrySet.getValue().getAskInfo().equals("time format: ")) {
        timeFormat = entrySet.getValue().getOptionChoose();
      } else if (entrySet.getValue().getAskInfo().equals("Database:")) {
        database = entrySet.getValue().getOptionChoose();
      }
    }
    replaceProperties.put("source.path", System.getProperty("user.dir"));

    treeMapSet = new TreeMap<Integer, ConfigureOption>(optionFirst);
    for (Map.Entry<Integer, ConfigureOption> entry : treeMapSet.entrySet()) {
      if (entry.getValue().getAskInfo().equals("Attachments directory: ")) {
        replaceProperties.put("attach.path", entry.getValue().getOptionChoose());
      } else if (entry.getValue().getAskInfo().equals("Context name: ")) {
        replaceProperties.put("context.name", entry.getValue().getOptionChoose());
      } else if (entry.getValue().getAskInfo().equals("Web URL: ")) {
        replaceProperties.put("web.url", entry.getValue().getOptionChoose());
      } else if (entry.getValue().getAskInfo().equals("Output script location: ")) {
        replaceProperties.put("bbdd.outputscript", entry.getValue().getOptionChoose());
      } else if (entry.getValue().getAskInfo().equals("Context URL :")) {
        replaceProperties.put("context.url", entry.getValue().getOptionChoose());
      }
    }
    // dateFormat.java
    if (dateFormat.substring(0, 1).equals("D")) {
      replaceProperties
          .put("dateFormat.java", "dd" + dateSeparator + "MM" + dateSeparator + "yyyy");
    } else if (dateFormat.substring(0, 1).equals("M")) {
      replaceProperties
          .put("dateFormat.java", "MM" + dateSeparator + "dd" + dateSeparator + "yyyy");
    } else if (dateFormat.substring(0, 1).equals("Y")) {
      replaceProperties
          .put("dateFormat.java", "yyyy" + dateSeparator + "MM" + dateSeparator + "dd");
    }
    // dateTimeFormat.java
    if (timeFormat.equals("12h")) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put("dateTimeFormat.java", "dd" + dateSeparator + "MM" + dateSeparator
            + "yyyy hh" + timeSeparator + "mm" + timeSeparator + "ss a");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put("dateTimeFormat.java", "MM" + dateSeparator + "dd" + dateSeparator
            + "yyyy hh" + timeSeparator + "mm" + timeSeparator + "ss a");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put("dateTimeFormat.java", "yyyy" + dateSeparator + "MM" + dateSeparator
            + "dd hh" + timeSeparator + "mm" + timeSeparator + "ss a");
      }
    } else if (timeFormat.equals("24h")) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put("dateTimeFormat.java", "dd" + dateSeparator + "MM" + dateSeparator
            + "yyyy HH" + timeSeparator + "mm" + timeSeparator + "ss");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put("dateTimeFormat.java", "MM" + dateSeparator + "dd" + dateSeparator
            + "yyyy HH" + timeSeparator + "mm" + timeSeparator + "ss");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put("dateTimeFormat.java", "yyyy" + dateSeparator + "MM" + dateSeparator
            + "dd HH" + timeSeparator + "mm" + timeSeparator + "ss");
      }
    }
    // dateFormat.sql
    if (dateFormat.substring(0, 1).equals("D")) {
      replaceProperties.put("dateFormat.sql", "DD" + dateSeparator + "MM" + dateSeparator + "YYYY");
    } else if (dateFormat.substring(0, 1).equals("M")) {
      replaceProperties.put("dateFormat.sql", "MM" + dateSeparator + "DD" + dateSeparator + "YYYY");
    } else if (dateFormat.substring(0, 1).equals("Y")) {
      replaceProperties.put("dateFormat.sql", "YYYY" + dateSeparator + "MM" + dateSeparator + "DD");
    }
    // "dateFormat.js"
    if (dateFormat.substring(0, 1).equals("D")) {
      replaceProperties.put("dateFormat.js", "%d" + dateSeparator + "%m" + dateSeparator + "%Y");
    } else if (dateFormat.substring(0, 1).equals("M")) {
      replaceProperties.put("dateFormat.js", "%m" + dateSeparator + "%d" + dateSeparator + "%Y");
    } else if (dateFormat.substring(0, 1).equals("Y")) {
      replaceProperties.put("dateFormat.js", "%Y" + dateSeparator + "%m" + dateSeparator + "%d");
    }
    // dateTimeFormat.sql
    replaceProperties.put("dateTimeFormat.sql", "DD-MM-YYYY HH24:MI:SS");
    // bbdd.sessionConfig and BBDD Oracle
    if (database.equals("Oracle")) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put("bbdd.sessionConfig", "ALTER SESSION SET NLS_DATE_FORMAT='DD"
            + dateSeparator + "MM" + dateSeparator + "YYYY' NLS_NUMERIC_CHARACTERS='.,'");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put("bbdd.sessionConfig", "ALTER SESSION SET NLS_DATE_FORMAT='MM"
            + dateSeparator + "DD" + dateSeparator + "YYYY' NLS_NUMERIC_CHARACTERS='.,'");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put("bbdd.sessionConfig", "ALTER SESSION SET NLS_DATE_FORMAT='YYYY"
            + dateSeparator + "MM" + dateSeparator + "DD' NLS_NUMERIC_CHARACTERS='.,'");
      }
      String nameBBDD = "", serverBBDD = "", portBBDD = "";
      treeMapSet = new TreeMap<Integer, ConfigureOption>(optionOracle);
      for (Map.Entry<Integer, ConfigureOption> entryO : treeMapSet.entrySet()) {
        if (entryO.getValue().getAskInfo().equals("SID: ")) {
          nameBBDD = entryO.getValue().getOptionChoose();
          replaceProperties.put("bbdd.sid", nameBBDD);
        } else if (entryO.getValue().getAskInfo().equals("System User: ")) {
          replaceProperties.put("bbdd.systemUser", entryO.getValue().getOptionChoose());
        } else if (entryO.getValue().getAskInfo().equals("System Password: ")) {
          replaceProperties.put("bbdd.systemPassword", entryO.getValue().getOptionChoose());
        } else if (entryO.getValue().getAskInfo().equals("DB User: ")) {
          replaceProperties.put("bbdd.user", entryO.getValue().getOptionChoose());
        } else if (entryO.getValue().getAskInfo().equals("DB User Password: ")) {
          replaceProperties.put("bbdd.password", entryO.getValue().getOptionChoose());
        } else if (entryO.getValue().getAskInfo().equals("DB Server Address: ")) {
          serverBBDD = entryO.getValue().getOptionChoose();
        } else if (entryO.getValue().getAskInfo().equals("DB Server Port: ")) {
          portBBDD = entryO.getValue().getOptionChoose();
        }
      }
      replaceProperties.put("bbdd.rdbms", "ORACLE");
      replaceProperties.put("bbdd.driver", "oracle.jdbc.driver.OracleDriver");
      replaceProperties.put("bbdd.url", "jdbc:oracle:thin:@" + serverBBDD + ":" + portBBDD + ":"
          + nameBBDD);
      // bbdd.sessionConfig and BBDD PostgreSQL
    } else if (database.equals("PostgreSQL")) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put("bbdd.sessionConfig", "select update_dateFormat('DD" + dateSeparator
            + "MM" + dateSeparator + "YYYY')");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put("bbdd.sessionConfig", "select update_dateFormat('MM" + dateSeparator
            + "DD" + dateSeparator + "YYYY')");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put("bbdd.sessionConfig", "select update_dateFormat('YYYY"
            + dateSeparator + "MM" + dateSeparator + "DD')");
      }
      String serverBBDD = "", portBBDD = "";
      treeMapSet = new TreeMap<Integer, ConfigureOption>(optionPostgreSQL);
      for (Map.Entry<Integer, ConfigureOption> entryP : treeMapSet.entrySet()) {
        if (entryP.getValue().getAskInfo().equals("SID: ")) {
          replaceProperties.put("bbdd.sid", entryP.getValue().getOptionChoose());
        } else if (entryP.getValue().getAskInfo().equals("System User: ")) {
          replaceProperties.put("bbdd.systemUser", entryP.getValue().getOptionChoose());
        } else if (entryP.getValue().getAskInfo().equals("System Password: ")) {
          replaceProperties.put("bbdd.systemPassword", entryP.getValue().getOptionChoose());
        } else if (entryP.getValue().getAskInfo().equals("DB User: ")) {
          replaceProperties.put("bbdd.user", entryP.getValue().getOptionChoose());
        } else if (entryP.getValue().getAskInfo().equals("DB User Password: ")) {
          replaceProperties.put("bbdd.password", entryP.getValue().getOptionChoose());
        } else if (entryP.getValue().getAskInfo().equals("DB Server Address: ")) {
          serverBBDD = entryP.getValue().getOptionChoose();
        } else if (entryP.getValue().getAskInfo().equals("DB Server Port: ")) {
          portBBDD = entryP.getValue().getOptionChoose();
        }
      }
      replaceProperties.put("bbdd.rdbms", "POSTGRE");
      replaceProperties.put("bbdd.driver", "org.postgresql.Driver");
      replaceProperties.put("bbdd.url", "jdbc:postgresql://" + serverBBDD + ":" + portBBDD);
    }
    treeMapSet = new TreeMap<Integer, ConfigureOption>(optionLast);
    for (Map.Entry<Integer, ConfigureOption> entryL : treeMapSet.entrySet()) {
      if (entryL.getValue().getAskInfo().equals("Tomcat Manager URL: ")) {
        replaceProperties.put("tomcat.manager.url", entryL.getValue().getOptionChoose());
      } else if (entryL.getValue().getAskInfo().equals("Tomcat manager username: ")) {
        replaceProperties.put("tomcat.manager.username", entryL.getValue().getOptionChoose());
      } else if (entryL.getValue().getAskInfo().equals("Tomcat manager password: ")) {
        replaceProperties.put("tomcat.manager.password", entryL.getValue().getOptionChoose());
      } else if (entryL.getValue().getAskInfo().equals("Authentication class: ")) {
        replaceProperties.put("authentication.class", entryL.getValue().getOptionChoose());
      }
    }
  }

  /**
   * This function replace in Openbravo.properties the value of option searchOption with value
   * changeOption. Concatenated searchOption+changeOption. For example: "bbdd.user=" + "admin".
   * 
   * @param searchOption
   *          : prefix to search
   * @param changeOption
   *          : value to write in Openbravo.properties
   */
  private static void replaceOptionsProperties(String searchOption, String changeOption, Project p) {
    try {
      boolean isFound = false;
      File fileR = new File(OPENBRAVO_PROPERTIES);
      FileReader fr = new FileReader(fileR);
      BufferedReader br = new BufferedReader(fr);
      // auxiliary file to rewrite
      File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
      FileWriter fw = new FileWriter(fileW);
      // data for restore
      String line;
      while ((line = br.readLine()) != null) {
        if (line.indexOf(searchOption) == 0) {
          // Replace new option
          line = line.replace(line, searchOption + changeOption);
          isFound = true;
        }
        fw.write(line + "\n");
      }
      if (!isFound) {
        fw.write(searchOption + changeOption);
      }
      fr.close();
      fw.close();
      br.close();
    } catch (Exception e1) {
      p.log("Excetion reading/writing file: " + e1);
    }
    // Second part: Delete Openbravo.properties and rename Openbravo.properties.aux to
    // Openbravo.properties
    try {
      File fileR = new File(OPENBRAVO_PROPERTIES);
      fileR.delete();
      File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
      fileW.renameTo(new File(OPENBRAVO_PROPERTIES));
    } catch (Exception e2) {
      p.log("Excetion deleting/rename file: " + e2);
    }
  }

  /**
   * This function SearchOptionsProperties() search an option in fileO file and returns the value of
   * searchOption.
   * 
   * @param fileO
   * @param searchOption
   * @return String
   */
  private static String searchOptionsProperties(File fileO, String searchOption, Project p) {
    String valueSearched = "";
    try {
      FileReader fr = new FileReader(fileO);
      BufferedReader br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        if (line.indexOf(searchOption) == 0) {
          valueSearched = line.substring(searchOption.length() + 1);
          break;
        }
      }
      fr.close();
      br.close();
    } catch (Exception e1) {
      p.log("Excetion reading/writing file: " + e1);
    }
    return valueSearched;
  }

  /**
   * This function createOP() create first options for configuration. Information is collected from
   * Openbravo.properties file.
   * 
   * @return HashMap<Integer,Option>
   */
  private static HashMap<Integer, ConfigureOption> createOP(Project p) {
    HashMap<Integer, ConfigureOption> options = new HashMap<Integer, ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);

    String askInfo = "date format: ";
    ArrayList<String> optChoosen = new ArrayList<String>();
    optChoosen.add("DDMMYYYY");
    optChoosen.add("MMDDYYYY");
    optChoosen.add("YYYYMMDD");
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    String compareDateformat = searchOptionsProperties(fileO, "dateFormat.sql", p).substring(0, 1);
    if (compareDateformat.equalsIgnoreCase("d")) {
      o0.setChooseString("DDMMYYYY");
    } else if (compareDateformat.equalsIgnoreCase("m")) {
      o0.setChooseString("MMDDYYYY");
    } else if (compareDateformat.equalsIgnoreCase("y")) {
      o0.setChooseString("YYYYMMDD");
    }
    options.put(0, o0);

    askInfo = "date separator: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add("-");
    optChoosen.add("/");
    optChoosen.add(".");
    optChoosen.add(":");
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    compareDateformat = searchOptionsProperties(fileO, "dateTimeFormat.sql", p).substring(0, 9);
    if (compareDateformat.contains("-")) {
      o1.setChooseString("-");
    } else if (compareDateformat.contains("/")) {
      o1.setChooseString("/");
    } else if (compareDateformat.contains(".")) {
      o1.setChooseString(".");
    } else if (compareDateformat.contains(":")) {
      o1.setChooseString(":");
    } else {
      o1.setChooseString("-");
    }
    options.put(1, o1);

    askInfo = "time format: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add("12h");
    optChoosen.add("24h");
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);

    if (searchOptionsProperties(fileO, "dateTimeFormat.java", p).contains("a")) {
      o2.setChooseString("12h");
    } else {
      o2.setChooseString("24h");
    }
    options.put(2, o2);

    askInfo = "time separator: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add(":");
    optChoosen.add(".");
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    compareDateformat = searchOptionsProperties(fileO, "dateTimeFormat.sql", p).substring(10);
    if (compareDateformat.contains(":")) {
      o3.setChooseString(":");
    } else if (compareDateformat.contains(".")) {
      o3.setChooseString(".");
    } else {
      o3.setChooseString(":");
    }
    options.put(3, o3);

    askInfo = "Attachments directory: ";
    ConfigureOption o4 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    String optionValueString = searchOptionsProperties(fileO, "attach.path", p);
    if (optionValueString.equals("")) {
      o4.setChooseString("/opt/openbravo/attachments");
    } else {
      o4.setChooseString(optionValueString);
    }
    options.put(4, o4);

    askInfo = "Context name: ";
    ConfigureOption o5 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "context.name", p);
    if (optionValueString.equals("")) {
      o5.setChooseString("openbravo");
    } else {
      o5.setChooseString(optionValueString);
    }
    options.put(5, o5);

    askInfo = "Web URL: ";
    ConfigureOption o6 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "web.url", p);
    if (optionValueString.equals("")) {
      o6.setChooseString("@actual_url_context@/web");
    } else {
      o6.setChooseString(optionValueString);
    }
    options.put(6, o6);

    askInfo = "Context URL :";
    ConfigureOption o7 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "context.url", p);
    if (optionValueString.equals("")) {
      o7.setChooseString("http://localhost:8080/openbravo");
    } else {
      o7.setChooseString(optionValueString);
    }
    options.put(7, o7);

    askInfo = "Output script location: ";
    ConfigureOption o8 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "bbdd.outputscript", p);
    if (optionValueString.equals("")) {
      o8.setChooseString("databasescript.sql");
    } else {
      o8.setChooseString(optionValueString);
    }
    options.put(8, o8);

    askInfo = "Database:";
    optChoosen = new ArrayList<String>();
    optChoosen.add("Oracle");
    optChoosen.add("PostgreSQL");
    ConfigureOption o9 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    if (searchOptionsProperties(fileO, "bbdd.rdbms", p).equals("ORACLE")) {
      o9.setChooseString("Oracle");
    } else if (searchOptionsProperties(fileO, "bbdd.rdbms", p).equals("POSTGRE")) {
      o9.setChooseString("PostgreSQL");
    } else {
      o9.setChooseString("PostgreSQL");
    }
    options.put(9, o9);

    return options;
  }

  /**
   * 
   * This function createOP2() create last options for configuration.Information is collected from
   * Openbravo.properties file.
   * 
   * @return HashMap<Integer, Option>
   */
  private static HashMap<Integer, ConfigureOption> createOP2(Project p) {
    HashMap<Integer, ConfigureOption> options = new HashMap<Integer, ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);

    String askInfo = "Tomcat Manager URL: ";
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    String optionValueString = searchOptionsProperties(fileO, "tomcat.manager.url", p);
    if (optionValueString.equals("")) {
      o0.setChooseString("http://localhost:8080/manager");
    } else {
      o0.setChooseString(optionValueString);
    }
    options.put(0, o0);

    askInfo = "Tomcat manager username: ";
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "tomcat.manager.username", p);
    if (optionValueString.equals("")) {
      o1.setChooseString("admin");
    } else {
      o1.setChooseString(optionValueString);
    }
    options.put(1, o1);

    askInfo = "Tomcat manager password: ";
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "tomcat.manager.password", p);
    if (optionValueString.equals("")) {
      o2.setChooseString("admin");
    } else {
      o2.setChooseString(optionValueString);
    }
    options.put(2, o2);

    askInfo = "Authentication class: ";
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "authentication.class", p);
    if (optionValueString.equals("")) {
      o3.setChooseString("");
    } else {
      o3.setChooseString(optionValueString);
    }
    options.put(3, o3);

    return options;
  }

  /**
   * This function createOPOracle() create options of Oracle configuration. Information is collected
   * from Openbravo.properties file.
   * 
   * @return HashMap<Integer, Option>
   */
  private static HashMap<Integer, ConfigureOption> createOPOracle(Project p) {
    HashMap<Integer, ConfigureOption> option = new HashMap<Integer, ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);
    // Modify Openbravo.properties file if Oracle's options have been disabled.
    if (searchOptionsProperties(fileO, "bbdd.rdbms", p).equals("POSTGRE")) {
      changeOraclePostgresql(p);
    }

    String askInfo = "SID: ";
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    String optionValueString = searchOptionsProperties(fileO, "bbdd.sid", p);
    if (optionValueString.equals("")) {
      o0.setChooseString("xe");
    } else {
      o0.setChooseString(optionValueString);
    }
    option.put(0, o0);

    askInfo = "System User: ";
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "bbdd.systemUser", p);
    if (optionValueString.equals("")) {
      o1.setChooseString("SYSTEM");
    } else {
      o1.setChooseString(optionValueString);
    }
    option.put(1, o1);

    askInfo = "System Password: ";
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "bbdd.systemPassword", p);
    if (optionValueString.equals("")) {
      o2.setChooseString("SYSTEM");
    } else {
      o2.setChooseString(optionValueString);
    }
    option.put(2, o2);

    askInfo = "DB User: ";
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "bbdd.user", p);
    if (optionValueString.equals("")) {
      o3.setChooseString("TAD");
    } else {
      o3.setChooseString(optionValueString);
    }
    option.put(3, o3);

    askInfo = "DB User Password: ";
    ConfigureOption o4 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, "bbdd.password", p);
    if (optionValueString.equals("")) {
      o4.setChooseString("TAD");
    } else {
      o4.setChooseString(optionValueString);
    }
    option.put(4, o4);

    String separateString = searchOptionsProperties(fileO, "bbdd.url", p);
    if (separateString.equals("")) {
      separateString = "jdbc:oracle:thin:@localhost:1521:xe";
    }
    String[] separateUrl = separateString.split(":");

    askInfo = "DB Server Address: ";
    ConfigureOption o5 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o5.setChooseString(separateUrl[3].substring(1));
    option.put(5, o5);

    askInfo = "DB Server Port: ";
    ConfigureOption o6 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o6.setChooseString(separateUrl[4]);
    option.put(6, o6);

    return option;
  }

  /**
   * This function createOPPostgreSQL() create options of PostgreSQL configuration.Information is
   * collected from Openbravo.properties file.
   * 
   * @return HashMap<Integer, Option>
   */
  private static HashMap<Integer, ConfigureOption> createOPPostgreSQL(Project p) {

    HashMap<Integer, ConfigureOption> option = new HashMap<Integer, ConfigureOption>();
    String askInfo;
    File fileO = new File(OPENBRAVO_PROPERTIES);
    // Modify Openbravo.properties file if PostgreSQL's options have been disabled.
    if (searchOptionsProperties(fileO, "bbdd.rdbms", p).equals("ORACLE")) {
      changeOraclePostgresql(p);
    }

    askInfo = "SID: ";
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o0.setChooseString(searchOptionsProperties(fileO, "bbdd.sid", p));
    option.put(0, o0);

    askInfo = "System User: ";
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o1.setChooseString(searchOptionsProperties(fileO, "bbdd.systemUser", p));
    option.put(1, o1);

    askInfo = "System Password: ";
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o2.setChooseString(searchOptionsProperties(fileO, "bbdd.systemPassword", p));
    option.put(2, o2);

    askInfo = "DB User: ";
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o3.setChooseString(searchOptionsProperties(fileO, "bbdd.user", p));
    option.put(3, o3);

    askInfo = "DB User Password: ";
    ConfigureOption o4 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o4.setChooseString(searchOptionsProperties(fileO, "bbdd.password", p));
    option.put(4, o4);

    String separateString = searchOptionsProperties(fileO, "bbdd.url", p);
    String[] separateUrl = separateString.split(":");

    askInfo = "DB Server Address: ";
    ConfigureOption o5 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o5.setChooseString(separateUrl[2].substring(2));
    option.put(5, o5);

    askInfo = "DB Server Port: ";
    ConfigureOption o6 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o6.setChooseString(separateUrl[3]);
    option.put(6, o6);

    return option;
  }

  /**
   * This function changeOraclePostgresql() disabling options Oracle or PostgreSQL, using the [#] at
   * the beginning of the options to disable them.
   * 
   */
  private static void changeOraclePostgresql(Project p) {
    try {
      File fileR = new File(OPENBRAVO_PROPERTIES);
      FileReader fr = new FileReader(fileR);
      BufferedReader br = new BufferedReader(fr);
      // Auxiliary file to rewrite
      File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
      FileWriter fw = new FileWriter(fileW);
      String line;
      while ((line = br.readLine()) != null) {
        // Searching bbdd.xxx and add/delete "#" symbol.
        if (line.indexOf("bbdd.") == 0) {
          // Not considering the following options: bbdd.outputscript and bbdd.verbosity because are
          // always.
          if (!(line.contains("bbdd.verbosity") || line.contains("bbdd.outputscript"))) {
            line = line.replace(line, "# " + line);
          }
        } else if (line.indexOf("bbdd.") == 2) {
          line = line.replace(line, line.substring(2));
        }
        fw.write(line + "\n");
      }
      fr.close();
      fw.close();
      br.close();
    } catch (Exception e1) {
      p.log("Excetion reading/writing file: " + e1);
    }
    // Second part: Delete Openbravo.properties and rename Openbravo.properties.aux to
    // Openbravo.properties
    try {
      File fileR = new File(OPENBRAVO_PROPERTIES);
      fileR.delete();
      File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
      fileW.renameTo(new File(OPENBRAVO_PROPERTIES));
    } catch (Exception e2) {
      p.log("Excetion deleting/rename file: " + e2);
    }
  }

  /**
   * This function FileCopyTemplate() copy a file if it is not exists.
   * 
   * @param sourceFile
   * @param destinationFile
   */
  private static void fileCopyTemplate(String sourceFile, String destinationFile, Project p) {
    try {
      File inFile = new File(sourceFile);
      File outFile = new File(destinationFile);
      if (!outFile.exists()) {
        FileUtils.copyFile(inFile, outFile);
      }
    } catch (IOException e) {
      p.log("Error in in/out in FileCopyTemplate.");
    }
  }

  /**
   * This function readLicense() show license terms for installing OpenBravo. License is located in
   * OPENBRAVO_LICENSE.
   * 
   * @throws IOException
   */
  private static void readLicense(Project p) throws IOException {
    File license = null;
    FileReader fr = null;
    BufferedReader br = null;
    Scanner in = new Scanner(System.in);
    int lineConsole = 0;

    try {
      license = new File(OPENBRAVO_LICENSE);
      fr = new FileReader(license);
      br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        p.log(line);
        if (lineConsole == LINES_SHOWING_LICENSE) {
          p.log("Press [Enter] to continue:");
          in.nextLine();
          lineConsole = 0;
        }
        lineConsole++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        fr.close();
        br.close();
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
  }
}