package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_process.AcctServerProcessData;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.scheduling.ProcessBundle.Channel;

public class AcctServerProcess implements Process {
  
  public final static String BATCH_SIZE = "50";
  
  private boolean isDirect;
  
  private StringBuffer lastLog = new StringBuffer();
  private StringBuffer message = new StringBuffer();
  
  private String[] TableIds = null;
  
  private ProcessLogger logger;
  private ConnectionProvider connection;
  
  public void initialize(ProcessBundle bundle) {
    logger = bundle.getLogger();
    connection = bundle.getConnection();
    
  }
  
  public void execute(ProcessBundle bundle) throws Exception {
    
    VariablesSecureApp vars = bundle.getContext().toVars();
    
    String processId = bundle.getProcessId();
    String pinstanceId = bundle.getPinstanceId();
    
    ProcessContext ctx = bundle.getContext();
    isDirect = bundle.getChannel() == Channel.DIRECT;
    
    String adNoteId = "";
    if(isDirect) {
      addLog("@DL_STARTING@", false);
    }else {
      addLog("Starting background process.");
    }
    if (vars == null) {
      try {
        AcctServerProcessData[] dataOrg = AcctServerProcessData.selectUserOrg(connection, processId);
        if (dataOrg == null || dataOrg.length == 0) {
          if(isDirect) {
            addLog("@DL_LOAD_FAILED@");
          } else {
            addLog("User and Organization loading failed.");
          }
          return;
        }
        vars = new VariablesSecureApp(dataOrg[0].adUserId, ctx.getClient(), dataOrg[0].adOrgId);
      } catch (ServletException ex) {
        ex.printStackTrace();
        return;
      }
    }
    try {
      AcctServerProcessData [] data = AcctServerProcessData.selectAcctTable(connection);
      ArrayList<Object> vTableIds = new ArrayList<Object>();
      for (int i=0;i<data.length;i++){
        vTableIds.add(data[i].adTableId);
      }
      TableIds = new String [vTableIds.size()];
      vTableIds.toArray(TableIds);
    } catch (ServletException ex) {
      ex.printStackTrace();
      return;
    }
    adNoteId = saveLog(adNoteId, vars.getClient());
    String[] tables=null;
    String strTable = "";
    String strOrg="0";
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
    for (int i=0;i<tables.length;i++){
      AcctServer acct = AcctServer.get(tables[i], vars.getClient(), strOrg, connection);
      acct.setBatchSize(BATCH_SIZE);
      strTableDesc = AcctServerProcessData.selectDescription(connection, ctx.getLanguage(), acct.AD_Table_ID);
      int total = 0;
      while (acct.checkDocuments()) {
        
        if (total==0) {
          if(isDirect)
            addLog("@DL_ACCOUNTING@ - "+ strTableDesc, false);
          else
            addLog("Accounting - " + strTableDesc, false);
        }
        else {
          if(isDirect)
            addLog("@DL_COUNTED@ " + total + " - " + strTableDesc, false);
          else
            addLog("Counted " + total + " - " + strTableDesc, false);
        }
        try {
          acct.run(vars);
          
        } catch(IOException ex) {
          ex.printStackTrace();
          return;
        } 
        if(isDirect) {
          addLog("@DL_TABLE@ = "+strTableDesc + " - " + acct.getInfo(ctx.getLanguage()), false);
        } else {
          addLog("Table = " + strTableDesc + " - " + acct.getInfo(ctx.getLanguage()));
          adNoteId = saveLog(adNoteId, vars.getClient());
          return;
        }
        total += Integer.valueOf(BATCH_SIZE).intValue();
      }
      if(isDirect) {
        addLog("@DL_TABLE@ = " + strTableDesc + " - " + acct.getInfo(ctx.getLanguage()), false);
      } else {
        addLog("Table = " + strTableDesc + " - " + acct.getInfo(ctx.getLanguage()));
      }
      adNoteId = saveLog(adNoteId, vars.getClient());
    }
  }
  
  /**
   * @param texto
   */
  public void addLog(String msg) {
    addLog(msg, true);
  }

  /**
   * @param texto
   * @param generalLog
   */
  public void addLog(String msg, boolean generalLog) {
    logger.log(msg + "\n");
    Timestamp tmp = new Timestamp(System.currentTimeMillis());
    if(isDirect){
      lastLog.append("<span>").append(msg).append("</span><br>");
    }
    else {
      if (generalLog) {
        this.message.append(tmp.toString()).append(" - ").append(msg).append("<br>");
      }
      lastLog.append("<span>").append(tmp.toString()).append(" - ").append(msg).append("</span><br>");
    }
  }
  
  /**
   * @param adNoteId
   * @param adClientId
   * @return
   */
  public String saveLog(String adNoteId, String adClientId) {
    String strMessage="", strNewMessage="";
    try {
      if (adNoteId==null || adNoteId.equals("")) {
        adNoteId = SequenceIdData.getUUID();
        AcctServerProcessData.insert(connection, adNoteId, adClientId, "");
      }
      if (this.message.length()>2000) {
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
    
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    return adNoteId;
  }

}
