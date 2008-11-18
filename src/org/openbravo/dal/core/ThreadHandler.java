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

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;

/**
 * Can be used to run a thread in a certain context: for example setting and
 * cleaning up certain threadlocals.
 * 
 * @author martintaal
 */

public abstract class ThreadHandler {
    private static final Logger log = Logger.getLogger(ThreadHandler.class);

    public void run() {
        boolean err = true;
        try {
            log.debug("Thread started --> doBefore");
            doBefore();
            log.debug("Thread --> doAction");
            doAction();
            log.debug("Thread --> Action done");
            err = false;
            // TODO add exception logging/tracing/emailing
            // } catch (Throwable t) {
            // ExceptionHandler.reportThrowable(t, (HttpServletRequest)
            // request);
            // throw new ServletException(t);
        } catch (ServletException se) {
            if (se.getRootCause() != null) {
                se.getRootCause().printStackTrace(System.err);
                log.error(se);
                throw new OBException("Exception thrown "
                        + se.getRootCause().getMessage(), se.getRootCause());
            } else {
                se.printStackTrace(System.err);
                log.error(se);
                throw new OBException("Exception thrown " + se.getMessage(), se);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            log.error(t);
            throw new OBException("Exception thrown " + t.getMessage(), t);
        } finally {
            doFinal(err);
        }
    }

    protected abstract void doBefore() throws Exception;

    protected abstract void doFinal(boolean errorOccured);

    protected abstract void doAction() throws Exception;
}