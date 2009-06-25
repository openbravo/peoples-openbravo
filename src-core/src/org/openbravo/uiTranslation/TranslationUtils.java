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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.uiTranslation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

class TranslationUtils {

  private static final Logger log4j = Logger.getLogger(TranslationUtils.class);

  public static final int TAB = 0, FORM = 1, PROCESS = 2;
  private static String fileName;
  private static String language;
  private static HashMap<String, String> textmap;
  private static TextInterfacesData[] textData;
  private static WindowLabel[] windowLabels;
  private static Collection<WindowLabel> windowLabelsCol = new ArrayList<WindowLabel>();
  private static ConnectionProvider conn;
  private static int interfaceType;

  public static HashMap<String, String> processFormLabels(ConnectionProvider con, String filename,
      String lang) {
    fileName = filename;
    language = lang;
    textmap = new HashMap<String, String>();
    conn = con;

    retrieveLabelData();
    populateLabelMap();

    return textmap;
  }

  public static InterfaceInfo getModuleLang(ConnectionProvider con, InterfaceInfo info) {
    String lang = "";
    try {
      if (info.getId() != null && !info.getId().equals("")) {
        InterfaceModuleInfoData[] moduleInfo;
        if (info.getInterfaceType() == InterfaceInfo.TAB) {
          moduleInfo = InterfaceModuleInfoData.selectTabModuleLang(con, info.getId());
          for (int i = 0; i < moduleInfo.length; i++) {
            InterfaceModuleInfoData module = moduleInfo[i];
            if (module != null && module.modulelanguage != null) {
              lang = module.modulelanguage;
              defineInterfaceInfo(info, module);
            }
          }
        } else if (info.getInterfaceType() == InterfaceInfo.PROCESS) {
          moduleInfo = InterfaceModuleInfoData.selectProcessModuleLang(con, info.getId());
          for (int i = 0; i < moduleInfo.length; i++) {
            InterfaceModuleInfoData module = moduleInfo[i];
            if (module != null && module.modulelanguage != null) {
              lang = module.modulelanguage;
              defineInterfaceInfo(info, module);
            }
          }
        }
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return info;
  }

  private static void defineInterfaceInfo(InterfaceInfo info, InterfaceModuleInfoData module) {
    info.setModuleLanguage(module.modulelanguage);
    info.setModuleId(module.moduleid);
    info.setTitle(module.name);
    info.setDescription(module.description);
    info.setHelp(module.help);
  }

  public static WindowLabel[] processWindowLabels(ConnectionProvider con, String tabId,
      String lang, String moduleLang) {
    FieldLabelsData[] fieldLabels;
    FieldGroupLabelsData[] fieldGroupLabels;
    try {
      if (lang.equals("") || lang.equals(moduleLang) || moduleLang.equals("")) {
        fieldLabels = FieldLabelsData.select(con, tabId, lang);
        fieldGroupLabels = FieldGroupLabelsData.select(con, tabId);
        populateFieldLabels(fieldLabels);
        if (fieldGroupLabels.length > 0) {
          populateFieldGroupLabels(fieldGroupLabels);
        }
      } else {
        fieldLabels = FieldLabelsData.select(con, tabId, lang);
        fieldGroupLabels = FieldGroupLabelsData.selectFieldGroupTrl(con, tabId, lang);
        populateFieldLabels(fieldLabels);
        if (fieldGroupLabels.length > 0) {
          populateFieldGroupLabels(fieldGroupLabels);
        }
      }
      windowLabels = new WindowLabel[windowLabelsCol.size()];
      int i = 0;
      for (Iterator<WindowLabel> iteratorWinLabel = windowLabelsCol.iterator(); iteratorWinLabel
          .hasNext();) {
        WindowLabel label = (WindowLabel) iteratorWinLabel.next();
        windowLabels[i] = label;
        i++;
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return windowLabels;
  }

  public static WindowLabel[] processProcessLabels(ConnectionProvider con, String lang,
      InterfaceInfo uiInfo) {
    ProcessLabelsData[] processLabels;
    try {
      if (lang.equals(uiInfo.getModuleLanguage()) || lang.equals("")
          || uiInfo.getModuleLanguage().equals("")) {
        processLabels = ProcessLabelsData.selectOriginalParameters(con, uiInfo.getId());
        populateProcessLabels(processLabels);
      } else {
        processLabels = ProcessLabelsData.selectTranslatedParameters(con, uiInfo.getId(), lang);
        populateProcessLabels(processLabels);
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return windowLabels;
  }

  public static InterfaceInfo getInterfaceHeaderTrlInfo(ConnectionProvider con, InterfaceInfo info,
      String lang) {
    InterfaceInfo uiInfoResult = info;
    if (info.getInterfaceType() == InterfaceInfo.TAB) {
      InterfaceTrlInfoData[] data;
      try {
        data = InterfaceTrlInfoData.selectProcessTrlInfo(con, uiInfoResult.getId(), lang);
        if (data != null && data.length > 0) {
          uiInfoResult.setTitle(data[0].name);
          uiInfoResult.setDescription(data[0].description);
          uiInfoResult.setHelp(data[0].help);
        }
      } catch (ServletException e) {
        e.printStackTrace();
      }
    } else if (info.getInterfaceType() == InterfaceInfo.PROCESS) {
      InterfaceTrlInfoData[] data;
      try {
        data = InterfaceTrlInfoData.selectProcessTrlInfo(con, uiInfoResult.getId(), lang);
        if (data != null && data.length > 0) {
          uiInfoResult.setTitle(data[0].name);
          uiInfoResult.setDescription(data[0].description);
          uiInfoResult.setHelp(data[0].help);
        }
      } catch (ServletException e) {
        e.printStackTrace();
      }
    }
    return uiInfoResult;
  }

  private static void populateFieldLabels(FieldLabelsData[] fieldLabels) {
    for (int labelsCount = 0; labelsCount < fieldLabels.length; labelsCount++) {
      FieldLabelsData labelData = fieldLabels[labelsCount];
      WindowLabel label = new WindowLabel(labelData.adColumnId, labelData.fieldName,
          labelData.fieldtrlName);
      windowLabelsCol.add(label);
    }
  }

  private static void populateFieldGroupLabels(FieldGroupLabelsData[] fieldGroupLabels) {
    HashMap<String, WindowLabel> uniqueFieldGroups = new HashMap<String, WindowLabel>();
    for (int labelsCount = 0; labelsCount < fieldGroupLabels.length; labelsCount++) {
      FieldGroupLabelsData labelData = fieldGroupLabels[labelsCount];
      WindowLabel label = new WindowLabel(WindowLabel.FIELD_GROUP_LABEL, labelData.fieldgroupid,
          labelData.fieldgroupname, labelData.fieldgrouptrlname);
      uniqueFieldGroups.put(label.getOriginalLabel(), label);
    }
    windowLabelsCol.addAll(uniqueFieldGroups.values());
  }

  private static void populateProcessLabels(ProcessLabelsData[] fieldLabels) {
    windowLabels = new WindowLabel[fieldLabels.length];
    for (int labelsCount = 0; labelsCount < fieldLabels.length; labelsCount++) {
      ProcessLabelsData labelData = fieldLabels[labelsCount];
      WindowLabel label = new WindowLabel(labelData.processparacolumnname,
          labelData.processparaname, labelData.processparatrlname);
      windowLabels[labelsCount] = label;
    }
  }

  private static void retrieveLabelData() {
    try {
      textData = TextInterfacesData.selectText(conn, fileName, language);
    } catch (ServletException e) {
      e.printStackTrace();
    }
  }

  private static void populateLabelMap() {
    for (int i = 0; i < textData.length; i++) {
      // trim values, in some occasions there is a character 160 representing blank spaces
      textmap.put(textData[i].text.replace((char) 160, ' ').trim(), textData[i].trltext);
    }
  }
}
