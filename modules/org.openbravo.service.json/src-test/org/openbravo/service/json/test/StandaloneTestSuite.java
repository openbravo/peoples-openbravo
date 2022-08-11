/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.service.json.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** Test suite to be run in CI */
@RunWith(Suite.class)
@Suite.SuiteClasses({ //
    JSONWriterToCSVTest.class //
})
public class StandaloneTestSuite {
}
