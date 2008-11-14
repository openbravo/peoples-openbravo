/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SL All
 * portions are Copyright (C) 2008 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 */

package org.openbravo.base.expression;

import org.apache.log4j.Logger;
import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.util.Check;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 * Evaluates expressions, the expression language supported by this class is
 * javascript rhino.
 * 
 * @author mtaal
 */

public class Evaluator implements OBSingleton {
    private static final Logger log = Logger.getLogger(Evaluator.class);

    private static Evaluator instance = new Evaluator();

    public static Evaluator getInstance() {
	if (instance == null) {
	    instance = OBProvider.getInstance().get(Evaluator.class);
	}
	return instance;
    }

    public static void setInstance(Evaluator instance) {
	Evaluator.instance = instance;
    }

    /**
     * Evaluates the passed script in the context of the passed business object.
     * This means that properties of the business object may be used directly in
     * the script. The result should always be a boolean.
     * 
     * @param contextBob
     *            the script is executed in the context of this business object
     * @param script
     *            the javascript which much evaluate to a boolean
     * @return
     */
    public Boolean evaluateBoolean(BaseOBObjectDef contextBob, String script) {
	// TODO: check if the compiled javascript can be cached
        // TODO: now this uses the jdk 1.6 Rhino implementation directly. 
        // it should be changed to ScriptEngineManager/ScriptEngine object

	log.debug("Evaluating script for " + contextBob + " script: " + script);

	try {
	    final Context cx = Context.enter();
	    final Scriptable scope = cx.initStandardObjects();
	    final OBScriptableObject obScope = new OBScriptableObject();
	    obScope.setDelegate(scope);
	    obScope.setBaseObject(contextBob);
	    final Object result = cx.evaluateString(obScope, script, "source",
		    1, null);
	    Check.isInstanceOf(result, Boolean.class);
	    return (Boolean) result;
	} finally {
	    Context.exit();
	}
    }

    // Forms the context in which the script is run
    class OBScriptableObject implements Scriptable {

	private Scriptable delegate;
	private BaseOBObjectDef baseObject;
	private Entity entity;

	public BaseOBObjectDef getBaseObject() {
	    return baseObject;
	}

	public void setBaseObject(BaseOBObjectDef baseObject) {
	    this.baseObject = baseObject;
	    entity = baseObject.getEntity();
	}

	public Entity getEntity() {
	    return entity;
	}

	public void delete(int arg0) {
	    delegate.delete(arg0);
	}

	public void delete(String arg0) {
	    delegate.delete(arg0);
	}

	public Object get(int arg0, Scriptable arg1) {
	    return delegate.get(arg0, arg1);
	}

	// first checks if the string evaluates to a property
	// of the business object, if not calls the super class.
	public Object get(String arg0, Scriptable arg1) {
	    if (entity.hasProperty(arg0)) {
		final Object o = baseObject.get(arg0);
		if (o instanceof BaseOBObjectDef) {
		    final OBScriptableObject obo = new OBScriptableObject();
		    obo.setDelegate(getDelegate());
		    obo.setBaseObject((BaseOBObjectDef) o);
		    return obo;
		}
		return o;
	    }
	    return delegate.get(arg0, arg1);
	}

	public String getClassName() {
	    return delegate.getClassName();
	}

	@SuppressWarnings("unchecked")
	public Object getDefaultValue(Class arg0) {
	    return delegate.getDefaultValue(arg0);
	}

	public Object[] getIds() {
	    return delegate.getIds();
	}

	public Scriptable getParentScope() {
	    return delegate.getParentScope();
	}

	public Scriptable getPrototype() {
	    return delegate.getPrototype();
	}

	public boolean has(int arg0, Scriptable arg1) {
	    return delegate.has(arg0, arg1);
	}

	public boolean has(String arg0, Scriptable arg1) {
	    if (entity.hasProperty(arg0)) {
		return true;
	    }
	    return delegate.has(arg0, arg1);
	}

	public boolean hasInstance(Scriptable arg0) {
	    return delegate.hasInstance(arg0);
	}

	public void put(int arg0, Scriptable arg1, Object arg2) {
	    delegate.put(arg0, arg1, arg2);
	}

	// first checks if the arg0 is a property and arg1 is an
	// instance of this class. If so then the value (arg2) is set in
	// the business object.
	public void put(String arg0, Scriptable arg1, Object arg2) {
	    if (arg1 instanceof OBScriptableObject) {
		final BaseOBObjectDef bob = ((OBScriptableObject) arg1)
			.getBaseObject();
		if (bob.getEntity().hasProperty(arg0)) {
		    bob.set(arg0, arg2);
		    return;
		}
	    }
	    delegate.put(arg0, arg1, arg2);
	}

	public void setParentScope(Scriptable arg0) {
	    delegate.setParentScope(arg0);
	}

	public void setPrototype(Scriptable arg0) {
	    delegate.setPrototype(arg0);
	}

	public Scriptable getDelegate() {
	    return delegate;
	}

	public void setDelegate(Scriptable delegate) {
	    this.delegate = delegate;
	}
    }
}
