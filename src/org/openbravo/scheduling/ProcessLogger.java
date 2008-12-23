package org.openbravo.scheduling;

import java.sql.Timestamp;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;

public class ProcessLogger {

    private StringBuilder log;

    private ConnectionProvider connection;

    public ProcessLogger(ConnectionProvider conn) {
        this.connection = conn;
        log = new StringBuilder();
    }

    public String getLog() {
        return log.toString();
    }

    /**
     * @param msgKey
     * @return
     */
    public String messageDb(String msgKey, String language) {
        return Utility.messageBD(connection, msgKey, language);
    }

    /**
     * @param msg
     * @param log
     */
    public void log(String msg) {
        log.append(new Timestamp(System.currentTimeMillis()).toString() + " - "
                + msg);
    }

    /**
     * @param msg
     */
    public void logln(String msg) {
        log(msg + "\n");
    }

}
