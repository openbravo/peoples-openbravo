/*
 ************************************************************************************
 * Copyright (C) 2001-2008 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/

package org.openbravo.base;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.exception.PoolNotFoundException;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Manage the creation and destruction of the db connection pool..
 *
 * @author Ben Sommerville
 */
public class ConnectionProviderContextListener implements ServletContextListener {
    public static final String POOL_ATTRIBUTE = "openbravoPool";
    private static Logger log4j = Logger.getLogger(ConnectionProviderContextListener.class);



    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        ConfigParameters configParameters = ConfigParameters.retrieveFrom(context);

        try {
            ConnectionProvider pool = createPool(configParameters);
            context.setAttribute(POOL_ATTRIBUTE, pool);
        } catch (PoolNotFoundException e) {
            log4j.error("Unable to create a connection pool", e);
        }

    }


    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        destroyPool(getPool(context));
        context.removeAttribute(POOL_ATTRIBUTE);
    }


    public static ConnectionProvider getPool(ServletContext context) {
         return (ConnectionProvider) context.getAttribute(POOL_ATTRIBUTE);
    }

    public static void reloadPool(ServletContext context) throws Exception {
        ConnectionProvider pool = getPool(context);
        if( pool instanceof ConnectionProviderImpl ) {
            ConfigParameters configParameters = ConfigParameters.retrieveFrom(context);
            String strPoolFile = configParameters.getPoolFilePath();
            boolean isRelative = !strPoolFile.startsWith("/") && !strPoolFile.substring(1, 1).equals(":");
            ((ConnectionProviderImpl)pool).reload(strPoolFile, isRelative, configParameters.strContext);;
        }
    }

    private ConnectionProvider createPool( ConfigParameters configParameters) throws PoolNotFoundException {
        return createXmlPool(configParameters);
    }


    private static ConnectionProvider createXmlPool(ConfigParameters configParameters) throws PoolNotFoundException {
        try {
            String strPoolFile = configParameters.getPoolFilePath();
            boolean isRelative = !strPoolFile.startsWith("/") && !strPoolFile.substring(1, 1).equals(":");
            return new ConnectionProviderImpl(strPoolFile, isRelative, configParameters.strContext);
        } catch (Exception ex) {
            throw new PoolNotFoundException(ex.getMessage());
        }
    }


    private static void destroyPool(ConnectionProvider pool) {
        if( pool != null && pool instanceof ConnectionProviderImpl) {
            try {
                ((ConnectionProviderImpl)pool).destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
