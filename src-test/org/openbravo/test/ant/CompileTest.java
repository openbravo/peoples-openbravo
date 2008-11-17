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

package org.openbravo.test.ant;

import org.apache.log4j.PropertyConfigurator;
import org.openbravo.base.exception.OBException;
import org.openbravo.wad.Wad;

/**
 * Tests an ant task.
 * 
 * @author mtaal
 */

public class CompileTest extends BaseAntTest {

    public void testCompileComplete() {
        PropertyConfigurator.configure(this.getClass().getResource(
                "/log4j.properties"));

        final String[] args = new String[5];
        args[0] = "config"; // ${base.config}'
        args[1] = "%";// '${tab}'
        args[2] = "srcAD/org/openbravo/erpWindows"; // '${build.AD}/org/openbravo/erpWindows'
        args[3] = "srcAD/org/openbravo/erpCommon"; //
        args[4] = "build/javasqlc/src"; // '${build.sqlc}/src'
        // args[5] = '${webTab}'
        // '${build.AD}/org/openbravo/erpCommon/ad_actionButton'
        // '${base.design}' '${base.translate.structure}' '${client.web.xml}'
        // '..' '${attach.path}' '${web.url}' '${base.src}' '${complete}'
        // '${module}'
        try {
            Wad.main(args);
        } catch (final Exception e) {
            throw new OBException(e);
        }

        // doTest("compile");
    }
}