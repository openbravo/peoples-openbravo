package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.scheduling.ProcessBundle.Channel;
import org.openbravo.service.db.DalBaseProcess;

public class AcctServerProcess extends DalBaseProcess {

  public final static String BATCH_SIZE = "50";

  private boolean isDirect;

  private StringBuffer lastLog = new StringBuffer();
  private StringBuffer message = new StringBuffer();

  private String[] TableIds = null;

  private ProcessLogger logger;
  private ConnectionProvider connection;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    VariablesSecureApp vars = bundle.getContext().toVars();

    final String processId = bundle.getProcessId();
    final String pinstanceId = bundle.getPinstanceId();

    final ProcessContext ctx = bundle.getContext();
    isDirect = bundle.getChannel() == Channel.DIRECT;

    String adNoteId = "";
    if (isDirect) {
      addLog("@DL_STARTING@", false);
    } else {
      addLog("Starting background process.");
    }
    if (vars == null) {
      try {
        final AcctServerProcessData[] dataOrg = AcctServerProcessData.selectUserOrg(connection,
            processId);
        if (dataOrg == null || dataOrg.length == 0) {
          if (isDirect) {
            addLog("@DL_LOAD_FAILED@");
          } else {
            addLog("User and Organization loading failed.");
          }
          return;
        }
        vars = new VariablesSecureApp(dataOrg[0].adUserId, ctx.getClient(), dataOrg[0].adOrgId);
      } catch (final ServletException ex) {
        ex.printStackTrace();
        return;
      }
    }
    try {
      final AcctServerProcessData[] data = AcctServerProcessData.selectAcctTable(connection);
      final ArrayList<Object> vTableIds = new ArrayList<Object>();
      for (int i = 0; i < data.length; i++) {
        vTableIds.add(data[i].adTableId);
      }
      TableIds = new String[vTableIds.size()];
      vTableIds.toArray(TableIds);
    } catch (final ServletException ex) {
      ex.printStackTrace();
      return;
    }
    adNoteId = saveLog(adNoteId, vars.getClient());
    String[] tables = null;
    String strTable = "";
    // fill organisation from process request by default
    String strOrg = ctx.getOrganization();
    // if called by 'Posting by DB tables' get params from ad_pinstance
    if (isDirect) {
      strTable = AcctServerProcessData.selectTable(connection, pinstanceId);
      strOrg = AcctServerProcessData.selectOrg(connection, pinstanceId);
    }
    if (!strTable.equals("")) {
      tables = new String[1];
      tables[0] = new String(strTable);
    } else {
      tables = TableIds;
    }
    String strTableDesc;
    for (int i = 0; i < tables.length; i++) {
      final AcctServer acct = AcctServer.get(tables[i], vars.getClient(), strOrg, connection);
      acct.setBatchSize(BATCH_SIZE);
      strTableDesc = AcctServerProcessData.selectDescription(connection, ctx.getLanguage(),
          acct.AD_Table_ID);
      int total = 0;
      while (acct.checkDocuments()) {

        if (total == 0) {
          if (isDirect)
            addLog("@DL_ACCOUNTING@ - " + strTableDesc, false);
          else
            addLog("Accounting - " + strTableDesc, false);
        } else {
          if (isDirect)
            addLog("@DL_COUNTED@ " + total + " - " + strTableDesc, false);
          else
            addLog("Counted " + total + " - " + strTableDesc, false);
        }
        try {
          acct.run(vars);

        } catch (final IOException ex) {
          ex.printStackTrace();
          return;
        }
        total += Integer.valueOf(BATCH_SIZE).intValue();
      }
      if (isDirect) {
        addLog("@DL_TABLE@ = " + strTableDesc + " - " + acct.getInfo(ctx.getLanguage()), false);
      } else {
        addLog("Table = " + strTableDesc + " - " + acct.getInfo(ctx.getLanguage()));
      }
      adNoteId = saveLog(adNoteId, vars.getClient());
    }
  }

  /**
   * Adds a message to the log.
   * 
   * @param msg
   *          to add to the log
   */
  public void addLog(String msg) {
    addLog(msg, true);
  }

  /**
   * Add a message to the log.
   * 
   * @param msg
   * @param generalLog
   */
  public void addLog(String msg, boolean generalLog) {
    logger.log(msg + "\n");
    final Timestamp tmp = new Timestamp(System.currentTimeMillis());
    if (isDirect) {
      lastLog.append("<span>").append(msg).append("</span><br>");
    } else {
      if (generalLog) {
        this.message.append(tmp.toString()).append(" - ").append(msg).append("<br>");
      }
      lastLog.append("<span>").append(tmp.toString()).append(" - ").append(msg).append(
          "</span><br>");
    }
  }

  /**
   * Saves the log as a note.
   * 
   * @param adNoteId
   *          the note id, if passed as null then a new one is created
   * @param adClientId
   * @return the id of the note if a new one is created (if passed as null)
   */
  public String saveLog(String adNoteId, String adClientId) {
    String strMessage = "", strNewMessage = "";
    try {
      if (adNoteId == null || adNoteId.equals("")) {
        adNoteId = SequenceIdData.getUUID();
        AcctServerProcessData.insert(connection, adNoteId, adClientId, "");
      }
      if (this.message.length() > 2000) {
        strMessage = this.message.toString().substring(0, 1990) + "...";
        strNewMessage = this.message.toString().substring(1990);
        this.message.setLength(0);
        this.message.append("...").append(strNewMessage);
      } else {
        strMessage = this.message.toString();
      }
      AcctServerProcessData.update(connection, strMessage, adNoteId);
      if (!strNewMessage.equals("")) {
        adNoteId = saveLog("", adClientId);
      }

    } catch (final ServletException ex) {
      ex.printStackTrace();
    }
    return adNoteId;
  }

}
