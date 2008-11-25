/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
        } catch (final ServletException se) {
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
        } catch (final Throwable t) {
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