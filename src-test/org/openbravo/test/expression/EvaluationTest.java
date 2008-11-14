/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
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