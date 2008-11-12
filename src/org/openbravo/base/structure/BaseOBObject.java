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
package org.openbravo.base.structure;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;

/**
 * Base business object, the root of the inheritance tree. The class model here
 * combines an inheritance structure with interface definitions. The inheritance
 * structure is used to enable some re-use of code. The interfaces are used to
 * tag a certain implementation with the functionality it provides. The outside
 * world should use the interfaces to determine if an object supports specific
 * functionality.
 * 
 * @author mtaal
 */

public abstract class BaseOBObject implements BaseOBObjectDef, Identifiable,
	DynamicEnabled, OBNotSingleton {
    private Entity model = null;

    // is used to force an insert of this object. This is usefull if the id of
    // the
    // object should be preserved when it is imported
    private boolean newOBObject = false;

    // contains all the data
    // TODO: an important one: the propertynames used in the subclasses should
    // be externalised so that always the same string instance is used.
    private Map<String, Object> data = new HashMap<String, Object>();

    // computed once therefore an object type
    private Boolean isDerivedReadable;

    // is used to set default data in a constructor of the generated class
    // without a security check
    protected void setDefaultValue(String propName, Object value) {
	getEntity().checkValidPropertyAndValue(propName, value);
	Check.isNotNull(value, "Null default values are not allowed");
	data.put(propName, value);
    }

    public Object getId() {
	return get("id");
    }

    public void setId(Object id) {
	set("id", id);
    }

    public abstract String getEntityName();

    public String getIdentifier() {
	return IdentifierProvider.getInstance().getIdentifier(this);
    }

    public Object get(String propName) {
	if (propName.equals("entityName")) {
	    return getEntityName();
	}

	// don't do anything special for id, this is required to let hibernate
	// work
	// correctly
	// if (propName.equals("id")) {
	// return data.get("id");
	// }

	final Property p = getEntity().getProperty(propName);
	checkDerivedReadable(p);
	return data.get(propName);
    }

    public void set(String propName, Object value) {
	final Property p = getEntity().getProperty(propName);
	p.checkIsValidValue(value);
	checkDerivedReadable(p);
	p.checkIsWritable();
	setValue(propName, value);
    }

    protected void checkDerivedReadable(Property p) {
	final OBContext obContext = OBContext.getOBContext();
	// obContext can be null in the OBContext initialize method
	if (obContext != null && obContext.isInitialized()
		&& !obContext.isInAdministratorMode()) {
	    if (isDerivedReadable == null) {
		isDerivedReadable = obContext.getEntityAccessChecker()
			.isDerivedReadable(getEntity());
	    }

	    if (isDerivedReadable && !p.allowDerivedRead()) {
		throw new OBSecurityException(
			"Entity "
				+ getEntity()
				+ " is not directly readable, only id and identifier properties are readable, property "
				+ p + " is neither of these.");
	    }
	}
    }

    // called by generated subclasses and hibernate,
    // is assumed to be safe and because no checking
    // is done it is faster
    public void setValue(String propName, Object value) {
	if (value == null) {
	    data.remove(propName);
	}
	data.put(propName, value);
    }

    // method call which does not any security checking
    // can be called by hibernate
    public Object getValue(String propName) {
	return data.get(propName);
    }

    public Entity getEntity() {
	if (model == null) {
	    model = ModelProvider.getInstance().getEntity(getEntityName());
	}
	return model;
    }

    public void validate() {
	getEntity().validate(this);
    }

    @Override
    public String toString() {
	final Entity e = getEntity();
	final StringBuilder sb = new StringBuilder();
	// and also display all values
	for (final Property p : e.getIdentifierProperties()) {
	    Object value = get(p.getName());
	    if (value != null) {
		if (sb.length() == 0) {
		    sb.append("(");
		} else {
		    sb.append(", ");
		}
		if (value instanceof BaseOBObject) {
		    value = ((BaseOBObject) value).getId();
		}
		sb.append(p.getName() + ": " + value);
	    }
	}
	if (sb.length() > 0) {
	    sb.append(")");
	}
	return getEntityName() + "(" + getId() + ") " + sb.toString();
    }

    public boolean isNewOBObject() {
	return getId() == null || newOBObject;
    }

    public void setNewOBObject(boolean newOBObject) {
	this.newOBObject = newOBObject;
    }
}