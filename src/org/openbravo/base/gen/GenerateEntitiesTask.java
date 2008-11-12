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

package org.openbravo.base.gen;

import org.apache.log4j.Logger;
import org.openarchitectureware.workflow.ant.WorkflowAntTask;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.OBClassLoader;

/**
 * Task which initializes the model layer. All other work is done by the
 * superclass.
 * 
 * @author Martin Taal
 */
public class GenerateEntitiesTask extends WorkflowAntTask {
    private static final Logger log = Logger
	    .getLogger(GenerateEntitiesTask.class);

    private String propertiesFile;
    private String providerConfigDirectory;
    private boolean debug;

    public String getPropertiesFile() {
	return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
	this.propertiesFile = propertiesFile;
    }

    @Override
    public final void execute() {
	if (debug) {
	    OBProvider.getInstance().register(OBClassLoader.class,
		    OBClassLoader.ClassOBClassLoader.class, false);

	    log.debug("initializating dal layer, getting properties from "
		    + getPropertiesFile());
	    OBPropertiesProvider.getInstance().setProperties(
		    getPropertiesFile());

	    if (getProviderConfigDirectory() != null) {
		OBConfigFileProvider.getInstance().setFileLocation(
			getProviderConfigDirectory());
	    }

	    log.info("Initializing in-memory model...");
	    try {
		ModelProvider.getInstance().getModel();
	    } catch (final Exception e) {
		e.printStackTrace(System.err);
		throw new OBException(e);
	    }
	}
	super.execute();
    }

    public String getProviderConfigDirectory() {
	return providerConfigDirectory;
    }

    public void setProviderConfigDirectory(String providerConfigDirectory) {
	this.providerConfigDirectory = providerConfigDirectory;
    }

    public boolean isDebug() {
	return debug;
    }

    public void setDebug(boolean debug) {
	this.debug = debug;
    }
}
