package org.openbravo.service.system;

import java.io.File;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.AntExecutor;

/**
 * Restarts the tomcat using the direct java bootstrap class/jar. The restart is
 * an ant task which itself calls another ant task. The reason that the first
 * ant task is required because the ant task needs to be spawned and only a java
 * or exec task can be spawned. The stop and restart action are two java tasks
 * which should be done in one step. Therefore these two actions have to be
 * combined into one task which is called by the first (spawned) java task.
 * 
 * @author mtaal
 */
public class RestartTomcat {
    private static final Logger log = Logger.getLogger(RestartTomcat.class);

    /**
     * Method is called from the tomcat.restart tasks, this method again starts
     * the tomcat.restart.do task.
     * 
     * @param args
     *            arg[0] contains the source path
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final String srcPath = args[0];
        final File srcDir = new File(srcPath);
        final File baseDir = srcDir.getParentFile();
        try {
            log.debug("Restarting tomcat with basedir " + baseDir);
            final AntExecutor antExecutor = new AntExecutor(baseDir
                    .getAbsolutePath());
            antExecutor.runTask("tomcat.restart.do");
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            throw new OBException(e);
        }
    }

    /**
     * Restarts the tomcat server. Assumes the the Openbravo.properties are
     * available through the {@link OBPropertiesProvider}.
     */
    public static void restart() {
        final String baseDirPath = OBPropertiesProvider.getInstance()
                .getOpenbravoProperties().getProperty("source.path");
        try {
            log.debug("Restarting tomcat with basedir " + baseDirPath);
            final AntExecutor antExecutor = new AntExecutor(baseDirPath);
            antExecutor.runTask("tomcat.restart");
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            throw new OBException(e);
        }
    }
}
