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

package org.openbravo.base.model;

import org.hibernate.cfg.Configuration;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes and provides the session factory for the model layer. It uses
 * fixed mappings for Table, Column etc..
 * 
 * @author mtaal
 */

public class ModelSessionFactoryController extends SessionFactoryController {

    @Override
    protected void mapModel(Configuration cfg) {
	cfg.addClass(Table.class);
	cfg.addClass(Package.class);
	cfg.addClass(Column.class);
	cfg.addClass(Reference.class);
	cfg.addClass(RefSearch.class);
	cfg.addClass(RefTable.class);
	cfg.addClass(RefList.class);
	cfg.addClass(Module.class);
    }
}
