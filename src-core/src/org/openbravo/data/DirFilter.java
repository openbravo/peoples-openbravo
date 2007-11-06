/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.data;

import java.io.FilenameFilter;
import java.io.File;

public class DirFilter implements FilenameFilter {
  String afn;
  DirFilter(String afn) {
    this.afn = afn;
  }

  public boolean accept(File dir, String name) {
    boolean boolReturn;
    // the name is obtained only to compare it with the filename and not with all the names in all the path
    String f = new File(name).getName();
    // return true if the filter agrees or if it is a directory
    boolReturn = f.indexOf(afn, f.length() - afn.length()) != -1 || new File(dir,name).isDirectory();
    return boolReturn;
  }
}
