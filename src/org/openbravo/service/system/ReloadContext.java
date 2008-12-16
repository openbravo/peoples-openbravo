package org.openbravo.service.system;

import java.io.File;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.AntExecutor;

/**
 * Reloads the tomcat using the tomcat reload ant task.
 * 
 * @author mtaal
 */
public class ReloadContext {
    private static final Logger log = Logger.getLogger(ReloadContext.class);

    /**
     * Method is called from the tomcat.reload tasks, this method again starts
     * the tomcat.reload.do task.
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
            log.debug("Reloading context with basedir " + baseDir);
            final AntExecutor antExecutor = new AntExecutor(baseDir
                    .getAbsolutePath());
            antExecutor.runTask("tomcat.reload.do");
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            throw new OBException(e);
        }
    }

    /**
     * Restarts the tomcat server. Assumes the the Openbravo.properties are
     * available through the {@link OBPropertiesProvider}.
     */
    public static void reload() {
        final String baseDirPath = OBPropertiesProvider.getInstance()
                .getOpenbravoProperties().getProperty("source.path");
        try {
            log.debug("Reloading context with basedir " + baseDirPath);
            final AntExecutor antExecutor = new AntExecutor(baseDirPath);
            antExecutor.runTask("tomcat.reload");
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            throw new OBException(e);
        }
    }
}
