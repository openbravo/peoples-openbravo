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

package org.openbravo.base.model;

import org.hibernate.cfg.Configuration;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes and provides the session factory for the model layer. It uses
 * fixed mappings for Table, Column etc..
 * 
 * @author mtaal
 */

public class ModelSessionFactoryController extends SessionFactoryController {

    @Override
    protected void mapModel(Configuration cfg) {
        cfg.addClass(Table.class);
        cfg.addClass(Package.class);
        cfg.addClass(Column.class);
        cfg.addClass(Reference.class);
        cfg.addClass(RefSearch.class);
        cfg.addClass(RefTable.class);
        cfg.addClass(RefList.class);
        cfg.addClass(Module.class);
    }
}
