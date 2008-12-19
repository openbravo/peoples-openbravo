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

package org.openbravo.service.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;

/**
 * Offers services for the Openbravo UI's. For example create a combo box
 * functionality.
 * 
 * @author Martin Taal
 */
public class UIService implements OBSingleton {
    private static final Logger log = Logger.getLogger(UIService.class);

    private static UIService instance;

    public static UIService getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(UIService.class);
        }
        return instance;
    }

    public static void setInstance(UIService instance) {
        UIService.instance = instance;
    }

    public FieldProvider[] getFieldProviders(String entityName,
            Object currentValueId) {
        final OBCriteria<BaseOBObject> fieldCriteria = OBDal.getInstance()
                .createCriteria(entityName);

        boolean found = false;
        final List<BaseOBObject> bobs = new ArrayList<BaseOBObject>();
        for (final BaseOBObject bob : fieldCriteria.list()) {
            bobs.add(bob);
            found = found
                    || (currentValueId == null || bob.getId().equals(
                            currentValueId));
        }
        return null;
    }

    private class BaseOBObjectSorter implements Comparator<BaseOBObject> {

        @Override
        public int compare(BaseOBObject o1, BaseOBObject o2) {
            // TODO Auto-generated method stub
            return 0;
        }

    }
}
