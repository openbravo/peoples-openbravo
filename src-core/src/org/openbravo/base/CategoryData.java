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
package org.openbravo.base;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

public class CategoryData implements FieldProvider {

  static Logger log4j = Logger.getLogger(CategoryData.class);
  public String category;
  public String priority;

  public String getField(String fieldName) {
    if (fieldName.equals("category"))
      return category;
    else if (fieldName.equals("priority"))
      return priority;
    else {
      if (log4j.isDebugEnabled())
        log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static CategoryData[] getCategories() {
    Vector<CategoryData> vector = new Vector<CategoryData>(0);

    for (Enumeration<?> e = LogManager.getCurrentLoggers(); e.hasMoreElements();) {
      Logger categoryItem = (Logger) e.nextElement();
      CategoryData categoryData = new CategoryData();
      categoryData.category = categoryItem.getName();
      if (categoryItem.getLevel() != null) {
        categoryData.priority = categoryItem.getLevel().toString();
      }
      if (vector.isEmpty())
        vector.addElement(categoryData);
      else {
        int index = 0;
        while (index < vector.size()) {
          CategoryData cd = vector.get(index);
          if (categoryData.category.compareTo(cd.category) < 0) {
            vector.add(index, categoryData);
            break;
          }
          index++;
        }
        if (index == vector.size())
          vector.addElement(categoryData);
      }

    }

    CategoryData categoryData[] = new CategoryData[vector.size()];
    vector.copyInto(categoryData);
    return (categoryData);
  }
}
