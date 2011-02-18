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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.remote;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.core.Manager;

/**
 * A DialogHandler is responsible for setting up the server side infrastructure for an opened
 * dialog. The main thing it does is start a long running conversation and possibly reads and
 * initializes server side context.
 * 
 * @author mtaal
 */
@Name("dialogHandler")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
public class DialogHandler {

  @WebRemote
  @Begin(join = true)
  public String createConversation() {
    // Manager.instance().beginConversation();
    // Manager.instance().setLongRunningConversation(true);
    return Manager.instance().getCurrentConversationId();
  }
}
