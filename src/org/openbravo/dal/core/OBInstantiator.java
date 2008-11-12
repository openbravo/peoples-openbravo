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

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.tuple.Instantiator;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.DynamicOBObject;
import org.openbravo.base.structure.Identifiable;
import org.openbravo.base.util.Check;

/**
 * Instantiates a Openbravo business object and tells it which type it is. Is
 * used by Hibernate.
 * 
 * Used to support dynamic business objects which can handle runtime model
 * changes.
 * 
 * TODO: support dynamic subclassing, this is currently not supported, see
 * hibernate DynamicMapInstantiator for ideas on how to accomplish this.
 * 
 * @author mtaal
 */
public class OBInstantiator implements Instantiator {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(OBInstantiator.class);

    private String entityName;
    private Class<?> mappedClass;

    public OBInstantiator() {
	this.entityName = null;
    }

    public OBInstantiator(PersistentClass mappingInfo) {
	this.entityName = mappingInfo.getEntityName();
	mappedClass = mappingInfo.getMappedClass();
	log.debug("Creating dynamic instantiator for " + entityName);
    }

    public Object instantiate() {
	return OBProvider.getInstance().get(entityName);
    }

    public Object instantiate(Serializable id) {
	if (mappedClass != null) {
	    final Identifiable obObject = (Identifiable) OBProvider
		    .getInstance().get(mappedClass);
	    obObject.setId(id);
	    Check.isTrue(obObject.getEntityName().equals(entityName),
		    "Entityname of instantiated object "
			    + obObject.getEntityName()
			    + " and expected entityName: " + entityName
			    + " is different.");
	    return obObject;
	} else {
	    final DynamicOBObject dob = new DynamicOBObject();
	    dob.setEntityName(entityName);
	    dob.setId((String) id);
	    return dob;
	}
    }

    public boolean isInstance(Object object) {
	if (object instanceof Identifiable) {
	    return entityName.equals(((Identifiable) object).getEntityName());
	}
	return false;
    }
}
