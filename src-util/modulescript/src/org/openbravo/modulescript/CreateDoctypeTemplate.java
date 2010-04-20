package org.openbravo.modulescript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;

public class CreateDoctypeTemplate extends ModuleScript{

	@Override
	//Inserting Doctype template for shipment and receipt.Related to the issue https://issues.openbravo.com/view.php?id=11996
	public void execute() {
		try {
		      ConnectionProvider cp = getConnectionProvider();
		      CreateDoctypeTemplateData[] data = CreateDoctypeTemplateData.select(cp);
		      for (int i = 0; i < data.length; i++) {
				
		    	  if(data[i].vCount.equals("0")){
		    		  String strReportFileName="Goods Shipment-@our_ref@";
		    		  if(data[i].docbasetype.equals("MMR")){
		    			  strReportFileName="Goods Receipt-@our_ref@";
		    		  }
		    		  String strDoctypeTemplate_id=UUID.randomUUID().toString().replace("-", "").toUpperCase();
		    		  CreateDoctypeTemplateData.insertDoctypeTemplate(cp.getConnection(), cp, strDoctypeTemplate_id, data[i].adClientId, data[i].cDoctypeId, data[i].name+" Report Template", "@basedesign@/org/openbravo/erpReports", strReportFileName, "RptM_InOut.jrxml");
		    		  CreateDoctypeTemplateData.insertEmailDefinition(cp.getConnection(), cp, data[i].adClientId, strDoctypeTemplate_id);
		    	  }
			}
		    } catch (Exception e) {
		    	handleError(e);
		    }

	}
	
	

}