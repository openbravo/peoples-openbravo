/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Iterator;

import org.hibernate.ScrollableResults;
import org.openbravo.dal.service.OBDal;

public abstract class Scroll {
  public abstract boolean next();
  public abstract Object get();
  public abstract void close();

  public static Scroll create(final ScrollableResults sr) {
    return new Scroll() {
      int index = 0;
      @Override
      public boolean next() {
        index++;
        if (index % 100 == 0) {
          OBDal.getInstance().getSession().clear();
        }
        return sr.next();
      }

      @Override
      public Object get() {
        Object[] row = sr.get();
        return row.length == 1 ? row[0] : row;
      }

      @Override
      public void close() {
        sr.close();
        OBDal.getInstance().getSession().clear();
      }
    };
  }

  public static Scroll create(final Iterator<?> i) {
    return new Scroll() {
      private Object current = null;
      @Override
      public boolean next() {
        if (i.hasNext()) {
          current = i;
          return true;
        } else {
          return false;
        }
      }

      @Override
      public Object get() {
        return current;
      }

      @Override
      public void close() {
      }
    };
  }

}
