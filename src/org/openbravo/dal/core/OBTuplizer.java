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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Subclass;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.util.ReflectHelper;

/**
 * The tuplizer for OBObjects objects. Sets the accessors and instantiator.
 * 
 * @author mtaal
 */
@SuppressWarnings("unchecked")
public class OBTuplizer extends PojoEntityTuplizer {
    private static final Logger log = Logger.getLogger(OBTuplizer.class);

    private final PersistentClass persistentClass;

    public OBTuplizer(EntityMetamodel entityMetamodel,
	    PersistentClass mappedEntity) {
	super(entityMetamodel, mappedEntity);
	log.debug("Created tuplizer for "
		+ (mappedEntity.getMappedClass() != null ? mappedEntity
			.getMappedClass().getName() : mappedEntity
			.getEntityName()));
	persistentClass = mappedEntity;
    }

    // this is done in the generated mapping
    // @Override
    // protected Getter buildPropertyGetter(Property mappedProperty,
    // PersistentClass mappedEntity) {
    // return new OBDynamicPropertyHandler.Getter(mappedProperty.getName());
    // }
    //  
    // @Override
    // protected Setter buildPropertySetter(Property mappedProperty,
    // PersistentClass mappedEntity) {
    // return new OBDynamicPropertyHandler.Setter(mappedProperty.getName());
    // }

    @Override
    protected Instantiator buildInstantiator(PersistentClass mappingInfo) {
	return new OBInstantiator(mappingInfo);
    }

    @Override
    protected ProxyFactory buildProxyFactory(
	    PersistentClass thePersistentClass, Getter idGetter, Setter idSetter) {
	// determine the id getter and setter methods from the proxy interface
	// (if
	// any)
	// determine all interfaces needed by the resulting proxy
	HashSet proxyInterfaces = new HashSet();
	proxyInterfaces.add(HibernateProxy.class);

	Class mappedClass = thePersistentClass.getMappedClass();
	Class proxyInterface = thePersistentClass.getProxyInterface();

	if (proxyInterface != null && !mappedClass.equals(proxyInterface)) {
	    if (!proxyInterface.isInterface()) {
		throw new MappingException(
			"proxy must be either an interface, or the class itself: "
				+ getEntityName());
	    }
	    proxyInterfaces.add(proxyInterface);
	}

	if (mappedClass.isInterface()) {
	    proxyInterfaces.add(mappedClass);
	}

	Iterator iter = thePersistentClass.getSubclassIterator();
	while (iter.hasNext()) {
	    Subclass subclass = (Subclass) iter.next();
	    Class subclassProxy = subclass.getProxyInterface();
	    Class subclassClass = subclass.getMappedClass();
	    if (subclassProxy != null && !subclassClass.equals(subclassProxy)) {
		if (proxyInterface == null || !proxyInterface.isInterface()) {
		    throw new MappingException(
			    "proxy must be either an interface, or the class itself: "
				    + subclass.getEntityName());
		}
		proxyInterfaces.add(subclassProxy);
	    }
	}

	Method idGetterMethod = idGetter == null ? null : idGetter.getMethod();
	Method idSetterMethod = idSetter == null ? null : idSetter.getMethod();

	Method proxyGetIdentifierMethod = idGetterMethod == null
		|| proxyInterface == null ? null : ReflectHelper.getMethod(
		proxyInterface, idGetterMethod);
	Method proxySetIdentifierMethod = idSetterMethod == null
		|| proxyInterface == null ? null : ReflectHelper.getMethod(
		proxyInterface, idSetterMethod);

	ProxyFactory pf = buildProxyFactoryInternal(thePersistentClass,
		idGetter, idSetter);
	try {
	    pf
		    .postInstantiate(
			    getEntityName(),
			    mappedClass,
			    proxyInterfaces,
			    proxyGetIdentifierMethod,
			    proxySetIdentifierMethod,
			    thePersistentClass.hasEmbeddedIdentifier() ? (AbstractComponentType) thePersistentClass
				    .getIdentifier().getType()
				    : null);
	} catch (HibernateException he) {
	    log.warn("could not create proxy factory for:" + getEntityName(),
		    he);
	    pf = null;
	}
	return pf;
    }

    @Override
    public Class getMappedClass() {
	return persistentClass.getMappedClass();
    }

    @Override
    public Class getConcreteProxyClass() {
	return persistentClass.getMappedClass();
    }
}