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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

package org.openbravo.base;

import org.openbravo.base.secureApp.SystemPreferencesData;
import org.openbravo.erpCommon.ad_background.PeriodicBackground;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.database.ConnectionProvider;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * A ServletContextListener to startup and shutdown the periodic background process.
 *
 * @author Ben Sommerville
 */
public class PeriodicBackgroundContextListener implements ServletContextListener {

    public static final String BACKGROUND_ATTRIBUTE = "openbravoBackgroundProcesses";


    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        ConfigParameters configParameters = ConfigParameters.retrieveFrom(context);

        Map<String, PeriodicBackground> backgroundProcess = new HashMap<String, PeriodicBackground>();
        context.setAttribute(BACKGROUND_ATTRIBUTE, backgroundProcess);

        if (configParameters.havePeriodicBackgroundTime() && configParameters.haveLogFileAcctServer() ) {
            ConnectionProvider connectionProvider = ConnectionProviderContextListener.getPool(context);

            SystemPreferencesData[] backgroundData = null;
            try {
                backgroundData = SystemPreferencesData.selectBackground(connectionProvider);
            } catch (ServletException sex) {
                sex.printStackTrace();
            }
            if (backgroundData != null && backgroundData.length > 0) {
                for (int countBack = 0; countBack < backgroundData.length; countBack++) {
                    PeriodicBackground object = null;
                    try {
                        object = new PeriodicBackground(connectionProvider, configParameters.getPeriodicBackgroundTime(),
                                                            configParameters.strLogFileAcctServer,
                                                            backgroundData[countBack].id,
                                                            backgroundData[countBack].classname);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    if (object != null) {
                        object.start();
                        try {
                            object.setActive(SystemPreferencesData.isActive(connectionProvider, backgroundData[countBack].id));
                        } catch (ServletException sex) {
                            sex.printStackTrace();
                        }
                        backgroundProcess.put(backgroundData[countBack].id, object);
                    }
                }
            }
        }
    }


    public void contextDestroyed(ServletContextEvent event) {
        Map<String, PeriodicBackground> processes = getBackgroundProcesses(event.getServletContext());
        for(PeriodicBackground object : processes.values()) {
            object.destroy();
        }
        processes.clear();
    }

    private static  Map<String, PeriodicBackground> getBackgroundProcesses(ServletContext context) {
        return (Map<String, PeriodicBackground>) context.getAttribute(BACKGROUND_ATTRIBUTE);
    }



    public static synchronized PeriodicBackground getBackgroundProcess(ServletContext context, String processId) {
        return getBackgroundProcesses(context).get(processId);
    }

    public static synchronized void updateBackgroundProcess(ServletContext context, String processId, PeriodicBackground process) {
        Map<String, PeriodicBackground> processes = getBackgroundProcesses(context);
        PeriodicBackground oldProcess = processes.put(processId,process);
        if( oldProcess != null ) {
            oldProcess.destroy();
        }
    }


}
