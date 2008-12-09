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

package org.openbravo.service.db;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.scheduling.ProcessBundle;

/**
 * The export client process is called from the ui. It exports all the data from
 * one client using a specific dataset. It again calls the
 * {@link DataExportService} for the actual export.
 * 
 * @author mtaal
 */

public class ExportClientProcess implements org.openbravo.scheduling.Process {

    /**
     * Executes the export process. The expected parameters in the bundle are
     * clientId (denoting the client) and fileLocation giving the full path
     * location of the file in which the data for the export should go.
     */
    public void execute(ProcessBundle bundle) throws Exception {

        try {
            final URL url = org.openbravo.dal.core.DalContextListener
                    .getServletContext().getResource("/WEB-INF/referencedata");
            final File file = new File(new URI(url.toString()));
        } catch (final Exception e) {
            throw new OBException(e);
        }
        for (final String key : bundle.getParams().keySet()) {
            System.err.println(key + ": " + bundle.getParams().get(key));
        }
        final OBError e = new OBError();
        e.setType("Success");
        e.setMessage("ClientID:" + bundle.getParams().get("adClientId"));
        e.setTitle("Done");

        bundle.setResult(e);
    }
}