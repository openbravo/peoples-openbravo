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

package org.openbravo.base.session;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.util.Check;

/**
 * Is used to read and provide the Openbravo.properties
 * 
 * TODO: change to single ton pattern, check if ant can work with this
 * 
 * @author Martin Taal
 */
public class OBPropertiesProvider {
    private final Logger log = Logger.getLogger(OBPropertiesProvider.class);

    private Properties obProperties = null;

    private static OBPropertiesProvider instance = new OBPropertiesProvider();

    public static OBPropertiesProvider getInstance() {
        return instance;
    }

    public static void setInstance(OBPropertiesProvider instance) {
        OBPropertiesProvider.instance = instance;
    }

    public Properties getOpenbravoProperties() {
        return obProperties;
    }

    public void setProperties(InputStream is) {
        Check
                .isNull(obProperties,
                        "Openbravo properties have already been set");
        log.debug("Setting openbravo.properties through input stream");
        obProperties = new Properties();
        try {
            obProperties.load(is);
            is.close();
        } catch (Exception e) {
            throw new OBException(e);
        }
    }

    public void setProperties(Properties props) {
        Check
                .isNull(obProperties,
                        "Openbravo properties have already been set");
        log.debug("Setting openbravo.properties through properties");
        obProperties = new Properties();
        obProperties.putAll(props);
    }

    public void setProperties(String fileLocation) {
        // Check.isNull(obProperties,
        // "Openbravo properties have already been set");
        log.debug("Setting openbravo.properties through a file");
        obProperties = new Properties();
        try {
            final FileInputStream fis = new FileInputStream(fileLocation);
            obProperties.load(fis);
            fis.close();
        } catch (Exception e) {
            throw new OBException(e);
        }
    }
}