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

  private static int TYPE_OPT_CHOOSE = 0;
  private static int TYPE_OPT_STRING = 1;

  private static HashMap<Integer, ConfigureOption> optionesLast = new HashMap<Integer, ConfigureOption>();
  private static HashMap<Integer, ConfigureOption> optionesOracle = new HashMap<Integer, ConfigureOption>();
  private static HashMap<Integer, ConfigureOption> optionesPostgreSQL = new HashMap<Integer, ConfigureOption>();
  private static HashMap<Integer, ConfigureOption> optionesFirst = new HashMap<Integer, ConfigureOption>();
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

  private final static String OPENBRAVO_LICENSE = BASEDIR + "/legal/Openbravo_license.txt";
  private static final int LINES_SHOWING_LICENSE = 15;

  public void execute() {
    Scanner licenseIn = new Scanner(System.in);
    Scanner inp = new Scanner(System.in);
    int opcion = 0, menuOption, optionMod = 0;
    String opcionString = "";
    String input;
    int mainOption = 0;
    Project p = getProject();
    // Copy templates and rename files
    FileCopyTemplate(FORMAT_XML_TEMPLATE, FORMAT_XML);
    FileCopyTemplate(LOG4J_LCF_TAMPLATE, LOG4J_LCF);
    FileCopyTemplate(USERCONFIG_XML_TEMPLATE, USERCONFIG_XML);
    FileCopyTemplate(OPENBRAVO_PROPERTIES_TEMPLATE, OPENBRAVO_PROPERTIES);
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
        while (!(input.equals("y") || input.equals("n") || input.equals("Y") || input.equals("N"))) {
          p.log("Please, introduce a correct option. Do you accept this license? [y/n]: ");
          input = licenseIn.nextLine();
        }
        if (input.equals("y") || input.equals("Y")) {
          mainOption++;
        } else if (input.equals("n") || input.equals("N")) {
          p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
          mainOption = -1;
        }
        break;
      case 1:
        p.log("---------------------------------------------------------------------------- \n Please choose one option. \n----------------------------------------------------------------------------");
        p.log("[1]. Step-by-step configuration.");
        p.log("[2]. Default configuration.");
        p.log("[3]. Exit");
        mainOption++;
        break;
      case 2:
        p.log("Choose an option: ");
        menuOption = inp.nextInt();
        inp.nextLine();
        // Create options one-by-one
        if (menuOption == 1) {
          if (optionesFirst.isEmpty()) {
            optionesFirst = createOP();
          }
          mainOption = 3;
          // Create all options by default.
        } else if (menuOption == 2) {
          if (optionesFirst.isEmpty()) {
            optionesFirst = createOP();
          }
          // Oracle or Postgresql options
          String keyValue = "";
          Iterator<Integer> optionBBDD = optionesFirst.keySet().iterator();
          do {
            keyValue = optionesFirst.get(optionBBDD.next()).getChooseString();
          } while (!(keyValue.equals("PostgreSQL") || keyValue.equals("Oracle")));
          if (keyValue.equals("Oracle")) {
            optionesOracle = createOPOracle();
          } else if (keyValue.equals("PostgreSQL")) {
            optionesPostgreSQL = createOPPostgreSQL();
          }
          if (optionesLast.isEmpty()) {
            optionesLast = createOP2();
          }
          // Go to preview options configurate by default
          mainOption = 4;
        } else if (menuOption == 3) {
          p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
          mainOption = -1;
        }
        break;
      case 3:
        String typeDDBB = "";
        Map<Integer, ConfigureOption> treeMap = new TreeMap<Integer, ConfigureOption>(optionesFirst);
        for (Map.Entry<Integer, ConfigureOption> entry : treeMap.entrySet()) {
          if (entry.getValue().getType() == TYPE_OPT_CHOOSE) {
            p.log("Please select " + entry.getValue().getAskInfo());
            entry.getValue().getOptions(p);
            opcion = inp.nextInt();
            while (!entry.getValue().setChoose(opcion)) {
              p.log("Please, introduce a correct option: ");
              opcion = inp.nextInt();
            }
            inp.nextLine();
            // REVIEW falta poner algo******
          } else if (entry.getValue().getType() == TYPE_OPT_STRING) {
            p.log("Please introduce " + entry.getValue().getAskInfo());
            entry.getValue().getOptions(p);
            opcionString = inp.nextLine();
            entry.getValue().setChooseString(opcionString);
          }
          optionesFirst.put(entry.getKey(), entry.getValue());
          typeDDBB = entry.getValue().getOptionChoose();
          p.log("\n-----\n\nYour choose " + typeDDBB + "\n-----\n");
        }
        // Select Oracle or PostgreSQL
        if (typeDDBB.equals("Oracle")) {
          if (optionesOracle.isEmpty()) {
            optionesOracle = createOPOracle();
          }
          if (!optionesPostgreSQL.isEmpty()) {
            optionesPostgreSQL.clear();
          }
          mainOption = 7;
        } else if (typeDDBB.equals("PostgreSQL")) {
          if (optionesPostgreSQL.isEmpty()) {
            optionesPostgreSQL = createOPPostgreSQL();
          }

          if (!optionesOracle.isEmpty()) {
            optionesOracle.clear();
          }
          mainOption = 8;
        }
        break;
      // Preview openBravo options
      case 4:
        p.log("---------------------------------------------------------------------------- \n Preview Openbravo ERP configuration \n----------------------------------------------------------------------------");
        // TreeMap for show questions in order for get user parameters.
        int numberOption = 1;
        Map<Integer, ConfigureOption> previewOptions1 = new TreeMap<Integer, ConfigureOption>(optionesFirst);
        Map<Integer, ConfigureOption> previewOptions2;
        Map<Integer, ConfigureOption> previewOptions3 = new TreeMap<Integer, ConfigureOption>(optionesLast);
        if (optionesPostgreSQL.isEmpty()) {
          previewOptions2 = new TreeMap<Integer, ConfigureOption>(optionesOracle);
        } else if (optionesOracle.isEmpty()) {
          previewOptions2 = new TreeMap<Integer, ConfigureOption>(optionesPostgreSQL);
        } else {
          previewOptions2 = new TreeMap<Integer, ConfigureOption>(optionesPostgreSQL);
        }
        // Show all options by order asc
        for (Map.Entry<Integer, ConfigureOption> entry : previewOptions1.entrySet()) {
          p.log("[" + numberOption + "] " + entry.getValue().getAskInfo() + " "
              + entry.getValue().getChooseString());
          numberOption = numberOption + 1;
        }
        for (Map.Entry<Integer, ConfigureOption> entry : previewOptions2.entrySet()) {
          p.log("[" + numberOption + "] " + entry.getValue().getAskInfo() + " "
              + entry.getValue().getChooseString());
          numberOption = numberOption + 1;
        }
        for (Map.Entry<Integer, ConfigureOption> entry : previewOptions3.entrySet()) {
          p.log("[" + numberOption + "] " + entry.getValue().getAskInfo() + " "
              + entry.getValue().getChooseString());
          numberOption = numberOption + 1;
        }
        mainOption = 5;
        break;
      // It can be changed an option...
      case 5:
        p.log("---------------------------------------------------------------------------- \n Do you change any option? \n----------------------------------------------------------------------------");
        p.log("Choose [0] for continue with configuration or a number option for modify: ");
        optionMod = inp.nextInt();
        // Accept all configuration
        if (optionMod == 0) {
          mainOption = 10;
          // Options 0 to numberLastOptions + NUM_OPTIONS_LAST, change a particular option
        } else if (optionMod > 0 && optionMod <= optionesFirst.size()) {
          mainOption = 6;
        } else if (optionMod > optionesFirst.size()
            && optionMod <= optionesFirst.size() + optionesLast.size()) {
          mainOption = 11;
        } else if (optionMod > optionesFirst.size() + optionesLast.size()
            && optionMod <= optionesFirst.size() + optionesLast.size() + optionesOracle.size()) {
          mainOption = 12;
          // Choose a real option
        } else {
          while (optionMod < 0
              && optionMod > optionesFirst.size() + optionesLast.size() + optionesOracle.size()) {
            p.log("Choose a real option: ");
            optionMod = inp.nextInt();
          }
        }
        inp.nextLine();
        break;
      // Change a option in optionesFirst[]...
      case 6:
        int keyOpt = 0;
        boolean isChange = false;
        Iterator<Integer> optionBBDD1 = optionesFirst.keySet().iterator();
        while ((optionBBDD1.hasNext() && !isChange)) {
          keyOpt = optionBBDD1.next();
          if (keyOpt == optionMod - 1) {
            ConfigureOption optionChange = optionesFirst.get(keyOpt);
            if (optionChange.getType() == TYPE_OPT_CHOOSE) {
              p.log("Please select " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              opcion = inp.nextInt();
              while (!optionChange.setChoose(opcion)) {
                p.log("Please, introduce a correct option: ");
                opcion = inp.nextInt();
              }
              inp.nextLine();
              optionChange.setChooseString(optionChange.getOptionChoose());
            } else if (optionChange.getType() == TYPE_OPT_STRING) {
              p.log("Please introduce " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              opcionString = inp.nextLine();
              optionChange.setChooseString(opcionString);
            }
            optionesFirst.put(optionMod - 1, optionChange);
            p.log("\n-----\n\nYour choose " + optionChange.getOptionChoose() + "\n-----\n");
            isChange = true;
            // Check a change in type of database
            File fileO = new File(OPENBRAVO_PROPERTIES);
            if (optionChange.getOptionChoose().equals("Oracle")) {
              if (SearchOptionsProperties(fileO, "bbdd.rdbms").equals("POSTGRE")) {
                if (optionesOracle.isEmpty()) {
                  optionesOracle = createOPOracle();
                }
                if (!optionesPostgreSQL.isEmpty()) {
                  optionesPostgreSQL.clear();
                }
              }
            } else if (optionChange.getOptionChoose().equals("PostgreSQL")) {
              if (SearchOptionsProperties(fileO, "bbdd.rdbms").equals("ORACLE")) {
                if (optionesPostgreSQL.isEmpty()) {
                  optionesPostgreSQL = createOPPostgreSQL();
                }
                if (!optionesOracle.isEmpty()) {
                  optionesOracle.clear();
                }
              }
            }
          }
        }
        mainOption = 4;
        break;
      // Change ALL options in optionesOracle[]...
      case 7:
        Map<Integer, ConfigureOption> treeMapO = new TreeMap<Integer, ConfigureOption>(optionesOracle);
        for (Map.Entry<Integer, ConfigureOption> entryO : treeMapO.entrySet()) {
          if (entryO.getValue().getType() == TYPE_OPT_CHOOSE) {
            p.log("Please select " + entryO.getValue().getAskInfo());
            entryO.getValue().getOptions(p);
            opcion = inp.nextInt();
            while (!entryO.getValue().setChoose(opcion)) {
              p.log("Please, introduce a correct option: ");
              opcion = inp.nextInt();
            }
            inp.nextLine();
          } else if (entryO.getValue().getType() == TYPE_OPT_STRING) {
            p.log("Please introduce " + entryO.getValue().getAskInfo());
            entryO.getValue().getOptions(p);
            opcionString = inp.nextLine();
            entryO.getValue().setChooseString(opcionString);
          }
          optionesPostgreSQL.put(entryO.getKey(), entryO.getValue());
          p.log("\n-----\n\nYour choose " + entryO.getValue().getOptionChoose() + "\n-----\n");
        }
        // All information are introduced. Configure now last options
        if (optionesLast.isEmpty()) {
          optionesLast = createOP2();
        }
        mainOption = 9;
        break;
      // Change ALL optionS in optionesPostgreSQL[]...
      case 8:
        Map<Integer, ConfigureOption> treeMapP = new TreeMap<Integer, ConfigureOption>(optionesPostgreSQL);
        for (Map.Entry<Integer, ConfigureOption> entryP : treeMapP.entrySet()) {
          if (entryP.getValue().getType() == TYPE_OPT_CHOOSE) {
            p.log("Please select " + entryP.getValue().getAskInfo());
            entryP.getValue().getOptions(p);
            opcion = inp.nextInt();
            while (!entryP.getValue().setChoose(opcion)) {
              p.log("Please, introduce a correct option: ");
              opcion = inp.nextInt();
            }
            inp.nextLine();
          } else if (entryP.getValue().getType() == TYPE_OPT_STRING) {
            p.log("Please introduce " + entryP.getValue().getAskInfo());
            entryP.getValue().getOptions(p);
            opcionString = inp.nextLine();
            entryP.getValue().setChooseString(opcionString);
          }
          optionesPostgreSQL.put(entryP.getKey(), entryP.getValue());
          p.log("\n-----\n\nYour choose " + entryP.getValue().getOptionChoose() + "\n-----\n");
        }
        // All information are introduced. Configure now last options
        if (optionesLast.isEmpty()) {
          optionesLast = createOP2();
        }
        mainOption = 9;
        break;
      case 9:
        Map<Integer, ConfigureOption> treeMapL = new TreeMap<Integer, ConfigureOption>(optionesLast);
        for (Map.Entry<Integer, ConfigureOption> entryL : treeMapL.entrySet()) {
          if (entryL.getValue().getType() == TYPE_OPT_CHOOSE) {
            p.log("Please select " + entryL.getValue().getAskInfo());
            entryL.getValue().getOptions(p);
            opcion = inp.nextInt();
            while (!entryL.getValue().setChoose(opcion)) {
              p.log("Please, introduce a correct option: ");
              opcion = inp.nextInt();
            }
            inp.nextLine();
          } else if (entryL.getValue().getType() == TYPE_OPT_STRING) {
            p.log("Please introduce " + entryL.getValue().getAskInfo());
            entryL.getValue().getOptions(p);
            opcionString = inp.nextLine();
            entryL.getValue().setChooseString(opcionString);
          }
          optionesLast.put(entryL.getKey(), entryL.getValue());
          p.log("\n-----\n\nYour choose " + entryL.getValue().getOptionChoose() + "\n-----\n");
        }
        mainOption = 10;
        break;
      case 10:
        p.log("---------------------------------------------------------------------------- \n Are you agree with all options that you configure? \n----------------------------------------------------------------------------");
        p.log("[1]. Accept");
        p.log("[2]. Back to preview configuration.");
        p.log("[3]. Exit");
        p.log("Choose an option: ");
        int optionConfigure = inp.nextInt();
        inp.nextLine();
        switch (optionConfigure) {
        case 1:
          mainOption = 20;
          break;
        case 2:
          mainOption = 4;
          break;
        case 3:
          p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
          mainOption = -1;
          break;
        default:
          p.log("Choose a real option: ");
        }
        break;
      // Change a option in optionesOracle or optionesPostgreSQL...
      case 11:
        if (!optionesOracle.isEmpty()) {
          int keyOracle = 0;
          boolean isChangeO = false;
          Iterator<Integer> optionBBDDoracle = optionesOracle.keySet().iterator();
          optionMod = optionMod - optionesFirst.size();
          while ((optionBBDDoracle.hasNext() && !isChangeO)) {
            keyOracle = optionBBDDoracle.next();
            if (keyOracle == optionMod - 1) {
              ConfigureOption optionChange = optionesOracle.get(keyOracle);
              if (optionChange.getType() == TYPE_OPT_CHOOSE) {
                p.log("Please select " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                opcion = inp.nextInt();
                while (!optionChange.setChoose(opcion)) {
                  p.log("Please, introduce a correct option: ");
                  opcion = inp.nextInt();
                }
                inp.nextLine();
                optionChange.setChooseString(optionChange.getOptionChoose());
              } else if (optionChange.getType() == TYPE_OPT_STRING) {
                p.log("Please introduce " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                opcionString = inp.nextLine();
                optionChange.setChooseString(opcionString);
              }
              optionesOracle.put(optionMod - 1, optionChange);
              p.log("\n-----\n\nYour choose " + optionChange.getOptionChoose() + "\n-----\n");
              isChangeO = true;
            }
          }
        } else if (!optionesPostgreSQL.isEmpty()) {
          int keyPostgre = 0;
          boolean isChangeP = false;
          Iterator<Integer> optionBBDDpostgre = optionesPostgreSQL.keySet().iterator();
          optionMod = optionMod - optionesFirst.size();
          while ((optionBBDDpostgre.hasNext() && !isChangeP)) {
            keyPostgre = optionBBDDpostgre.next();
            if (keyPostgre == optionMod - 1) {
              ConfigureOption optionChange = optionesPostgreSQL.get(keyPostgre);
              if (optionChange.getType() == TYPE_OPT_CHOOSE) {
                p.log("Please select " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                opcion = inp.nextInt();
                while (!optionChange.setChoose(opcion)) {
                  p.log("Please, introduce a correct option: ");
                  opcion = inp.nextInt();
                }
                inp.nextLine();
                optionChange.setChooseString(optionChange.getOptionChoose());
              } else if (optionChange.getType() == TYPE_OPT_STRING) {
                p.log("Please introduce " + optionChange.getAskInfo());
                optionChange.getOptions(p);
                opcionString = inp.nextLine();
                optionChange.setChooseString(opcionString);
              }
              optionesPostgreSQL.put(optionMod - 1, optionChange);
              p.log("\n-----\n\nYour choose " + optionChange.getOptionChoose() + "\n-----\n");
              isChangeP = true;
            }
          }
        }
        mainOption = 4;
        break;
      // Change a option in optionesLast[]...
      case 12:
        int keyLast = 0;
        boolean isChangeL = false;
        Iterator<Integer> optionBBDDlast = optionesLast.keySet().iterator();
        optionMod = optionMod - optionesFirst.size() - optionesPostgreSQL.size();
        while ((optionBBDDlast.hasNext() && !isChangeL)) {
          keyLast = optionBBDDlast.next();
          if (keyLast == optionMod - 1) {
            ConfigureOption optionChange = optionesLast.get(keyLast);
            if (optionChange.getType() == TYPE_OPT_CHOOSE) {
              p.log("Please select " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              opcion = inp.nextInt();
              while (!optionChange.setChoose(opcion)) {
                p.log("Please, introduce a correct option: ");
                opcion = inp.nextInt();
              }
              inp.nextLine();
              optionChange.setChooseString(optionChange.getOptionChoose());
            } else if (optionChange.getType() == TYPE_OPT_STRING) {
              p.log("Please introduce " + optionChange.getAskInfo());
              optionChange.getOptions(p);
              opcionString = inp.nextLine();
              optionChange.setChooseString(opcionString);
            }
            optionesLast.put(optionMod - 1, optionChange);
            p.log("\n-----\n\nYour choose " + optionChange.getOptionChoose() + "\n-----\n");
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
          ReplaceOptionsProperties(keyForFile + "=", replaceProperties.get(keyForFile));
        }
        mainOption = 21;
        break;
      case 21:
        p.log("---------------------------------------------------------------------------- \n Configuration complete. \n----------------------------------------------------------------------------");
        mainOption = -1;
        break;
      }
    }
    p.log("---------------------------------------------------------------------------- \n Thanks for use Openbravo ERP Setup. \n----------------------------------------------------------------------------");
  }

  /**
   * This function showWelcome() whow a welcome to install application.
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
    Map<Integer, ConfigureOption> treeMapSet = new TreeMap<Integer, ConfigureOption>(optionesFirst);
    for (Map.Entry<Integer, ConfigureOption> entrySet : treeMapSet.entrySet()) {
      if (entrySet.getValue().getAskInfo().equals("date separator: ")) {
        dateSeparator = entrySet.getValue().getChooseString();
      } else if (entrySet.getValue().getAskInfo().equals("time separator: ")) {// 3
        timeSeparator = entrySet.getValue().getChooseString();
      } else if (entrySet.getValue().getAskInfo().equals("date format: ")) {// 0
        dateFormat = entrySet.getValue().getChooseString();
      } else if (entrySet.getValue().getAskInfo().equals("time format: ")) {// 2
        timeFormat = entrySet.getValue().getChooseString();
      } else if (entrySet.getValue().getAskInfo().equals("a database: ")) {// 10
        database = entrySet.getValue().getChooseString();
      }
    }
    replaceProperties.put("source.path", System.getProperty("user.dir"));

    treeMapSet = new TreeMap<Integer, ConfigureOption>(optionesFirst);
    for (Map.Entry<Integer, ConfigureOption> entry : treeMapSet.entrySet()) {
      if (entry.getValue().getAskInfo().equals("Attachments directory: ")) {
        replaceProperties.put("attach.path", entry.getValue().getChooseString());
      } else if (entry.getValue().getAskInfo().equals("Context name: ")) {
        replaceProperties.put("context.name", entry.getValue().getChooseString());
      } else if (entry.getValue().getAskInfo().equals("Web URL: ")) {
        replaceProperties.put("web.url", entry.getValue().getChooseString());
      } else if (entry.getValue().getAskInfo().equals("Output script location: ")) {
        replaceProperties.put("bbdd.outputscript", entry.getValue().getChooseString());
      } else if (entry.getValue().getAskInfo().equals("DB-XML operations log verbosity: ")) {
        replaceProperties.put("bbdd.verbosity", entry.getValue().getOptionChoose());
      } else if (entry.getValue().getAskInfo().equals("Context URL :")) {
        replaceProperties.put("context.url", entry.getValue().getChooseString());
      } else if (entry.getValue().getAskInfo().equals("")) {

      } else if (entry.getValue().getAskInfo().equals("")) {

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
            + "yyyy KK" + timeSeparator + "mm" + timeSeparator + "ss a");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put("dateTimeFormat.java", "MM" + dateSeparator + "dd" + dateSeparator
            + "yyyy KK" + timeSeparator + "mm" + timeSeparator + "ss a");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put("dateTimeFormat.java", "yyyy" + dateSeparator + "MM" + dateSeparator
            + "dd KK" + timeSeparator + "mm" + timeSeparator + "ss a");
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
      treeMapSet = new TreeMap<Integer, ConfigureOption>(optionesOracle);
      for (Map.Entry<Integer, ConfigureOption> entryO : treeMapSet.entrySet()) {
        if (entryO.getValue().getAskInfo().equals("SID: ")) {
          nameBBDD = entryO.getValue().getChooseString();
          replaceProperties.put("bbdd.sid", nameBBDD);
        } else if (entryO.getValue().getAskInfo().equals("System User: ")) {
          replaceProperties.put("bbdd.systemUser", entryO.getValue().getChooseString());
        } else if (entryO.getValue().getAskInfo().equals("System Password: ")) {
          replaceProperties.put("bbdd.systemPassword", entryO.getValue().getChooseString());
        } else if (entryO.getValue().getAskInfo().equals("DB User: ")) {
          replaceProperties.put("bbdd.user", entryO.getValue().getChooseString());
        } else if (entryO.getValue().getAskInfo().equals("DB User Password: ")) {
          replaceProperties.put("bbdd.password", entryO.getValue().getChooseString());
        } else if (entryO.getValue().getAskInfo().equals("DB Server Address: ")) {// 5
          serverBBDD = entryO.getValue().getChooseString();
        } else if (entryO.getValue().getAskInfo().equals("DB Server Port: ")) {// 6
          portBBDD = entryO.getValue().getChooseString();
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
      treeMapSet = new TreeMap<Integer, ConfigureOption>(optionesPostgreSQL);
      for (Map.Entry<Integer, ConfigureOption> entryP : treeMapSet.entrySet()) {
        if (entryP.getValue().getAskInfo().equals("SID: ")) {
          replaceProperties.put("bbdd.sid", entryP.getValue().getChooseString());
        } else if (entryP.getValue().getAskInfo().equals("System User: ")) {
          replaceProperties.put("bbdd.systemUser", entryP.getValue().getChooseString());
        } else if (entryP.getValue().getAskInfo().equals("System Password: ")) {
          replaceProperties.put("bbdd.systemPassword", entryP.getValue().getChooseString());
        } else if (entryP.getValue().getAskInfo().equals("DB User: ")) {
          replaceProperties.put("bbdd.user", entryP.getValue().getChooseString());
        } else if (entryP.getValue().getAskInfo().equals("DB User Password: ")) {
          replaceProperties.put("bbdd.password", entryP.getValue().getChooseString());
        } else if (entryP.getValue().getAskInfo().equals("DB Server Address: ")) {// 5
          serverBBDD = entryP.getValue().getChooseString();
        } else if (entryP.getValue().getAskInfo().equals("DB Server Port: ")) {// 6
          portBBDD = entryP.getValue().getChooseString();
        }
      }
      replaceProperties.put("bbdd.rdbms", "POSTGRE");
      replaceProperties.put("bbdd.driver", "org.postgresql.Driver");
      replaceProperties.put("bbdd.url", "jdbc:postgresql://" + serverBBDD + ":" + portBBDD);
    }
    treeMapSet = new TreeMap<Integer, ConfigureOption>(optionesLast);
    for (Map.Entry<Integer, ConfigureOption> entryL : treeMapSet.entrySet()) {
      if (entryL.getValue().getAskInfo().equals("Tomcat Manager URL: ")) {
        replaceProperties.put("tomcat.manager.url", entryL.getValue().getChooseString());
      } else if (entryL.getValue().getAskInfo().equals("Tomcat manager username: ")) {
        replaceProperties.put("tomcat.manager.username", entryL.getValue().getChooseString());
      } else if (entryL.getValue().getAskInfo().equals("Tomcat manager password: ")) {
        replaceProperties.put("tomcat.manager.password", entryL.getValue().getChooseString());
      } else if (entryL.getValue().getAskInfo().equals("Pentaho Server: ")) {
        replaceProperties.put("pentahoServer.js", entryL.getValue().getChooseString());
      } else if (entryL.getValue().getAskInfo().equals("Authentication class: ")) {
        replaceProperties.put("authentication.class", entryL.getValue().getChooseString());
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
  private static void ReplaceOptionsProperties(String searchOption, String changeOption) {
    try {
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
        }
        fw.write(line + "\n");
      }
      fr.close();
      fw.close();
      br.close();
    } catch (Exception e1) {
      System.out.println("Excetion reading/writing file: " + e1);
    }
    // Second part: Delete Openbravo.properties and rename Openbravo.properties.aux to
    // Openbravo.properties
    try {
      File fileR = new File(OPENBRAVO_PROPERTIES);
      fileR.delete();
      File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
      fileW.renameTo(new File(OPENBRAVO_PROPERTIES));
    } catch (Exception e2) {
      System.out.println("Excetion deleting/rename file: " + e2);
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
  private static String SearchOptionsProperties(File fileO, String searchOption) {
    String valueSearched = "null";
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
      System.out.println("Excetion reading/writing file: " + e1);
    }
    return valueSearched;
  }

  /**
   * This function createOP() create first options for configuration. Information is collected from
   * Openbravo.properties file.
   * 
   * @return HashMap<Integer,Option>
   */
  private static HashMap<Integer, ConfigureOption> createOP() {
    HashMap<Integer, ConfigureOption> options = new HashMap<Integer, ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);

    String askInfo = "date format: ";
    ArrayList<String> optChoosen = new ArrayList<String>();
    optChoosen.add("DDMMYYYY");
    optChoosen.add("MMDDYYYY");
    optChoosen.add("YYYYMMDD");
    ConfigureOption o0 = new ConfigureOption(TYPE_OPT_CHOOSE, askInfo, optChoosen);
    String compareDateformat = SearchOptionsProperties(fileO, "dateFormat.sql").substring(0, 1);
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
    ConfigureOption o1 = new ConfigureOption(TYPE_OPT_CHOOSE, askInfo, optChoosen);
    compareDateformat = SearchOptionsProperties(fileO, "dateTimeFormat.sql").substring(0, 9);
    if (compareDateformat.contains("-")) {
      o1.setChooseString("-");
    } else if (compareDateformat.contains("/")) {
      o1.setChooseString("/");
    } else if (compareDateformat.contains(".")) {
      o1.setChooseString(".");
    } else if (compareDateformat.contains(":")) {
      o1.setChooseString(":");
    }
    options.put(1, o1);

    askInfo = "time format: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add("12h");
    optChoosen.add("24h");
    ConfigureOption o2 = new ConfigureOption(TYPE_OPT_CHOOSE, askInfo, optChoosen);

    if (SearchOptionsProperties(fileO, "dateTimeFormat.sql").contains("12")) {
      o2.setChooseString("12h");
    } else if (SearchOptionsProperties(fileO, "dateTimeFormat.sql").contains("24")) {
      o2.setChooseString("24h");
    }
    options.put(2, o2);

    askInfo = "time separator: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add(":");
    optChoosen.add(".");
    ConfigureOption o3 = new ConfigureOption(TYPE_OPT_CHOOSE, askInfo, optChoosen);
    compareDateformat = SearchOptionsProperties(fileO, "dateTimeFormat.sql").substring(10);
    if (compareDateformat.contains(":")) {
      o3.setChooseString(":");
    } else if (compareDateformat.contains(".")) {
      o3.setChooseString(".");
    }
    options.put(3, o3);

    askInfo = "Attachments directory: ";
    ConfigureOption o4 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o4.setChooseString(SearchOptionsProperties(fileO, "attach.path"));
    options.put(4, o4);

    askInfo = "Context name: ";
    ConfigureOption o5 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o5.setChooseString(SearchOptionsProperties(fileO, "context.name"));
    options.put(5, o5);

    askInfo = "Web URL: ";
    ConfigureOption o6 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o6.setChooseString(SearchOptionsProperties(fileO, "web.url"));
    options.put(6, o6);

    askInfo = "Context URL :";
    ConfigureOption o7 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o7.setChooseString(SearchOptionsProperties(fileO, "context.url"));
    options.put(7, o7);

    askInfo = "Output script location: ";
    ConfigureOption o8 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o8.setChooseString(SearchOptionsProperties(fileO, "bbdd.outputscript"));
    options.put(8, o8);

    askInfo = "DB-XML operations log verbosity: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add("DEBUG");
    optChoosen.add("INFO");
    optChoosen.add("WARN");
    optChoosen.add("ERROR");
    optChoosen.add("FATAL");
    ConfigureOption o9 = new ConfigureOption(TYPE_OPT_CHOOSE, askInfo, optChoosen);
    o9.setChooseString(SearchOptionsProperties(fileO, "bbdd.verbosity"));
    options.put(9, o9);

    askInfo = "a database: ";
    optChoosen = new ArrayList<String>();
    optChoosen.add("Oracle");
    optChoosen.add("PostgreSQL");
    ConfigureOption o10 = new ConfigureOption(TYPE_OPT_CHOOSE, askInfo, optChoosen);
    if (SearchOptionsProperties(fileO, "bbdd.rdbms").equals("ORACLE")) {
      o10.setChooseString("Oracle");
    } else if (SearchOptionsProperties(fileO, "bbdd.rdbms").equals("POSTGRE")) {
      o10.setChooseString("PostgreSQL");
    }
    options.put(10, o10);

    return options;
  }

  /**
   * 
   * This function createOP2() create last options for configuration.Information is collected from
   * Openbravo.properties file.
   * 
   * @return HashMap<Integer, Option>
   */
  private static HashMap<Integer, ConfigureOption> createOP2() {
    HashMap<Integer, ConfigureOption> options = new HashMap<Integer, ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);

    String askInfo = "Tomcat Manager URL: ";
    ConfigureOption o0 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o0.setChooseString(SearchOptionsProperties(fileO, "tomcat.manager.url"));
    options.put(0, o0);

    askInfo = "Tomcat manager username: ";
    ConfigureOption o1 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o1.setChooseString(SearchOptionsProperties(fileO, "tomcat.manager.username"));
    options.put(1, o1);

    askInfo = "Tomcat manager password: ";
    ConfigureOption o2 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o2.setChooseString(SearchOptionsProperties(fileO, "tomcat.manager.password"));
    options.put(2, o2);

    askInfo = "Pentaho Server: ";
    ConfigureOption o3 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o3.setChooseString(SearchOptionsProperties(fileO, "pentahoServer"));
    options.put(3, o3);

    askInfo = "Authentication class: ";
    ConfigureOption o4 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o4.setChooseString(SearchOptionsProperties(fileO, "authentication.class"));
    options.put(4, o4);

    return options;
  }

  /**
   * This function createOPOracle() create options of Oracle configuration. Information is collected
   * from Openbravo.properties file.
   * 
   * @return HashMap<Integer, Option>
   */
  private static HashMap<Integer, ConfigureOption> createOPOracle() {
    HashMap<Integer, ConfigureOption> option = new HashMap<Integer, ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);
    // Modify Openbravo.properties file if Oracle's options have been disabled.
    if (SearchOptionsProperties(fileO, "bbdd.rdbms").equals("POSTGRE")) {
      changeOraclePostgresql();
    }

    String askInfo = "SID: ";
    ConfigureOption o0 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o0.setChooseString(SearchOptionsProperties(fileO, "bbdd.sid"));
    option.put(0, o0);

    askInfo = "System User: ";
    ConfigureOption o1 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o1.setChooseString(SearchOptionsProperties(fileO, "bbdd.systemUser"));
    option.put(1, o1);

    askInfo = "System Password: ";
    ConfigureOption o2 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o2.setChooseString(SearchOptionsProperties(fileO, "bbdd.systemPassword"));
    option.put(2, o2);

    askInfo = "DB User: ";
    ConfigureOption o3 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o3.setChooseString(SearchOptionsProperties(fileO, "bbdd.user"));
    option.put(3, o3);

    askInfo = "DB User Password: ";
    ConfigureOption o4 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o4.setChooseString(SearchOptionsProperties(fileO, "bbdd.password"));
    option.put(4, o4);

    String separateString = SearchOptionsProperties(fileO, "bbdd.url");
    String[] separateUrl = separateString.split(":");

    askInfo = "DB Server Address: ";
    ConfigureOption o5 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o5.setChooseString(separateUrl[3].substring(1));
    option.put(5, o5);

    askInfo = "DB Server Port: ";
    ConfigureOption o6 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
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
  private static HashMap<Integer, ConfigureOption> createOPPostgreSQL() {

    HashMap<Integer, ConfigureOption> option = new HashMap<Integer, ConfigureOption>();
    String askInfo;
    File fileO = new File(OPENBRAVO_PROPERTIES);
    // Modify Openbravo.properties file if PostgreSQL's options have been disabled.
    if (SearchOptionsProperties(fileO, "bbdd.rdbms").equals("ORACLE")) {
      changeOraclePostgresql();
    }

    askInfo = "SID: ";
    ConfigureOption o0 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o0.setChooseString(SearchOptionsProperties(fileO, "bbdd.sid"));
    option.put(0, o0);

    askInfo = "System User: ";
    ConfigureOption o1 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o1.setChooseString(SearchOptionsProperties(fileO, "bbdd.systemUser"));
    option.put(1, o1);

    askInfo = "System Password: ";
    ConfigureOption o2 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o2.setChooseString(SearchOptionsProperties(fileO, "bbdd.systemPassword"));
    option.put(2, o2);

    askInfo = "DB User: ";
    ConfigureOption o3 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o3.setChooseString(SearchOptionsProperties(fileO, "bbdd.user"));
    option.put(3, o3);

    askInfo = "DB User Password: ";
    ConfigureOption o4 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o4.setChooseString(SearchOptionsProperties(fileO, "bbdd.password"));
    option.put(4, o4);

    String separateString = SearchOptionsProperties(fileO, "bbdd.url");
    String[] separateUrl = separateString.split(":");

    askInfo = "DB Server Address: ";
    ConfigureOption o5 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o5.setChooseString(separateUrl[2].substring(2));
    option.put(5, o5);

    askInfo = "DB Server Port: ";
    ConfigureOption o6 = new ConfigureOption(TYPE_OPT_STRING, askInfo, new ArrayList<String>());
    o6.setChooseString(separateUrl[3]);
    option.put(6, o6);

    return option;
  }

  /**
   * This function changeOraclePostgresql() disabling options Oracle or PostgreSQL, using the [#] at
   * the beginning of the options to disable them.
   * 
   */
  private static void changeOraclePostgresql() {
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
      System.out.println("Excetion reading/writing file: " + e1);
    }
    // Second part: Delete Openbravo.properties and rename Openbravo.properties.aux to
    // Openbravo.properties
    try {
      File fileR = new File(OPENBRAVO_PROPERTIES);
      fileR.delete();
      File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
      fileW.renameTo(new File(OPENBRAVO_PROPERTIES));
    } catch (Exception e2) {
      System.out.println("Excetion deleting/rename file: " + e2);
    }
  }

  /**
   * This function FileCopyTemplate() copy a file if it is not exists.
   * 
   * @param sourceFile
   * @param destinationFile
   */
  private static void FileCopyTemplate(String sourceFile, String destinationFile) {
    try {
      File inFile = new File(sourceFile);
      File outFile = new File(destinationFile);
      if (!outFile.exists()) {
        FileUtils.copyFile(inFile, outFile);
      }
    } catch (IOException e) {
      System.out.println("Error in in/out in FileCopyTemplate.");
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
        if (fr != null) {
          fr.close();
        }
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
  }
}