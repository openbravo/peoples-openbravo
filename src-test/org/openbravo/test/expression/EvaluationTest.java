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

package org.openbravo.test.expression;

import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.openbravo.base.expression.Evaluator;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.test.base.BaseTest;

/**
 * Test different parts of the dal api.
 * 
 * Note the testcases assume that they are run in the order defined in this
 * class.
 * 
 * @author mtaal
 */

public class EvaluationTest extends BaseTest {

    public void testEvaluation() {
        setErrorOccured(true);
        setUserContext("0");

        // as a test print scripting language names
        final ScriptEngineManager manager = new ScriptEngineManager();
        for (final ScriptEngineFactory sef : manager.getEngineFactories()) {
            System.err.println(sef.getEngineName());
        }

        final List<Table> tables = OBDal.getInstance().createCriteria(
                Table.class).list();
        boolean found = false;
        for (final Table t : tables) {
            final String script = Table.PROPERTY_CLIENT + "."
                    + Client.PROPERTY_ID + " == '0' && "
                    + Table.PROPERTY_TABLENAME + "== 'AD_Client' && "
                    + Table.PROPERTY_ACCESSLEVEL + " > 5";
            final Boolean result = Evaluator.getInstance().evaluateBoolean(t,
                    script);
            System.err.println(t.getName() + " : " + result);
            found = found || result;
        }
        assertTrue(found);
        setErrorOccured(false);
    }
}