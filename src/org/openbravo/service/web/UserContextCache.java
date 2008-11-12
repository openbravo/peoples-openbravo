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

package org.openbravo.service.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.dal.core.OBContext;

/**
 * The user context cache takes care of storing a cache of user contexts which
 * are re-used when a webservice call is done. Note that the OBContext which is
 * cached can be re-used by multiple threads at the same time.
 * 
 * @author mtaal
 */

public class UserContextCache implements OBSingleton {

    private final long EXPIRES_IN = 1000 * 60 * 30;

    private static UserContextCache instance;

    public static UserContextCache getInstance() {
	if (instance == null) {
	    instance = OBProvider.getInstance().get(UserContextCache.class);
	}
	return instance;
    }

    public static void setInstance(UserContextCache instance) {
	UserContextCache.instance = instance;
    }

    private Map<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();

    public OBContext getCreateOBContext(String userId) {
	CacheEntry ce = cache.get(userId);
	purgeCache();
	if (ce != null) {
	    ce.setLastUsed(System.currentTimeMillis());
	    return ce.getObContext();
	}
	final OBContext obContext = OBContext.createOBContext(userId);
	ce = new CacheEntry();
	ce.setLastUsed(System.currentTimeMillis());
	ce.setObContext(obContext);
	ce.setUserId(userId);
	cache.put(userId, ce);
	return obContext;
    }

    private void purgeCache() {
	final List<CacheEntry> toRemove = new ArrayList<CacheEntry>();
	for (CacheEntry ce : cache.values()) {
	    if (ce.hasExpired()) {
		toRemove.add(ce);
	    }
	}
	for (CacheEntry ce : toRemove) {
	    cache.remove(ce.getUserId());
	}
    }

    class CacheEntry {
	private OBContext obContext;
	private long lastUsed;
	private String userId;

	public boolean hasExpired() {
	    return getLastUsed() < (System.currentTimeMillis() - EXPIRES_IN);
	}

	public OBContext getObContext() {
	    return obContext;
	}

	public void setObContext(OBContext obContext) {
	    this.obContext = obContext;
	}

	public long getLastUsed() {
	    return lastUsed;
	}

	public void setLastUsed(long lastUsed) {
	    this.lastUsed = lastUsed;
	}

	public String getUserId() {
	    return userId;
	}

	public void setUserId(String userId) {
	    this.userId = userId;
	}

    }

}