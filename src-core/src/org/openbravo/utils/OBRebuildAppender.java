package org.openbravo.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.openbravo.database.ConnectionProviderImpl;

public class OBRebuildAppender extends AppenderSkeleton {

  ConnectionProviderImpl cp;
  Connection connection;
  private static final Logger log4j = Logger.getLogger(OBRebuildAppender.class);
  static File properties = null;
  private static String Basedir;

  public OBRebuildAppender() {
    super();
    try {

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void append(LoggingEvent arg0) {
    if (arg0.getLevel().isGreaterOrEqual(org.apache.log4j.Level.INFO))
      try {
        if (cp == null) {
          File f = new File("");
          f = new File(f.getAbsolutePath());
          System.out.println(f.getAbsolutePath());
          File fProp = null;
          if (Basedir != null)
            fProp = new File(Basedir, "config/Openbravo.properties");
          else {
            if (new File("../../config/Openbravo.properties").exists())
              fProp = new File("../../config/Openbravo.properties");
            else if (new File("../config/Openbravo.properties").exists())
              fProp = new File("../config/Openbravo.properties");
            else if (new File("config/Openbravo.properties").exists())
              fProp = new File("config/Openbravo.properties");
          }
          if (fProp != null)
            properties = fProp;
          cp = new ConnectionProviderImpl(properties.getAbsolutePath());
        }
        if (cp == null) {
          log4j
              .error("Error while initializing connection pool" + (new File("").getAbsolutePath()));
          return;
        }
        if (connection == null || connection.isClosed()) {
          connection = cp.getConnection();
        }
        String message = arg0.getMessage().toString();
        if (message.length() > 3000)
          message = message.substring(0, 2997) + "...";
        PreparedStatement ps = connection
            .prepareStatement("INSERT INTO ad_error_log (ad_error_log_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, system_status, error_level, message) SELECT get_uuid(), '0', '0', 'Y', now(), '0', now(), '0', system_status, ?,? FROM ad_system_info");
        ps.setString(1, arg0.getLevel().toString());
        ps.setString(2, arg0.getMessage().toString());
        ps.executeUpdate();
      } catch (Exception e) {

      }

  }

  @Override
  public void close() {
    try {
      if (connection != null)
        connection.close();
    } catch (Exception e) {
    }

  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  public void setBasedir(String basedir) {
    Basedir = basedir;
  }

  public String getBasedir() {
    return Basedir;
  }

}
