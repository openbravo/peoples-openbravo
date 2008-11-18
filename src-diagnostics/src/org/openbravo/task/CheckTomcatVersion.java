package org.openbravo.task;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openbravo.utils.PropertiesManager;
import org.openbravo.utils.ServerConnection;
import org.openbravo.utils.Version;

public class CheckTomcatVersion extends Task{
    
    static Logger log4j = Logger.getLogger(CheckTomcatVersion.class);
    
    @Override
    public void execute() throws BuildException {
        log4j.info("Checking tomcat version...");
        final String versionString = new ServerConnection().getCheck("server");
        if (!versionString.contains("Tomcat")) throw new BuildException("Server seems not to be Tomcat");
        final String version = Version.getVersion(versionString);
        final String minVersion = new PropertiesManager().getProperty("tomcat.version");
        final String msg = "Minimun Tomcat version: "+minVersion+", current version: "+version;
        if (Version.compareVersion(version, minVersion)<0)
            throw new BuildException(msg);
        else {
            log4j.info(msg);
            log4j.info("Tomcat version OK");
        }
    }
}
