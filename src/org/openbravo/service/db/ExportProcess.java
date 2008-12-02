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

import org.openbravo.scheduling.ProcessBundle;

/**
 * The export process is called from the ui. It again calls the
 * {@link DataExportService} for the actual export.
 * 
 * @author mtaal
 */

public class ExportProcess implements org.openbravo.scheduling.Process {

    /**
     * Executes the export process. The expected parameters in the bundle are
     * clientId (denoting the client) and fileLocation giving the full path
     * location of the file in which the data for the export should go.
     */
    @Override
    public void execute(ProcessBundle bundle) throws Exception {
        // TODO Auto-generated method stub
    }
}