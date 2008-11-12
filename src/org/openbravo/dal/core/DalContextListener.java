/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.dal.core;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes the dal layer when the servlet container starts.
 * 
 * @author Martin Taal
 */
public class DalContextListener implements ServletContextListener {
    private static Properties obProperties = null;
    private static ServletContext servletContext = null;

    public static ServletContext getServletContext() {
	return servletContext;
    }

    public static void setServletContext(ServletContext context) {
	DalContextListener.servletContext = context;
    }

    public static Properties getOpenBravoProperties() {
	return obProperties;
    }

    public void contextInitialized(ServletContextEvent event) {
	// this allows the sessionfactory controller to use jndi
	SessionFactoryController.setRunningInWebContainer(true);

	final ServletContext context = event.getServletContext();
	setServletContext(context);
	final InputStream is = context
		.getResourceAsStream("/WEB-INF/Openbravo.properties");
	if (is != null) {
	    OBPropertiesProvider.getInstance().setProperties(is);
	}

	// set our own config file provider which uses the servletcontext
	OBConfigFileProvider.getInstance().setServletContext(context);
	OBConfigFileProvider.getInstance().setClassPathLocation("/WEB-INF");

	// initialize the dal layer
	DalLayerInitializer.getInstance().initialize();
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
