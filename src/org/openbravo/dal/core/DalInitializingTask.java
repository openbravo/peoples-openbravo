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

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.dal.service.OBDal;

/**
 * A task which initializes the dal and then calls a doExecute method which can
 * be subclassed.
 * 
 * @author Martin Taal
 */
public class DalInitializingTask extends Task {
    private static final Logger log = Logger
            .getLogger(DalInitializingTask.class);

    protected String propertiesFile;
    protected String userId;
    private String providerConfigDirectory;

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void execute() {
        OBProvider.getInstance().register(OBClassLoader.class,
                OBClassLoader.ClassOBClassLoader.class, false);

        if (!DalLayerInitializer.getInstance().isInitialized()) {
            log.debug("initializating dal layer, getting properties from "
                    + getPropertiesFile());
            OBPropertiesProvider.getInstance().setProperties(
                    getPropertiesFile());

            if (getProviderConfigDirectory() != null) {
                OBConfigFileProvider.getInstance().setFileLocation(
                        getProviderConfigDirectory());
            }

            DalLayerInitializer.getInstance().initialize();
        } else {
            log.debug("Dal Layer already initialized");
        }
        boolean errorOccured = true;
        try {
            log.debug("Setting user context to user " + getUserId());
            OBContext.setOBContext(getUserId());
            doExecute();
            errorOccured = false;
        } finally {
            if (errorOccured) {
                OBDal.getInstance().rollbackAndClose();
            } else {
                OBDal.getInstance().commitAndClose();
            }
        }
    }

    protected void doExecute() {
    }

    public String getProviderConfigDirectory() {
        return providerConfigDirectory;
    }

    public void setProviderConfigDirectory(String providerConfigDirectory) {
        this.providerConfigDirectory = providerConfigDirectory;
    }
}
