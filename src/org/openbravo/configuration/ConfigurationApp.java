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
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
  private static List<ConfigureOption> optionOracle = new ArrayList<ConfigureOption>();
  private static List<ConfigureOption> optionPostgreSQL = new ArrayList<ConfigureOption>();
  private static List<ConfigureOption> optionForOpenbravo = new ArrayList<ConfigureOption>();
  private static Map<String, String> replaceProperties = new HashMap<String, String>();

  private final static String BASEDIR = System.getProperty("user.dir");
  private final static String BASEDIR_CONFIG = BASEDIR + "/config/";
  private final static String OPENBRAVO_PROPERTIES = BASEDIR_CONFIG + "Openbravo.properties";
  private final static String OPENBRAVO_PROPERTIES_AUX = BASEDIR_CONFIG
      + "Openbravo.properties.aux";
  private final static String FORMAT_XML = BASEDIR_CONFIG + "Format.xml";
  private final static String LOG4J_LCF = BASEDIR_CONFIG + "log4j.lcf";
  private final static String USERCONFIG_XML = BASEDIR_CONFIG + "userconfig.xml";
  private final static String COMMON_COMPONENT = ".settings/org.eclipse.wst.common.component";
  private final static String CLASSPATH = ".classpath";
  private final static String OPENBRAVO_LICENSE = BASEDIR + "/legal/Licensing.txt";
  private final static int LINES_SHOWING_LICENSE = 50;

  private final static int WELCOME = 0;
  private final static int MAIN_MENU = 1;
  private final static int SELECT_OPTION_MAIN_MENU = 2;
  private final static int STEP_BY_STEP = 3;
  private final static int PREVIEW_CONFIGURATION_PROPERTIES = 4;
  private final static int ASKING_CHANGE_OPTION = 5;
  private final static int CHANGE_AN_OPTION = 6;
  private final static int CHANGE_OPTIONS_ORACLE = 7;
  private final static int CHANGE_OPTIONS_POSTGRESQL = 8;
  private final static int FINAL_MENU = 10;
  private final static int CHANGE_AN_OPTION_DB = 11;
  private final static int WRITE_PROPERTIES = 20;
  private final static int FINISH_CONFIGURATION = 21;
  private final static int CONFIRM_EXIT = 22;
  private final static int EXIT_APP = -1;

  private final static String ORACLE = "Oracle";
  private final static String POSTGRE_SQL = "PostgreSQL";

  private static final String OPT_DATE_FORMAT = "date format: ";
  private static final String OPT_DATE_SEPARATOR = "date separator: ";
  private static final String OPT_TIME_FORMAT = "time format: ";
  private static final String OPT_TIME_SEPARATOR = "time separator: ";
  private static final String OPT_ATTACHMENTS = "Attachments directory: ";
  private static final String OPT_CONTEXT_NAME = "Context name: ";
  private static final String OPT_WEB_URL = "Web URL: ";
  private static final String OPT_CONTEXT_URL = "Context URL :";
  private static final String OPT_AUTH_CLASS = "Authentication class: ";
  private static final String OPT_DATABASE = "Database:";

  private static final String DB_SID = "SID: ";
  private static final String DB_SYSTEM_USER = "System User: ";
  private static final String DB_SYSTEM_PASS = "System Password: ";
  private static final String DB_USER = "DB User: ";
  private static final String DB_USER_PASS = "DB User Password: ";
  private static final String DB_SERVER = "DB Server Address: ";
  private static final String DB_SERVER_PORT = "DB Server Port: ";

  private static final String PREFIX_DATE_FORMAT_SQL = "dateFormat.sql";
  private static final String PREFIX_ATTACH_PATH = "attach.path";
  private static final String PREFIX_CONTEXT_NAME = "context.name";
  private static final String PREFIX_WEB_URL = "web.url";
  private static final String PREFIX_AUTH_CLASS = "authentication.class";
  private static final String PREFIX_CONTEXT_URL = "context.url";
  private static final String PREFIX_SOURCE_PATH = "source.path";
  private static final String PREFIX_DATE_FORMAT_JAVA = "dateFormat.java";
  private static final String PREFIX_DATE_TIME_FORMAT_JAVA = "dateTimeFormat.java";
  private static final String PREFIX_DATE_FORMAT_JS = "dateFormat.js";
  private static final String PREFIX_DB_SESSION = "bbdd.sessionConfig";
  private static final String PREFIX_DATE_TIME_FORMAT_SQL = "dateTimeFormat.sql";
  private static final String PREFIX_DB_SID = "bbdd.sid";
  private static final String PREFIX_DB_SYSTEM_USER = "bbdd.systemUser";
  private static final String PREFIX_DB_SYSTEM_PASS = "bbdd.systemPassword";
  private static final String PREFIX_DB_USER = "bbdd.user";
  private static final String PREFIX_DB_PASS = "bbdd.password";
  private static final String PREFIX_DB_RDBMS = "bbdd.rdbms";
  private static final String PREFIX_DB_DRIVER = "bbdd.driver";
  private static final String PREFIX_DB_URL = "bbdd.url";

  private int optionForModify = 0, numberOptionsDDBB = 0, mainFlowOption = WELCOME;
  private Scanner agreementLicense = new Scanner(System.in);
  private Scanner infoCollected = new Scanner(System.in);

  /**
   * This is the main method that is invoke by ant setup task.
   * 
   */
  public void execute() {
    Project p = getProject();
    while (mainFlowOption != EXIT_APP) {
      switch (mainFlowOption) {
      case WELCOME:
        showWelcome(p);
        try {
          readLicense(p);
        } catch (IOException e) {
          e.printStackTrace();
        }
        acceptLicense(p);
        break;
      case MAIN_MENU:
        showMainMenu(p);
        break;
      case SELECT_OPTION_MAIN_MENU:
        selectOptionMainMenu(p);
        break;
      case STEP_BY_STEP:
        configureStepByStep(p);
        break;
      case PREVIEW_CONFIGURATION_PROPERTIES:
        previewConfigurationOptions(p);
        break;
      case ASKING_CHANGE_OPTION:
        askForChangeAnOption(p);
        break;
      case CHANGE_AN_OPTION:
        changeAnOptionFirst(p);
        break;
      case CHANGE_OPTIONS_ORACLE:
        changeAllOptionsDatabase(p, optionOracle);
        break;
      case CHANGE_OPTIONS_POSTGRESQL:
        changeAllOptionsDatabase(p, optionPostgreSQL);
        break;
      case FINAL_MENU:
        showFinalMenu(p);
        break;
      case CHANGE_AN_OPTION_DB:
        changeAnOptionDatabase(p);
        break;
      case WRITE_PROPERTIES:
        // All options have been selected... configure Openbravo.properties file.
        setValuesInOpenbravoProperties(p);
        break;
      case FINISH_CONFIGURATION:
        finishConfigurationProcess(p);
        break;
      case CONFIRM_EXIT:
        reConfirmExit(p);
      }
    }
    closeExitProgram(p);
  }

  /**
   * This method shows message "Configuration complete" and copy some templates.
   */
  private void finishConfigurationProcess(Project p) {
    p.log("---------------------------------------------------------------------------- \n Configuration complete. \n----------------------------------------------------------------------------");
    // Copy templates and rename files
    fileCopySomeTemplates(p);
    mainFlowOption = EXIT_APP;
  }

  /**
   * This method changes all options in database: Oracle or PostgreSQL.
   * 
   * @param p
   * @param optionsDatabase
   *          List of database properties
   */
  private void changeAllOptionsDatabase(Project p, List<ConfigureOption> optionsDatabase) {
    for (ConfigureOption optionToCange : optionsDatabase) {
      if (optionToCange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
        p.log("Please select " + optionToCange.getAskInfo());
        optionToCange.getOptions(p);
        boolean numberOk = false;
        do {
          String optionS = infoCollected.nextLine();
          try {
            int option = Integer.parseInt(optionS);
            if (option >= 0 && option < optionToCange.getMax()) {
              optionToCange.setChoose(option);
              optionToCange.setChooseString(optionToCange.getOptionChoose());
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
      } else if (optionToCange.getType() == ConfigureOption.TYPE_OPT_STRING) {
        p.log("\nPlease introduce " + optionToCange.getAskInfo());
        optionToCange.getOptions(p);
        String optionString = infoCollected.nextLine();
        if (!optionString.equals("")) {
          optionToCange.setChooseString(optionString);
        }
      }
      optionsDatabase.set(optionsDatabase.indexOf(optionToCange), optionToCange);
      p.log("\n-------------------------\nYour choice " + optionToCange.getOptionChoose()
          + "\n-------------------------\n\n");
    }
    mainFlowOption = FINAL_MENU;
  }

  /**
   * This method closes scanners and say goodbye.
   */
  private void closeExitProgram(Project p) {
    infoCollected.close();
    agreementLicense.close();
    p.log("---------------------------------------------------------------------------- \n Thanks for use Openbravo ERP Setup. \n----------------------------------------------------------------------------");
  }

  /**
   * This method checks that user wants to leave the program.
   */
  private void reConfirmExit(Project p) {
    p.log("Do you want to exit this program? [y/n]: ");
    String input = agreementLicense.nextLine();
    while (!("Y".equalsIgnoreCase(input) || "N".equalsIgnoreCase(input))) {
      p.log("Please, introduce a correct option. Do you want to exit this program? [y/n]: ");
      input = agreementLicense.nextLine();
    }
    if ("Y".equalsIgnoreCase(input)) {
      p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
      mainFlowOption = EXIT_APP;
    } else if ("N".equalsIgnoreCase(input)) {
      if (optionForOpenbravo.isEmpty()) {
        mainFlowOption = MAIN_MENU;
      } else {
        mainFlowOption = FINAL_MENU;
      }
    }
  }

  /**
   * This method replaces old values in Openbravo.properties by the new requested values.
   */
  private void setValuesInOpenbravoProperties(Project p) {
    setValuesProperties();
    Iterator<String> keySetIterator = replaceProperties.keySet().iterator();
    while (keySetIterator.hasNext()) {
      String keyForFile = keySetIterator.next();
      replaceOptionsProperties(keyForFile + "=", replaceProperties.get(keyForFile), p);
    }
    mainFlowOption = FINISH_CONFIGURATION;
  }

  /**
   * This method changes an option in database [optionOracle or optionPostgreSQL] like SID, DB port,
   * ...
   */
  private void changeAnOptionDatabase(Project p) {
    String optionS, optionString;
    int option;
    optionForModify = optionForModify - optionForOpenbravo.size();
    if (!optionOracle.isEmpty()) {
      ConfigureOption optionToChange = optionOracle.get(optionForModify - 1);
      if (optionToChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
        p.log("Please select " + optionToChange.getAskInfo());
        optionToChange.getOptions(p);
        boolean numberOk = false;
        do {
          optionS = infoCollected.nextLine();
          try {
            option = Integer.parseInt(optionS);
            if (option >= 0 && option < optionToChange.getMax()) {
              optionToChange.setChoose(option);
              optionToChange.setChooseString(optionToChange.getOptionChoose());
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
      } else if (optionToChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
        p.log("\nPlease introduce " + optionToChange.getAskInfo());
        optionToChange.getOptions(p);
        optionString = infoCollected.nextLine();
        if (!optionString.equals("")) {
          optionToChange.setChooseString(optionString);
        }
      }
      optionOracle.set(optionForModify - 1, optionToChange);
      p.log("\n-------------------------\nYour choice " + optionToChange.getOptionChoose()
          + "\n-------------------------\n\n");
    } else if (!optionPostgreSQL.isEmpty()) {
      ConfigureOption optionToChange = optionPostgreSQL.get(optionForModify - 1);
      if (optionToChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
        p.log("Please select " + optionToChange.getAskInfo());
        optionToChange.getOptions(p);
        boolean numberOk = false;
        do {
          optionS = infoCollected.nextLine();
          try {
            option = Integer.parseInt(optionS);
            if (option >= 0 && option < optionToChange.getMax()) {
              optionToChange.setChoose(option);
              optionToChange.setChooseString(optionToChange.getOptionChoose());
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
      } else if (optionToChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
        p.log("\nPlease introduce " + optionToChange.getAskInfo());
        optionToChange.getOptions(p);
        optionString = infoCollected.nextLine();
        if (!optionString.equals("")) {
          optionToChange.setChooseString(optionString);
        }
      }
      optionPostgreSQL.set(optionForModify - 1, optionToChange);
      p.log("\n-------------------------\nYour choice " + optionToChange.getOptionChoose()
          + "\n-------------------------\n\n");
    }
    mainFlowOption = PREVIEW_CONFIGURATION_PROPERTIES;
  }

  /**
   * This method shows the final menu in where user can select accept o return to configure.
   */
  private void showFinalMenu(Project p) {
    p.log("---------------------------------------------------------------------------- \n Are you agree with all options that you configure? \n----------------------------------------------------------------------------");
    printOptionWithStyle(1, "Accept.", p);
    printOptionWithStyle(2, "Back to preview configuration.", p);
    printOptionWithStyle(3, "Exit without saving.", p);
    p.log("Choose an option: ");
    boolean menuOptionOk = false;
    int optionConfigure = 0;
    do {
      String menuOptionS = infoCollected.nextLine();
      try {
        optionConfigure = Integer.parseInt(menuOptionS);
        menuOptionOk = true;
      } catch (NumberFormatException e) {
        p.log("Choose a real option: ");
      }
    } while (!menuOptionOk);
    switch (optionConfigure) {
    case 1:
      // Accept
      mainFlowOption = WRITE_PROPERTIES;
      break;
    case 2:
      // Preview configuration
      mainFlowOption = PREVIEW_CONFIGURATION_PROPERTIES;
      break;
    case 3:
      // Reconfirm exit
      mainFlowOption = CONFIRM_EXIT;
      break;
    default:
      p.log("Choose a real option: ");
    }
  }

  /**
   * This method changes an option in "optionFirst" like date format, time format, ...
   */
  private void changeAnOptionFirst(Project p) {
    ConfigureOption optionToChange = optionForOpenbravo.get(optionForModify - 1);
    if (optionToChange.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
      p.log("Please select " + optionToChange.getAskInfo());
      optionToChange.getOptions(p);
      boolean numberOk = false;
      do {
        String optionS = infoCollected.nextLine();
        try {
          int option = Integer.parseInt(optionS);
          if (option >= 0 && option < optionToChange.getMax()) {
            optionToChange.setChoose(option);
            optionToChange.setChooseString(optionToChange.getOptionChoose());
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
    } else if (optionToChange.getType() == ConfigureOption.TYPE_OPT_STRING) {
      p.log("\nPlease introduce " + optionToChange.getAskInfo());
      optionToChange.getOptions(p);
      String optionString = infoCollected.nextLine();
      if (!optionString.equals("")) {
        optionToChange.setChooseString(optionString);
      }
    }
    optionForOpenbravo.set(optionForModify - 1, optionToChange);
    p.log("\n-------------------------\nYour choice " + optionToChange.getOptionChoose()
        + "\n-------------------------\n\n");
    if (optionToChange.getOptionChoose().equals(ORACLE)) {
      if (optionOracle.isEmpty()) {
        optionOracle = createOPOracle(p);
        numberOptionsDDBB = optionOracle.size();
      }
      if (!optionPostgreSQL.isEmpty()) {
        optionPostgreSQL.clear();
      }
    } else if (optionToChange.getOptionChoose().equals(POSTGRE_SQL)) {
      if (optionPostgreSQL.isEmpty()) {
        optionPostgreSQL = createOPPostgreSQL(p);
        numberOptionsDDBB = optionPostgreSQL.size();
      }
      if (!optionOracle.isEmpty()) {
        optionOracle.clear();
      }
    }
    mainFlowOption = PREVIEW_CONFIGURATION_PROPERTIES;
  }

  /**
   * This method asks for an option for changing. It can be optionFirst, option database or
   * optionLast.
   */
  private void askForChangeAnOption(Project p) {
    p.log("---------------------------------------------------------------------------- \n Do you change any option? \n----------------------------------------------------------------------------");
    p.log("Choose [0] for continue with configuration or a number option for modify: ");
    boolean menuOptionOk = false;
    do {
      String menuOptionS = infoCollected.nextLine();
      try {
        optionForModify = Integer.parseInt(menuOptionS);
        if (optionForModify >= 0
            && optionForModify <= optionForOpenbravo.size() + numberOptionsDDBB) {
          menuOptionOk = true;
        } else {
          p.log("Choose a real option: ");
        }
      } catch (NumberFormatException e) {
        p.log("Choose a real option: ");
      }
    } while (!menuOptionOk);
    // Accept all configuration
    if (optionForModify == 0) {
      mainFlowOption = FINAL_MENU;
      // Options 0 to numberLastOptions + NUM_OPTIONS_LAST, change a particular option
    } else if (optionForModify > 0 && optionForModify <= optionForOpenbravo.size()) {
      mainFlowOption = CHANGE_AN_OPTION;
    } else if (optionForModify > optionForOpenbravo.size()
        && optionForModify <= optionForOpenbravo.size() + numberOptionsDDBB) {
      mainFlowOption = CHANGE_AN_OPTION_DB;
    }
  }

  /**
   * This method shows all options with their values.
   */
  private void previewConfigurationOptions(Project p) {
    p.log("---------------------------------------------------------------------------- \n Preview Openbravo ERP configuration \n----------------------------------------------------------------------------");
    // Show questions in order for get user parameters.
    int numberOption = 1;
    // Show all options by order asc
    for (ConfigureOption previewOptionsLast : optionForOpenbravo) {
      printOptionWithStyle(numberOption,
          previewOptionsLast.getAskInfo() + " " + previewOptionsLast.getOptionChoose(), p);
      numberOption = numberOption + 1;
    }
    if (optionPostgreSQL.isEmpty()) {
      for (ConfigureOption previewOptionsLast : optionOracle) {
        printOptionWithStyle(numberOption, previewOptionsLast.getAskInfo() + " "
            + previewOptionsLast.getOptionChoose(), p);
        numberOption = numberOption + 1;
      }
    } else if (optionOracle.isEmpty()) {
      for (ConfigureOption previewOptionsLast : optionPostgreSQL) {
        printOptionWithStyle(numberOption, previewOptionsLast.getAskInfo() + " "
            + previewOptionsLast.getOptionChoose(), p);
        numberOption = numberOption + 1;
      }
    }
    mainFlowOption = ASKING_CHANGE_OPTION;
  }

  /**
   * This method invokes all the options for configuration one by one.
   */
  private void configureStepByStep(Project p) {
    String typeDDBB = "";
    for (ConfigureOption optionOneByOne : optionForOpenbravo) {
      if (optionOneByOne.getType() == ConfigureOption.TYPE_OPT_CHOOSE) {
        p.log("Please select " + optionOneByOne.getAskInfo());
        optionOneByOne.getOptions(p);
        boolean numberOk = false;
        do {
          String optionS = infoCollected.nextLine();
          try {
            int option = Integer.parseInt(optionS);
            if (option >= 0 && option < optionOneByOne.getMax()) {
              optionOneByOne.setChoose(option);
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
      } else if (optionOneByOne.getType() == ConfigureOption.TYPE_OPT_STRING) {
        p.log("\nPlease introduce " + optionOneByOne.getAskInfo());
        optionOneByOne.getOptions(p);
        String optionString = infoCollected.nextLine();
        if (!optionString.equals("")) {
          optionOneByOne.setChooseString(optionString);
        }
      }
      // review
      optionForOpenbravo.set(optionForOpenbravo.indexOf(optionOneByOne), optionOneByOne);
      typeDDBB = optionOneByOne.getOptionChoose();
      p.log("\n-------------------------\nYour choice " + typeDDBB
          + "\n-------------------------\n\n");
    }
    // Select Oracle or PostgreSQL
    if (typeDDBB.equals(ORACLE)) {
      if (optionOracle.isEmpty()) {
        optionOracle = createOPOracle(p);
        numberOptionsDDBB = optionOracle.size();
      }
      if (!optionPostgreSQL.isEmpty()) {
        optionPostgreSQL.clear();
      }
      mainFlowOption = CHANGE_OPTIONS_ORACLE;
    } else if (typeDDBB.equals(POSTGRE_SQL)) {
      if (optionPostgreSQL.isEmpty()) {
        optionPostgreSQL = createOPPostgreSQL(p);
        numberOptionsDDBB = optionPostgreSQL.size();
      }
      if (!optionOracle.isEmpty()) {
        optionOracle.clear();
      }
      mainFlowOption = CHANGE_OPTIONS_POSTGRESQL;
    }
  }

  /**
   * This method asks selected option in main menu.
   */
  private void selectOptionMainMenu(Project p) {
    boolean menuOptionOk = false;
    int menuOption = 3;
    do {
      String menuOptionS = infoCollected.nextLine();
      try {
        menuOption = Integer.parseInt(menuOptionS);
        menuOptionOk = true;
      } catch (NumberFormatException e) {
        p.log("Please, introduce a correct option: ");
      }
    } while (!menuOptionOk);
    // Create options one-by-one
    if (menuOption == 1) {
      if (optionForOpenbravo.isEmpty()) {
        optionForOpenbravo = createOpenbravoProperties(p);
      }
      // Create optionsDDBB
      // Oracle or Postgresql options.
      String optionDatabaseToCreate = "";
      for (ConfigureOption option : optionForOpenbravo) {
        if (option.getChooseString().equals(ORACLE)) {
          optionDatabaseToCreate = ORACLE;
        } else if (option.getChooseString().equals(POSTGRE_SQL)) {
          optionDatabaseToCreate = POSTGRE_SQL;
        }
      }
      if (optionDatabaseToCreate.equals(ORACLE)) {
        optionOracle = createOPOracle(p);
        numberOptionsDDBB = optionOracle.size();
      } else if (optionDatabaseToCreate.equals(POSTGRE_SQL)) {
        optionPostgreSQL = createOPPostgreSQL(p);
        numberOptionsDDBB = optionPostgreSQL.size();
      }
      mainFlowOption = STEP_BY_STEP;
      // Create all options by default.
    } else if (menuOption == 2) {
      if (optionForOpenbravo.isEmpty()) {
        optionForOpenbravo = createOpenbravoProperties(p);
      }
      // Oracle or Postgresql options
      String optionDatabaseToCreate = "";
      for (ConfigureOption option : optionForOpenbravo) {
        if (option.getChooseString().equals(ORACLE)) {
          optionDatabaseToCreate = ORACLE;
        } else if (option.getChooseString().equals(POSTGRE_SQL)) {
          optionDatabaseToCreate = POSTGRE_SQL;
        }
      }
      if (optionDatabaseToCreate.equals(ORACLE)) {
        optionOracle = createOPOracle(p);
        numberOptionsDDBB = optionOracle.size();
      } else if (optionDatabaseToCreate.equals(POSTGRE_SQL)) {
        optionPostgreSQL = createOPPostgreSQL(p);
        numberOptionsDDBB = optionPostgreSQL.size();
      }
      // Go to preview options configurate by default
      mainFlowOption = PREVIEW_CONFIGURATION_PROPERTIES;
    } else if (menuOption == 3) {
      mainFlowOption = CONFIRM_EXIT;
    } else {
      p.log("Please, introduce a correct option: ");
    }
  }

  /**
   * This method shows main menu of application.
   */
  private void showMainMenu(Project p) {
    p.log("---------------------------------------------------------------------------- \n Please choose one option. \n----------------------------------------------------------------------------");
    printOptionWithStyle(1, "Step-by-step configuration.", p);
    printOptionWithStyle(2, "Default configuration.", p);
    printOptionWithStyle(3, "Exit without saving.", p);
    p.log("Choose an option: ");
    mainFlowOption = SELECT_OPTION_MAIN_MENU;
  }

  /**
   * This method prints options with the same style.
   */
  static void printOptionWithStyle(int numberOption, String textOption, Project p) {
    p.log("[" + numberOption + "] " + textOption);
  }

  /**
   * This method asks for users that accept the license of Openbravo installation.
   */
  private void acceptLicense(Project p) {
    p.log("Do you accept this license? [y/n]: ");
    String input = agreementLicense.nextLine();
    while (!("Y".equalsIgnoreCase(input) || "N".equalsIgnoreCase(input))) {
      p.log("Please, introduce a correct option. Do you accept this license? [y/n]: ");
      input = agreementLicense.nextLine();
    }
    if ("Y".equalsIgnoreCase(input)) {
      mainFlowOption = MAIN_MENU;
    } else if ("N".equalsIgnoreCase(input)) {
      p.log("---------------------------------------------------------------------------- \n You have not successfully completed the configuration process.");
      mainFlowOption = EXIT_APP;
    }
  }

  /**
   * This method copies some important files.
   */
  private static void fileCopySomeTemplates(Project p) {
    fileCopyTemplate(FORMAT_XML + ".template", FORMAT_XML, p);
    fileCopyTemplate(LOG4J_LCF + ".template", LOG4J_LCF, p);
    fileCopyTemplate(USERCONFIG_XML + ".template", USERCONFIG_XML, p);
    fileCopyTemplate(COMMON_COMPONENT + ".template", COMMON_COMPONENT, p);
    fileCopyTemplate(CLASSPATH + ".template", CLASSPATH, p);
  }

  /**
   * This function shows a welcome to install application.
   */
  private static void showWelcome(Project p1) {
    Scanner inp = new Scanner(System.in);
    p1.log("---------------------------------------------------------------------------- \n Welcome to the Openbravo ERP Setup Wizard. \n----------------------------------------------------------------------------");
    p1.log("Please read the following License Agreement. You must accept the terms of this\n agreement before continuing with the installation.");
    p1.log("Press [Enter] to continue:");
    inp.nextLine();
  }

  /**
   * This function uses all information asking to user for configurate Openbravo.properties file.
   */
  private static void setValuesProperties() {
    String timeSeparator = "", dateSeparator = "", timeFormat = "", dateFormat = "", database = "";
    for (ConfigureOption optionFirstForReplace : optionForOpenbravo) {
      if (optionFirstForReplace.getAskInfo().equals(OPT_DATE_SEPARATOR)) {
        dateSeparator = optionFirstForReplace.getOptionChoose();
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_TIME_SEPARATOR)) {
        timeSeparator = optionFirstForReplace.getOptionChoose();
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_DATE_FORMAT)) {
        dateFormat = optionFirstForReplace.getOptionChoose();
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_TIME_FORMAT)) {
        timeFormat = optionFirstForReplace.getOptionChoose();
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_DATABASE)) {
        database = optionFirstForReplace.getOptionChoose();
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_ATTACHMENTS)) {
        replaceProperties.put(PREFIX_ATTACH_PATH, optionFirstForReplace.getOptionChoose());
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_CONTEXT_NAME)) {
        replaceProperties.put(PREFIX_CONTEXT_NAME, optionFirstForReplace.getOptionChoose());
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_WEB_URL)) {
        replaceProperties.put(PREFIX_WEB_URL, optionFirstForReplace.getOptionChoose());
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_AUTH_CLASS)) {
        replaceProperties.put(PREFIX_AUTH_CLASS, optionFirstForReplace.getOptionChoose());
      } else if (optionFirstForReplace.getAskInfo().equals(OPT_CONTEXT_URL)) {
        replaceProperties.put(PREFIX_CONTEXT_URL, optionFirstForReplace.getOptionChoose());
      }
    }
    replaceProperties.put(PREFIX_SOURCE_PATH, System.getProperty("user.dir"));

    if (dateFormat.substring(0, 1).equals("D")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_JAVA, "dd" + dateSeparator + "MM" + dateSeparator
          + "yyyy");
    } else if (dateFormat.substring(0, 1).equals("M")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_JAVA, "MM" + dateSeparator + "dd" + dateSeparator
          + "yyyy");
    } else if (dateFormat.substring(0, 1).equals("Y")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_JAVA, "yyyy" + dateSeparator + "MM" + dateSeparator
          + "dd");
    }

    if (timeFormat.equals("12h")) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put(PREFIX_DATE_TIME_FORMAT_JAVA, "dd" + dateSeparator + "MM"
            + dateSeparator + "yyyy hh" + timeSeparator + "mm" + timeSeparator + "ss a");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put(PREFIX_DATE_TIME_FORMAT_JAVA, "MM" + dateSeparator + "dd"
            + dateSeparator + "yyyy hh" + timeSeparator + "mm" + timeSeparator + "ss a");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put(PREFIX_DATE_TIME_FORMAT_JAVA, "yyyy" + dateSeparator + "MM"
            + dateSeparator + "dd hh" + timeSeparator + "mm" + timeSeparator + "ss a");
      }
    } else if (timeFormat.equals("24h")) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put(PREFIX_DATE_TIME_FORMAT_JAVA, "dd" + dateSeparator + "MM"
            + dateSeparator + "yyyy HH" + timeSeparator + "mm" + timeSeparator + "ss");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put(PREFIX_DATE_TIME_FORMAT_JAVA, "MM" + dateSeparator + "dd"
            + dateSeparator + "yyyy HH" + timeSeparator + "mm" + timeSeparator + "ss");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put(PREFIX_DATE_TIME_FORMAT_JAVA, "yyyy" + dateSeparator + "MM"
            + dateSeparator + "dd HH" + timeSeparator + "mm" + timeSeparator + "ss");
      }
    }

    if (dateFormat.substring(0, 1).equals("D")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_SQL, "DD" + dateSeparator + "MM" + dateSeparator
          + "YYYY");
    } else if (dateFormat.substring(0, 1).equals("M")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_SQL, "MM" + dateSeparator + "DD" + dateSeparator
          + "YYYY");
    } else if (dateFormat.substring(0, 1).equals("Y")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_SQL, "YYYY" + dateSeparator + "MM" + dateSeparator
          + "DD");
    }
    if (dateFormat.substring(0, 1).equals("D")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_JS, "%d" + dateSeparator + "%m" + dateSeparator
          + "%Y");
    } else if (dateFormat.substring(0, 1).equals("M")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_JS, "%m" + dateSeparator + "%d" + dateSeparator
          + "%Y");
    } else if (dateFormat.substring(0, 1).equals("Y")) {
      replaceProperties.put(PREFIX_DATE_FORMAT_JS, "%Y" + dateSeparator + "%m" + dateSeparator
          + "%d");
    }
    replaceProperties.put(PREFIX_DATE_TIME_FORMAT_SQL, "DD-MM-YYYY HH24:MI:SS");

    if (database.equals(ORACLE)) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put(PREFIX_DB_SESSION, "ALTER SESSION SET NLS_DATE_FORMAT='DD"
            + dateSeparator + "MM" + dateSeparator + "YYYY' NLS_NUMERIC_CHARACTERS='.,'");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put(PREFIX_DB_SESSION, "ALTER SESSION SET NLS_DATE_FORMAT='MM"
            + dateSeparator + "DD" + dateSeparator + "YYYY' NLS_NUMERIC_CHARACTERS='.,'");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put(PREFIX_DB_SESSION, "ALTER SESSION SET NLS_DATE_FORMAT='YYYY"
            + dateSeparator + "MM" + dateSeparator + "DD' NLS_NUMERIC_CHARACTERS='.,'");
      }
      String nameBBDD = "", serverBBDD = "", portBBDD = "";
      for (ConfigureOption optionLastForReplace : optionOracle) {
        if (optionLastForReplace.getAskInfo().equals(DB_SID)) {
          nameBBDD = optionLastForReplace.getOptionChoose();
          replaceProperties.put(PREFIX_DB_SID, nameBBDD);
        } else if (optionLastForReplace.getAskInfo().equals(DB_SYSTEM_USER)) {
          replaceProperties.put(PREFIX_DB_SYSTEM_USER, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_SYSTEM_PASS)) {
          replaceProperties.put(PREFIX_DB_SYSTEM_PASS, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_USER)) {
          replaceProperties.put(PREFIX_DB_USER, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_USER_PASS)) {
          replaceProperties.put(PREFIX_DB_PASS, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_SERVER)) {
          serverBBDD = optionLastForReplace.getOptionChoose();
        } else if (optionLastForReplace.getAskInfo().equals(DB_SERVER_PORT)) {
          portBBDD = optionLastForReplace.getOptionChoose();
        }
      }
      replaceProperties.put(PREFIX_DB_RDBMS, ORACLE);
      replaceProperties.put(PREFIX_DB_DRIVER, "oracle.jdbc.driver.OracleDriver");
      replaceProperties.put(PREFIX_DB_URL, "jdbc:oracle:thin:@" + serverBBDD + ":" + portBBDD + ":"
          + nameBBDD);
    } else if (database.equals(POSTGRE_SQL)) {
      if (dateFormat.substring(0, 1).equals("D")) {
        replaceProperties.put(PREFIX_DB_SESSION, "select update_dateFormat('DD" + dateSeparator
            + "MM" + dateSeparator + "YYYY')");
      } else if (dateFormat.substring(0, 1).equals("M")) {
        replaceProperties.put(PREFIX_DB_SESSION, "select update_dateFormat('MM" + dateSeparator
            + "DD" + dateSeparator + "YYYY')");
      } else if (dateFormat.substring(0, 1).equals("Y")) {
        replaceProperties.put(PREFIX_DB_SESSION, "select update_dateFormat('YYYY" + dateSeparator
            + "MM" + dateSeparator + "DD')");
      }
      String serverBBDD = "", portBBDD = "";
      for (ConfigureOption optionLastForReplace : optionPostgreSQL) {
        if (optionLastForReplace.getAskInfo().equals(DB_SID)) {
          replaceProperties.put(PREFIX_DB_SID, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_SYSTEM_USER)) {
          replaceProperties.put(PREFIX_DB_SYSTEM_USER, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_SYSTEM_PASS)) {
          replaceProperties.put(PREFIX_DB_SYSTEM_PASS, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_USER)) {
          replaceProperties.put(PREFIX_DB_USER, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_USER_PASS)) {
          replaceProperties.put(PREFIX_DB_PASS, optionLastForReplace.getOptionChoose());
        } else if (optionLastForReplace.getAskInfo().equals(DB_SERVER)) {
          serverBBDD = optionLastForReplace.getOptionChoose();
        } else if (optionLastForReplace.getAskInfo().equals(DB_SERVER_PORT)) {
          portBBDD = optionLastForReplace.getOptionChoose();
        }
      }
      replaceProperties.put(PREFIX_DB_RDBMS, "POSTGRE");
      replaceProperties.put(PREFIX_DB_DRIVER, "org.postgresql.Driver");
      replaceProperties.put(PREFIX_DB_URL, "jdbc:postgresql://" + serverBBDD + ":" + portBBDD);
    }
  }

  /**
   * This function replaces in Openbravo.properties the value of option searchOption with value
   * changeOption. Concatenated searchOption+changeOption. For example: "bbdd.user=" + "admin".
   * 
   * @param searchOption
   *          Prefix to search
   * @param changeOption
   *          Value to write in Openbravo.properties
   */
  private static void replaceOptionsProperties(String searchOption, String changeOption, Project p) {
    try {
      boolean isFound = false;
      File fileR = new File(OPENBRAVO_PROPERTIES);
      if (!fileR.exists()) {
        // Copy if not exists Openbravo.properties
        // FileUtils.copyFile(new File(OPENBRAVO_PROPERTIES + ".template"), fileR);
        fileCopyTemplate(OPENBRAVO_PROPERTIES + ".template", OPENBRAVO_PROPERTIES, p);
      }
      // Modify Openbravo.properties file if PostgreSQL's options have been disabled.
      if (!searchOptionsProperties(fileR, PREFIX_DB_RDBMS, p).equals(
          replaceProperties.get(PREFIX_DB_RDBMS))) {
        changeOraclePostgresql(p);
      }

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
   * This function searches an option in fileO file and returns the value of searchOption.
   * 
   * @param fileO
   *          File to search
   * @param searchOption
   *          Option that is searched
   * @return String Value found
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
   * This function creates first options for configuration. Information is collected from
   * Openbravo.properties file.
   * 
   * @return List<ConfigureOption> of default properties
   */
  private static List<ConfigureOption> createOpenbravoProperties(Project p) {
    List<ConfigureOption> options = new ArrayList<ConfigureOption>();
    File fileO = new File(OPENBRAVO_PROPERTIES);
    if (!fileO.exists()) {
      fileCopyTemplate(OPENBRAVO_PROPERTIES + ".template", OPENBRAVO_PROPERTIES_AUX, p);
      fileO = new File(OPENBRAVO_PROPERTIES_AUX);
    }

    String askInfo = OPT_DATE_FORMAT;
    ArrayList<String> optChoosen = new ArrayList<String>();
    optChoosen.add("DDMMYYYY");
    optChoosen.add("MMDDYYYY");
    optChoosen.add("YYYYMMDD");
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    String compareDateformat = searchOptionsProperties(fileO, PREFIX_DATE_FORMAT_SQL, p).substring(
        0, 1);
    if (compareDateformat.equalsIgnoreCase("d")) {
      o0.setChooseString("DDMMYYYY");
    } else if (compareDateformat.equalsIgnoreCase("m")) {
      o0.setChooseString("MMDDYYYY");
    } else if (compareDateformat.equalsIgnoreCase("y")) {
      o0.setChooseString("YYYYMMDD");
    }
    options.add(o0);

    askInfo = OPT_DATE_SEPARATOR;
    optChoosen = new ArrayList<String>();
    optChoosen.add("-");
    optChoosen.add("/");
    optChoosen.add(".");
    optChoosen.add(":");
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    compareDateformat = searchOptionsProperties(fileO, PREFIX_DATE_TIME_FORMAT_SQL, p).substring(0,
        9);
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
    options.add(o1);

    askInfo = OPT_TIME_FORMAT;
    optChoosen = new ArrayList<String>();
    optChoosen.add("12h");
    optChoosen.add("24h");
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);

    if (searchOptionsProperties(fileO, PREFIX_DATE_TIME_FORMAT_JAVA, p).contains("a")) {
      o2.setChooseString("12h");
    } else {
      o2.setChooseString("24h");
    }
    options.add(o2);

    askInfo = OPT_TIME_SEPARATOR;
    optChoosen = new ArrayList<String>();
    optChoosen.add(":");
    optChoosen.add(".");
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    compareDateformat = searchOptionsProperties(fileO, PREFIX_DATE_TIME_FORMAT_SQL, p)
        .substring(10);
    if (compareDateformat.contains(":")) {
      o3.setChooseString(":");
    } else if (compareDateformat.contains(".")) {
      o3.setChooseString(".");
    } else {
      o3.setChooseString(":");
    }
    options.add(o3);

    askInfo = OPT_ATTACHMENTS;
    ConfigureOption o4 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    String optionValueString = searchOptionsProperties(fileO, PREFIX_ATTACH_PATH, p);
    if (optionValueString.equals("")) {
      o4.setChooseString("/opt/openbravo/attachments");
    } else {
      o4.setChooseString(optionValueString);
    }
    options.add(o4);

    askInfo = OPT_CONTEXT_NAME;
    ConfigureOption o5 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_CONTEXT_NAME, p);
    if (optionValueString.equals("")) {
      o5.setChooseString("openbravo");
    } else {
      o5.setChooseString(optionValueString);
    }
    options.add(o5);

    askInfo = OPT_WEB_URL;
    ConfigureOption o6 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_WEB_URL, p);
    if (optionValueString.equals("")) {
      o6.setChooseString("@actual_url_context@/web");
    } else {
      o6.setChooseString(optionValueString);
    }
    options.add(o6);

    askInfo = OPT_CONTEXT_URL;
    ConfigureOption o7 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_CONTEXT_URL, p);
    if (optionValueString.equals("")) {
      o7.setChooseString("http://localhost:8080/openbravo");
    } else {
      o7.setChooseString(optionValueString);
    }
    options.add(o7);

    askInfo = OPT_AUTH_CLASS;
    ConfigureOption o8 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_AUTH_CLASS, p);
    if (optionValueString.equals("")) {
      o8.setChooseString("");
    } else {
      o8.setChooseString(optionValueString);
    }
    options.add(o8);

    askInfo = OPT_DATABASE;
    optChoosen = new ArrayList<String>();
    optChoosen.add(ORACLE);
    optChoosen.add(POSTGRE_SQL);
    ConfigureOption o9 = new ConfigureOption(ConfigureOption.TYPE_OPT_CHOOSE, askInfo, optChoosen);
    if (searchOptionsProperties(fileO, PREFIX_DB_RDBMS, p).equals(ORACLE)) {
      o9.setChooseString(ORACLE);
    } else if (searchOptionsProperties(fileO, PREFIX_DB_RDBMS, p).equals("POSTGRE")) {
      o9.setChooseString(POSTGRE_SQL);
    } else {
      o9.setChooseString(POSTGRE_SQL);
    }
    options.add(o9);

    // Delete auxiliar file
    if (fileO.getPath().equals(OPENBRAVO_PROPERTIES_AUX)) {
      fileO.delete();
    }
    return options;
  }

  /**
   * This function creates options of Oracle configuration. Information is collected from
   * Openbravo.properties file.
   * 
   * @return List<ConfigureOption> of Oracle default properties
   */
  private static List<ConfigureOption> createOPOracle(Project p) {
    List<ConfigureOption> option = new ArrayList<ConfigureOption>();

    File fileO = new File(OPENBRAVO_PROPERTIES);
    if (!fileO.exists()) {
      fileO = new File(OPENBRAVO_PROPERTIES + ".template");
    }
    // Modify Openbravo.properties file if Oracle's options have been disabled.
    if (searchOptionsProperties(fileO, PREFIX_DB_RDBMS, p).equals("POSTGRE")) {
      changeOraclePostgresql(p);
    } else {
      fileO = new File(OPENBRAVO_PROPERTIES);
      if (!fileO.exists()) {
        fileCopyTemplate(OPENBRAVO_PROPERTIES + ".template", OPENBRAVO_PROPERTIES_AUX, p);
      }
    }
    // If not exists OPENBRAVO_PROPERTIES_AUX file, use OPENBRAVO_PROPERTIES
    fileO = new File(OPENBRAVO_PROPERTIES_AUX);
    if (!fileO.exists()) {
      fileO = new File(OPENBRAVO_PROPERTIES);
    }

    String askInfo = DB_SID;
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    String optionValueString = searchOptionsProperties(fileO, PREFIX_DB_SID, p);
    if (optionValueString.equals("")) {
      o0.setChooseString("xe");
    } else {
      o0.setChooseString(optionValueString);
    }
    option.add(o0);

    askInfo = DB_SYSTEM_USER;
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_DB_SYSTEM_USER, p);
    if (optionValueString.equals("")) {
      o1.setChooseString("SYSTEM");
    } else {
      o1.setChooseString(optionValueString);
    }
    option.add(o1);

    askInfo = DB_SYSTEM_PASS;
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_DB_SYSTEM_PASS, p);
    if (optionValueString.equals("")) {
      o2.setChooseString("SYSTEM");
    } else {
      o2.setChooseString(optionValueString);
    }
    option.add(o2);

    askInfo = DB_USER;
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_DB_USER, p);
    if (optionValueString.equals("")) {
      o3.setChooseString("TAD");
    } else {
      o3.setChooseString(optionValueString);
    }
    option.add(o3);

    askInfo = DB_USER_PASS;
    ConfigureOption o4 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    optionValueString = searchOptionsProperties(fileO, PREFIX_DB_PASS, p);
    if (optionValueString.equals("")) {
      o4.setChooseString("TAD");
    } else {
      o4.setChooseString(optionValueString);
    }
    option.add(o4);

    String separateString = searchOptionsProperties(fileO, PREFIX_DB_URL, p);
    if (separateString.equals("")) {
      separateString = "jdbc:oracle:thin:@localhost:1521:xe";
    }
    String[] separateUrl = separateString.split(":");

    askInfo = DB_SERVER;
    ConfigureOption o5 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o5.setChooseString(separateUrl[3].substring(1));
    option.add(o5);

    askInfo = DB_SERVER_PORT;
    ConfigureOption o6 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o6.setChooseString(separateUrl[4]);
    option.add(o6);

    // Delete auxiliar file
    if (fileO.getPath().equals(OPENBRAVO_PROPERTIES_AUX)) {
      fileO.delete();
    }

    return option;
  }

  /**
   * This function creates options of PostgreSQL configuration.Information is collected from
   * Openbravo.properties file.
   * 
   * @return List<ConfigureOption> of PostgreSQL default properties
   */
  private static List<ConfigureOption> createOPPostgreSQL(Project p) {
    List<ConfigureOption> option = new ArrayList<ConfigureOption>();
    String askInfo;

    File fileO = new File(OPENBRAVO_PROPERTIES);
    if (!fileO.exists()) {
      fileO = new File(OPENBRAVO_PROPERTIES + ".template");
    }
    // Modify Openbravo.properties file if PostgreSQL's options have been disabled.
    if (searchOptionsProperties(fileO, PREFIX_DB_RDBMS, p).equals("ORACLE")) {
      changeOraclePostgresql(p);
    } else {
      fileO = new File(OPENBRAVO_PROPERTIES);
      if (!fileO.exists()) {
        fileCopyTemplate(OPENBRAVO_PROPERTIES + ".template", OPENBRAVO_PROPERTIES_AUX, p);
      }
    }
    // If not exists OPENBRAVO_PROPERTIES_AUX file, use OPENBRAVO_PROPERTIES
    fileO = new File(OPENBRAVO_PROPERTIES_AUX);
    if (!fileO.exists()) {
      fileO = new File(OPENBRAVO_PROPERTIES);
    }

    askInfo = DB_SID;
    ConfigureOption o0 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o0.setChooseString(searchOptionsProperties(fileO, PREFIX_DB_SID, p));
    option.add(o0);

    askInfo = DB_SYSTEM_USER;
    ConfigureOption o1 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o1.setChooseString(searchOptionsProperties(fileO, PREFIX_DB_SYSTEM_USER, p));
    option.add(o1);

    askInfo = DB_SYSTEM_PASS;
    ConfigureOption o2 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o2.setChooseString(searchOptionsProperties(fileO, PREFIX_DB_SYSTEM_PASS, p));
    option.add(o2);

    askInfo = DB_USER;
    ConfigureOption o3 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o3.setChooseString(searchOptionsProperties(fileO, PREFIX_DB_USER, p));
    option.add(o3);

    askInfo = DB_USER_PASS;
    ConfigureOption o4 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o4.setChooseString(searchOptionsProperties(fileO, PREFIX_DB_PASS, p));
    option.add(o4);

    String separateString = searchOptionsProperties(fileO, PREFIX_DB_URL, p);
    String[] separateUrl = separateString.split(":");

    askInfo = DB_SERVER;
    ConfigureOption o5 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o5.setChooseString(separateUrl[2].substring(2));
    option.add(o5);

    askInfo = DB_SERVER_PORT;
    ConfigureOption o6 = new ConfigureOption(ConfigureOption.TYPE_OPT_STRING, askInfo,
        new ArrayList<String>());
    o6.setChooseString(separateUrl[3]);
    option.add(o6);

    // Delete auxiliar file
    if (fileO.getPath().equals(OPENBRAVO_PROPERTIES_AUX)) {
      fileO.delete();
    }

    return option;
  }

  /**
   * This function disables options Oracle or PostgreSQL, using the [#] at the beginning of the
   * options to disable them.
   */
  private static void changeOraclePostgresql(Project p) {
    try {
      File fileR = new File(OPENBRAVO_PROPERTIES);
      if (!fileR.exists()) {
        fileR = new File(OPENBRAVO_PROPERTIES + ".template");
      }
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
      if (fileR.exists()) {
        fileR.delete();
        File fileW = new File(OPENBRAVO_PROPERTIES_AUX);
        fileW.renameTo(new File(OPENBRAVO_PROPERTIES));
      }
    } catch (Exception e2) {
      p.log("Excetion deleting/rename file: " + e2);
    }
  }

  /**
   * This function copies a file if it is not exists.
   * 
   * @param sourceFile
   *          input file
   * @param destinationFile
   *          output file
   * @param p
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
   * This function shows license terms for installing OpenBravo. License is located in
   * OPENBRAVO_LICENSE.
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