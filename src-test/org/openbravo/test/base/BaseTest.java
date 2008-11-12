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

package org.openbravo.test.base;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.DalLayerInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;

/**
 * Testcase.
 * 
 * @author mtaal
 */

public class BaseTest extends TestCase {

    private boolean errorOccured = false;

    @Override
    protected void setUp() throws Exception {
	initializeDalLayer();
	// clear the session otherwise it keeps the old model
	setBigBazaarUserContext();
	super.setUp();
    }

    protected void initializeDalLayer() throws Exception {
	if (!DalLayerInitializer.getInstance().isInitialized()) {
	    setConfigPropertyFiles();
	    DalLayerInitializer.getInstance().initialize();
	}
    }

    protected void setConfigPropertyFiles() {

	// get the location of the current class file
	final URL url = this.getClass().getResource(
		getClass().getSimpleName() + ".class");
	File f = new File(url.getPath());
	// go up 7 levels
	for (int i = 0; i < 7; i++) {
	    f = f.getParentFile();
	}
	final File configDirectory = new File(f, "config");
	f = new File(configDirectory, "Openbravo.properties");
	if (!f.exists()) {
	    throw new OBException("The testrun assumes that it is run from "
		    + "within eclipse and that the Openbravo.properties "
		    + "file is located as a grandchild of the 7th ancestor "
		    + "of this class");
	}
	OBPropertiesProvider.getInstance().setProperties(f.getAbsolutePath());
	OBConfigFileProvider.getInstance().setFileLocation(
		configDirectory.getAbsolutePath());
    }

    protected void setSystemAdministratorContext() {
	setUserContext("0");
    }

    protected void setBigBazaarUserContext() {
	setUserContext("1000000");
    }

    protected void setUserContext(String userId) {
	OBContext.setOBContext(userId);
    }

    protected void setBigBazaarAdminContext() {
	setUserContext("100");
    }

    @Override
    protected void tearDown() throws Exception {
	try {
	    if (SessionHandler.isSessionHandlerPresent()) {
		if (SessionHandler.getInstance().getDoRollback()) {
		    SessionHandler.getInstance().rollback();
		} else if (isErrorOccured()) {
		    SessionHandler.getInstance().rollback();
		} else {
		    SessionHandler.getInstance().commitAndClose();
		}
	    }
	} catch (Exception e) {
	    SessionHandler.getInstance().rollback();
	    reportException(e);
	    throw e;
	} finally {
	    SessionHandler.deleteSessionHandler();
	    OBContext.setOBContext((OBContext) null);
	}
	super.tearDown();
    }

    protected void reportException(Exception e) {
	if (e == null)
	    return;
	e.printStackTrace(System.err);
	if (e instanceof SQLException) {
	    reportException(((SQLException) e).getNextException());
	}
    }

    public boolean isErrorOccured() {
	return errorOccured;
    }

    public void setErrorOccured(boolean errorOccured) {
	this.errorOccured = errorOccured;
    }
}