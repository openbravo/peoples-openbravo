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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.modules;

import java.io.File;
import java.util.ArrayList;

import javax.servlet.ServletException;

import net.sf.cglib.transform.impl.FieldProvider;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Translation;
import org.openbravo.erpCommon.reference.ActionButtonData;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * ApplyModule processes all modules that are in status (I)Installed or (P)Pending
 * but not (A)Applied yet. This process is done by the execute method.
 * 
 * 
 */
public class ApplyModule {
    private static ConnectionProvider pool;
    static Logger log4j = Logger.getLogger(ApplyModule.class);
    private String obDir;

    public ApplyModule(ConnectionProvider cp, String dir) {
        pool = cp;
        obDir = dir;
        PropertyConfigurator.configure(obDir+"/src/log4j.lcf");
        log4j = Logger.getLogger(ApplyModule.class);
    }

    /**
     * Process the Installed but not applied modules, the treatement for these
     * modules is: *Translation modules In case the module contains translations
     * the process will: -Sets the module language as system -Populates the trl
     * tables calling the verify language process (this is done just once for
     * all the modules with translations. -Imports the xml files with
     * translations into trl tables.
     * 
     * *Reference data modules for client system Loads the reference data in
     * client system (TODO: implement it)
     * 
     * *All modules Sets them as installed
     * 
     * *Uninstalled modules Deletes them
     */
    public void execute() {
        PropertyConfigurator.configure(obDir+"/src/log4j.lcf");
		try {
			// **************** Translation modules ************************
			// Check whether modules to install are translations
			log4j.info("Looking for tranlation modules");
			final ApplyModuleData[] data = ApplyModuleData
					.selectTranslationModules(pool);

			if (data != null && data.length > 0) {
				log4j.info(data.length + " tranlation modules found");
				// Set language as system in case it is not already
				for (int i = 0; i < data.length; i++) {
					if (data[i].issystemlanguage.equals("N")) {
						ApplyModuleData.setSystemLanguage(pool,
								data[i].adLanguage);
					}
				}

				// Populate trl tables (execute verify languages)
				try {
					log4j.info("Executing verify language process");
					final String pinstance = SequenceIdData.getUUID();
					PInstanceProcessData.insertPInstance(pool, pinstance,
							"179", "0", "N", "0", "0", "0");

					final VariablesSecureApp vars = new VariablesSecureApp("0", "0",
							"0");

					ActionButtonData.process179(pool, pinstance);

					final PInstanceProcessData[] pinstanceData = PInstanceProcessData
							.select(pool, pinstance);
					final OBError myMessage = Utility.getProcessInstanceMessage(pool,
							vars, pinstanceData);
					if (myMessage.getType().equals("Error"))
						log4j.error(myMessage.getMessage());
					else
						log4j.info(myMessage.getMessage());
				} catch (final ServletException ex) {
					ex.printStackTrace();
				}

				// Import language modules
				Translation.setLog4j(log4j);
				Translation.setConnectionProvicer(pool);

				for (int i = 0; i < data.length; i++) {
					log4j.info("Importing language " + data[i].adLanguage
							+ " from module " + data[i].name);
					Translation.importTrlDirectory(obDir + "/modules/"
							+ data[i].javapackage
							+ "/referencedata/translation", data[i].adLanguage,
							"0", null);
				}

			}

			// **************** Reference data for system client modules
			// ************************
			log4j.info("Looking for reference data modules");
			final ApplyModuleData[] ds = orderModuleByDependency(
			                        ApplyModuleData.selectClientReferenceModules(pool));

			if (ds != null && ds.length > 0) {
				log4j.info(ds.length + " System reference data modules found");
				for (int i = 0; i < ds.length; i++) {
					log4j.info("Importing data from module " + ds[i].name);
					String strPath;
					if (ds[i].adModuleId.equals("0")) strPath = obDir+"/referencedata/standard";
					else strPath = obDir + "/modules/" + ds[i].javapackage+"/referencedata/standard";
					
					final File myDir = new File(strPath);
					final File[] myFiles = myDir.listFiles();
					final ArrayList<File> myTargetFiles = new ArrayList<File>();
					for (int j = 0; j < myFiles.length; j++) {
						if (myFiles[j].getName().endsWith(".xml"))
							myTargetFiles.add(myFiles[j]);
					}
					
					for (final File myF: myTargetFiles) {
					  
						final String strXml = Utility.fileToString(myF.getPath());
						final DataImportService importService = DataImportService
								.getInstance();
						final ImportResult result = importService.importDataFromXML(
								OBDal.getInstance().get(Client.class, "0"),
								OBDal.getInstance()
										.get(Organization.class, "0"), strXml);
						String msg = result.getErrorMessages();
						if (msg!=null && msg.length()>0) log4j.error(result.getErrorMessages());
						
						msg = result.getWarningMessages();
						if (msg!=null && msg.length()>0) log4j.warn(msg);
						
						msg = result.getLogMessages();
						if (msg!=null && msg.length()>0) log4j.debug(msg);
					}

				}
			}

			// **************** Set applied as installed and delete
			// uninstalled************************
			log4j.info("Set modules as installed");
			ApplyModuleData.setInstalled(pool);
			ApplyModuleData.deleteUninstalled(pool);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Returns the modules {@link FieldProvider} ordered taking into account
     * dependencies
     * 
     * @param modules
     * @return
     */
    private ApplyModuleData[] orderModuleByDependency(ApplyModuleData[] modules) {
        if (modules == null || modules.length == 0)
            return null;
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < modules.length; i++) {
            list.add(modules[i].adModuleId);
        }
        final ArrayList<String> orderList = ModuleUtiltiy.orderByDependency(
                pool, list);
        final ApplyModuleData[] rt = new ApplyModuleData[orderList.size()];
        for (int i = 0; i < orderList.size(); i++) {
            int j = 0;
            while (j < modules.length
                    && !modules[j].adModuleId.equals(orderList.get(i)))
                j++;
            rt[i] = modules[j];
        }
        return rt;
    }

    public static void main(String[] args) {
        final ApplyModule am = new ApplyModule(new CPStandAlone(
                "/ws/modularity/openbravo/config/Openbravo.properties"),
                "/ws/modularity/openbravo");
        am.execute();
    }
}
