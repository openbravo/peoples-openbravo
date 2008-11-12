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

package org.openbravo.erpCommon.modules;

import java.util.ArrayList;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;

/**
 * This class implements different utilities related to modules
 * 
 *
 */
public class ModuleUtiltiy {

  /**
   * It receives an ArrayList<String> with modules IDs and returns the same
   * list ordered taking into account the module dependency tree.
   * 
   * @param conn DB connection
   * @param modules List of module to order
   * @return modules list ordered
   */
  public static ArrayList<String> orderByDependency(ConnectionProvider conn, ArrayList<String> modules) {
    try {
      ArrayList<String> rt = new ArrayList<String>(); //return object
      ArrayList<String> checked = new ArrayList<String>(); //checked list to avoid endless loops
      
      ArrayList<String> roots = selectRoots(conn); //selects all the root nodes (no dependencies) from the complete tree
     
      for (int i=0; i<roots.size(); i++){
        if (rt.indexOf(roots.get(i))==-1 && modules.indexOf(roots.get(i))!=-1) 
          rt.add(roots.get(i)); //Adds all the root trees in the return list
        
        checked.add(roots.get(i));
        checkTree(conn, roots.get(i), rt, modules, checked);
      }
      
      return rt;
    } catch (Exception e) {
      e.printStackTrace();
      return modules;
    }
  }
  
  private static ArrayList<String> selectRoots(ConnectionProvider conn) throws  ServletException {
    ArrayList<String> rt = new ArrayList<String>();
    
    ModuleUtilityData module[] = ModuleUtilityData.selectRootNodes(conn);
    if (module!=null){
      for (int i=0; i<module.length; i++) {
        rt.add(module[i].adModuleId);
      }
    }
    
    return rt;
  }

  private static void checkTree(ConnectionProvider conn, String root, ArrayList<String> rt, ArrayList<String> list, ArrayList<String> checked) throws  ServletException{
    ModuleUtilityData parents[] = ModuleUtilityData.selectParents(conn, root);
    if (parents==null || parents.length==0) return;
    for (int i=0; i<parents.length; i++) {
      if (checked.indexOf(parents[i].adModuleId) == -1) {
        checked.add(parents[i].adModuleId);
        if (list.indexOf(parents[i].adModuleId) != -1) {
          rt.add(parents[i].adModuleId);
        }
        checkTree(conn, parents[i].adModuleId, rt, list, checked);
      }
    }
  }
}
