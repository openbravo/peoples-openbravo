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

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes and provides the session factory for the runtime dal layer. This
 * SessionFactoryController is initialized after the model has been read
 * in-memory. This sfc generates a hibernate mapping.
 * 
 * @author mtaal
 */

public class DalSessionFactoryController extends SessionFactoryController {
    private static final Logger log = Logger
	    .getLogger(DalSessionFactoryController.class);

    private String mapping;

    @Override
    protected void mapModel(Configuration configuration) {
	mapping = DalMappingGenerator.getInstance().generateMapping();
	// System.err.println(mapping);
	log.debug("Generated mapping: ");
	log.debug(mapping);
	configuration.addXML(mapping);
    }

    @Override
    protected void setInterceptor(Configuration configuration) {
	configuration.setInterceptor(new OBInterceptor());
    }

    public String getMapping() {
	return mapping;
    }
}
