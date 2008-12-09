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

package org.openbravo.test.ant;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.AntExecutor;

/**
 * Base test class for ant test cases.
 * 
 * @author mtaal
 */

public class BaseAntTest extends TestCase {
    private static final Logger log = Logger.getLogger(BaseAntTest.class);

    @Override
    protected void setUp() throws Exception {
        setConfigPropertyFiles();
        super.setUp();
    }

    protected void doTest(String task) {
        doTest(task, null);
    }

    protected void doTest(String task, String additionalPath) {
        log.info("Running ant task " + task);
        final PrintWriter outputLogWriter = new PrintWriter(System.err);

        try {
            final AntExecutor ant = new AntExecutor(getProperty("source.path")
                    + (additionalPath != null ? "/" + additionalPath : ""));
            ant.setOBPrintStreamLog(System.err);
            // ant.setLogLevel(Project.MSG_INFO);
            // ant.setOBPrintStreamLog(null);// outputLogWriter);
            ant.runTask(task);
            final String result = ant.getErr();
            if (result.indexOf("Success") != -1) {
                return;
            } else {
                System.err.println(result);
                fail(result);
            }
        } catch (final Exception e) {
            throw new OBException(e);
        } finally {
            outputLogWriter.close();
        }
    }

    protected String getProperty(String propertyName) {
        return OBPropertiesProvider.getInstance().getOpenbravoProperties()
                .getProperty(propertyName);
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
    }

}