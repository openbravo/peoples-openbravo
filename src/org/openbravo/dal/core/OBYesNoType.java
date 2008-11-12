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

package org.openbravo.dal.core;

import org.hibernate.type.YesNoType;
import org.hibernate.util.EqualsHelper;

/**
 * Extends the hibernate yesno type, handles null values as false. As certain
 * methods can not be extended the solution is to catch the isDirty check by
 * reimplementing the isEqual method. This type will say that null is equal to
 * false.
 * 
 * @author mtaal
 */
public class OBYesNoType extends YesNoType {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isEqual(Object x, Object y) {
	if (x == y) {
	    return true;
	}
	if (x == null && y != null && y instanceof Boolean) {
	    return ((Boolean) y).booleanValue() == false;
	} else if (y == null && x != null && x instanceof Boolean) {
	    return ((Boolean) x).booleanValue() == false;
	}

	return EqualsHelper.equals(x, y);
    }

}
