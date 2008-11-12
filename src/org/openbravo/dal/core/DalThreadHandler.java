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

/**
 * Ensures that the session/transaction are closed/committed/rolledback at the
 * end of the thread. It also ensures that the OBContext is removed from the
 * thread.
 * 
 * Note that cleaning up the thread is particularly important in webcontainer
 * environments because webcontainers (tomcat) re-use thread instances for new
 * requests (using a threadpool).
 * 
 * @author mtaal
 */

public abstract class DalThreadHandler extends ThreadHandler {

    @Override
    public void doBefore() {
    }

    @Override
    public void doFinal(boolean errorOccured) {
	try {
	    if (SessionHandler.isSessionHandlerPresent()) {
		// application software can force a rollback
		if (SessionHandler.getInstance().getDoRollback()) {
		    SessionHandler.getInstance().rollback();
		} else if (errorOccured) {
		    SessionHandler.getInstance().rollback();
		} else {
		    SessionHandler.getInstance().commitAndClose();
		}
	    }
	} finally {
	    SessionHandler.deleteSessionHandler();
	    if (OBContext.getOBContext() != null) {
		OBContext.getOBContext().setInAdministratorMode(false);
	    }
	    OBContext.setOBContext((OBContext) null);
	}
    }
}