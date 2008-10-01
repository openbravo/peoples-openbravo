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

import org.openbravo.base.session.OBPropertiesProvider;

/**
 * Is used to read the Openbravo.properties
 * 
 * @author Martin Taal
 */
public class DalContextListener implements ServletContextListener {
  private static Properties obProperties = null;
  
  public static Properties getOpenBravoProperties() {
    return obProperties;
  }
  
  public void contextInitialized(ServletContextEvent event) {
    final ServletContext context = event.getServletContext();
    final InputStream is = context.getResourceAsStream("/WEB-INF/Openbravo.properties");
    if (is != null) {
      OBPropertiesProvider.getInstance().setProperties(is);
    }
  }
  
  public void contextDestroyed(ServletContextEvent event) {
  }
}
