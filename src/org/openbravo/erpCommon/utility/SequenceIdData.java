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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
//Sqlc generated VO.O11-2
// modified manually to return a String;

package org.openbravo.erpCommon.utility;

import java.sql.*;
import java.util.*;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;

public class SequenceIdData implements FieldProvider {
  public String dummy;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("dummy"))
      return dummy;
    else {
      return null;
    }
  }

  /**
   * Returns a new UUID
   * @return a new random UUID
   */
  public static String getUUID(){
	return UUID.randomUUID().toString().replace("-", "").toUpperCase();
  }
  
  /**Get the sequence for the specified table
   * this shouldn't be used anymore, use instead getUUID() 
   * @deprecated
  */
  public static String getSequence(ConnectionProvider conn, String table, String client) {
    return getUUID();
  }
  
  /**Get the sequence for the specified table 
   */
   public static String getSequenceConnection(Connection conn, ConnectionProvider con, String table, String client)
     throws ServletException {
     String object;
     CSResponse response = SequenceData.getSequenceConnection(conn, con, table, client);
     object = response.razon;

     return(object);
   }
}
