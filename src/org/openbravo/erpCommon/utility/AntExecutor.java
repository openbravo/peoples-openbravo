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
package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Vector;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.openbravo.base.exception.OBException;
import org.openbravo.utils.OBLogAppender;

/**
 * The AntExecutor class allows to execute ant tasks in a given build.xml file.
 * 
 * 
 */
public class AntExecutor {
    private Project project;
    private String baseDir;
    private OBPrintStream log;
    private OBPrintStream err;
    private PrintWriter out;

    /**
     * Initializes a newly created AntExecutor object assigning it the build.xml
     * file to execute tasks from and the base directory where they will be
     * executed.
     * 
     * @param buildFile
     *            - Complete path to the build.xml file
     * @param baseDir
     *            - Complete path to the base directory
     * @throws Exception
     *             - if an error occurs loading the xml file
     */
    public AntExecutor(String buildFile, String baseDir) throws Exception {
        project = new Project();
        this.baseDir = (baseDir == null || baseDir.equals("") ? "." : baseDir);
        try {
            project.init();
            project.setBasedir(this.baseDir);
            ProjectHelper.getProjectHelper()
                    .parse(project, new File(buildFile));
        } catch (final BuildException e) {
            throw new Exception("ErrorLoadingBuildXML");
        }
    }

    /**
     * Initializes a new AntExecutor object assigning it the build.xml file in
     * the directory passed as parameter, the base directory is the same as the
     * one the build.xml is in.
     * 
     * @param buildDir
     *            - Directory where is the build.xml file and that will be the
     *            base directory
     * @throws Exception
     *             - if an error occurs loading the xml file
     */
    public AntExecutor(String buildDir) throws Exception {
        this(buildDir + "/build.xml", buildDir);
    }

    public void setPrintWriter(PrintWriter p) {
        out = p;
    }

    /**
     * Sets a file where the execution log will be saved.
     * 
     * @param directory
     *            - Path to the directory for the file
     * @param logFileName
     *            - Name of the log file
     * @return - The complete file name (including directory)
     * @throws Exception
     */
    public String setLogFile(String directory, String logFileName)
            throws Exception {
        // DefaultLogger logger = new DefaultLogger();
        final FileOutputStream logFile = new FileOutputStream(directory + "/"
                + logFileName);
        // PrintStream ps = new PrintStream(logFile);
        // logger.setOutputPrintStream(ps);
        // logger.setErrorPrintStream(ps);
        // logger.setMessageOutputLevel(Project.MSG_INFO);
        // project.addBuildListener(logger);
        return directory + "/" + logFileName;
    }

    /**
     * Sets a file where the execution log will be saved. It only receives the
     * file name, the path is the log directory inside the base directory
     * 
     * @param name
     *            - File name
     * @return - The complete file name (including directory)
     * @throws Exception
     */
    public String setLogFile(String name) throws Exception {
        final File dir = new File(baseDir + "/log");
        if (!dir.exists())
            if (!dir.mkdir())
                return null;
        return setLogFile(baseDir + "/log", name);
    }

    public void setLogFileInOBPrintStream(File f) {
        log.setLogFile(f);
        err.setLogFile(f);
    }

    /**
     * Sets two OBPrintStream objects to maintain the execution log. One is for
     * standard log and the other one for the errors.
     * 
     * @see OBPrintStream
     */
    public void setOBPrintStreamLog(PrintWriter p) {
        setPrintWriter(p);
        final DefaultLogger logger1 = new DefaultLogger();
        final OBPrintStream ps1 = new OBPrintStream(out);
        final OBPrintStream ps2 = new OBPrintStream(out);
        logger1.setOutputPrintStream(ps1);
        logger1.setErrorPrintStream(ps2);
        logger1.setMessageOutputLevel(Project.MSG_INFO);
        project.addBuildListener(logger1);
        err = ps2;
        log = ps1;
    }

    public void setOBPrintStreamLog(PrintStream p) {
        final DefaultLogger logger1 = new DefaultLogger();
        final OBPrintStream ps1 = new OBPrintStream(p);
        final OBPrintStream ps2 = new OBPrintStream(p);
        logger1.setOutputPrintStream(ps1);
        logger1.setErrorPrintStream(ps2);
        logger1.setMessageOutputLevel(Project.MSG_INFO);
        project.addBuildListener(logger1);
        err = ps2;
        log = ps1;

        // force log4j to also print to this response
        OBLogAppender.setOutputStream(ps1);

    }

    /**
     * Set a value to a property to the project.
     * 
     * @param property
     *            - Property name
     * @param value
     *            - Value to assign
     */
    public void setProperty(String property, String value) {
        project.setProperty(property, value);
    }

    /**
     * Executes an ant task
     * 
     * @param task
     *            - Name of the task to execute
     * @throws Exception
     *             - In case the project is not loaded
     */
    public void runTask(String task) throws Exception {
        if (project == null)
            throw new Exception("NoProjectLoaded");
        if (task == null)
            task = project.getDefaultTarget();
        try {
            project.executeTarget(task);
        } catch (final BuildException e) {
            e.printStackTrace();
            err.print(e.toString());
        }
    }

    /**
     * Executes a set of ant tasks
     * 
     * @param tasks
     *            - A Vector<String> with the names of the tasks to be executed
     * @throws Exception
     *             - In case the project is not loaded
     */
    public void runTask(Vector<String> tasks) throws Exception {
        if (project == null)
            throw new Exception("NoProjectLoaded");
        try {
            project.executeTargets(tasks);
        } catch (final BuildException e) {
            e.printStackTrace();
            err.print(e.toString());
        }
    }

    /**
     * Sets the finished attribute to the log. It used for loggin purposes.
     * 
     * @see OBPrintStream
     * 
     * @param v
     *            - boolean value to set
     */
    public void setFinished(boolean v) {
        log.setFinished(v);
        if (out != null)
            out.close();
    }

    /**
     * Returns an String with the log generated after the las getLog() call.
     * 
     * @return - log String
     */
    /*
     * public String getLog() { return log.getLog(OBPrintStream.TEXT_HTML); }
     */

    /**
     * Returns an String with error messages, in case no error is logged a
     * "Success" String is returned.
     * 
     * @return - error String
     */
    public String getErr() {
        String rt = err.getLog(OBPrintStream.TEXT_PLAIN);
        if (rt == null || rt.equals("")) {
            final String mode = project.getProperty("deploy.mode");
            rt = "SuccessRebuild." + mode;
        }
        return rt;
    }

    private class LocalListener implements BuildListener {

        private final OutputStream os;

        private LocalListener(OutputStream os) {
            this.os = os;
        }

        @Override
        public void buildFinished(BuildEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void buildStarted(BuildEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void messageLogged(BuildEvent arg0) {
            try {
                if (arg0.getMessage() != null) {
                    os.write(arg0.getMessage().getBytes());
                }
                if (arg0.getException() != null) {
                    os.write(arg0.getException().getMessage().getBytes());
                }
            } catch (final IOException e) {
                throw new OBException(e);
            }
        }

        @Override
        public void targetFinished(BuildEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void targetStarted(BuildEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void taskFinished(BuildEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void taskStarted(BuildEvent arg0) {
            // TODO Auto-generated method stub

        }

    }

}
