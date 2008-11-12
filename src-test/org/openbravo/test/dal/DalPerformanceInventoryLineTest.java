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

package org.openbravo.test.dal;

import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.test.base.BaseTest;

/**
 * Does some simple performance tests.
 * 
 * @author mtaal
 */

public class DalPerformanceInventoryLineTest extends BaseTest {

    private static final int NO_HEADER = 5000;
    private static final int NO_LINE = 10;
    private static String NAME_PREFIX = "" + System.currentTimeMillis();

    public void testACreateInventoryLine() {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBCriteria<InventoryCount> icObc = OBDal.getInstance()
		.createCriteria(InventoryCount.class);
	icObc.setFirstResult(1);
	icObc.setMaxResults(1);
	icObc.addOrderBy("id", false);
	final InventoryCount baseIc = (InventoryCount) DalUtil.copy(icObc
		.list().get(0), false);

	final OBCriteria<InventoryCountLine> iclObc = OBDal.getInstance()
		.createCriteria(InventoryCountLine.class);
	iclObc.setFirstResult(1);
	iclObc.setMaxResults(1);
	final InventoryCountLine baseLine = (InventoryCountLine) DalUtil.copy(
		iclObc.list().get(0), false);
	final long time = System.currentTimeMillis();
	OBDal.getInstance().commitAndClose();
	for (int i = 0; i < NO_HEADER; i++) {
	    final InventoryCount ic = (InventoryCount) DalUtil.copy(baseIc,
		    false);
	    ic.setPosted("N");
	    ic.setProcessed(false);
	    ic.setName(NAME_PREFIX + "_" + i);
	    for (int j = 0; j < NO_LINE; j++) {
		final InventoryCountLine icl = (InventoryCountLine) DalUtil
			.copy(baseLine, false);
		icl.setInventoryCount(ic);
		icl.setLine(j);
		ic.getInventoryCountLineList().add(icl);

	    }
	    OBDal.getInstance().save(ic);
	}
	OBDal.getInstance().commitAndClose();
	System.err.println("Created " + NO_HEADER + " inventorycounts and "
		+ (NO_HEADER * NO_LINE) + " inventory lines" + " in "
		+ (System.currentTimeMillis() - time) + " milliseconds");
	setErrorOccured(false);
    }

    public void testBReadAndAddLine() {
	setErrorOccured(true);
	setUserContext("1000001");

	final OBCriteria<InventoryCountLine> iclObc = OBDal.getInstance()
		.createCriteria(InventoryCountLine.class);
	iclObc.setFirstResult(1);
	iclObc.setMaxResults(1);
	final InventoryCountLine baseLine = (InventoryCountLine) DalUtil.copy(
		iclObc.list().get(0), false);
	final long time = System.currentTimeMillis();
	OBDal.getInstance().commitAndClose();

	final OBCriteria<InventoryCount> icObc = OBDal.getInstance()
		.createCriteria(InventoryCount.class);
	icObc.add(Expression.like("name", NAME_PREFIX + "%"));
	int cnt = 0;
	int cntLine = 0;
	for (final InventoryCount ic : icObc.list()) {
	    cnt++;
	    final InventoryCountLine icl = (InventoryCountLine) DalUtil.copy(
		    baseLine, false);
	    icl.setInventoryCount(ic);
	    icl.setLine(ic.getInventoryCountLineList().size() + 1);
	    ic.getInventoryCountLineList().add(icl);

	    cntLine = ic.getInventoryCountLineList().size();

	    icl.setDescription("desc " + ic.getName());
	    final InventoryCountLine icl2 = ic.getInventoryCountLineList().get(
		    0);
	    icl2.setQuantityOrderBook((icl2.getQuantityOrderBook() == null ? 0f
		    : icl2.getQuantityOrderBook()) + 1f);
	    OBDal.getInstance().save(ic);
	}
	OBDal.getInstance().commitAndClose();
	System.err
		.println("Read "
			+ cnt
			+ " inventorycounts with each "
			+ cntLine
			+ " inventory lines and added one new line and updated one line in "
			+ (System.currentTimeMillis() - time) + " milliseconds");
	setErrorOccured(false);
    }
}