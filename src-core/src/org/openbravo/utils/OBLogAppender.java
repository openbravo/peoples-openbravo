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

package org.openbravo.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This appender can be used to send log4j to a programmatically set
 * OutputStream. The OutputStream is stored in a ThreadLocal so only log events
 * of the thread itself are send to the OutputStream set by that thread.
 * 
 * @author mtaal
 */
public class OBLogAppender extends AppenderSkeleton {

    private static final ThreadLocal<OutputStream> outputStreamHolder = new ThreadLocal<OutputStream>();

    /**
     * Sets the passed OutputStream in a ThreadLocal, this OutputStream is then
     * used by the appender to pass in log4j statements.
     * 
     * @param os
     *            the OutputStream to which log4j events will be send.
     */
    public static void setOutputStream(OutputStream os) {
        outputStreamHolder.set(os);
    }

    /**
     * @return the OutputStream stored in the ThreadLocal, note can be null if
     *         no OutputStream has been set.
     */
    public static OutputStream getOutputStream() {
        return outputStreamHolder.get();
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            if (outputStreamHolder.get() != null) {
                if (getLayout() != null) {
                    outputStreamHolder.get().write(
                            getLayout().format(event).getBytes());
                } else {
                    outputStreamHolder.get().write(
                            (event.getMessage().toString() + "\n").getBytes());
                }
                outputStreamHolder.get().flush();
            }
        } catch (final IOException e) {
            // TODO: replace with OBException to log this exception
            // can be done when OBException has been moved to the core
            // lib
            throw new RuntimeException(e);
        }
    }

    /**
     * Does not do anything in this implementation.
     */
    public void close() {
    }

    /**
     * @return always returns false
     */
    public boolean requiresLayout() {
        return false;
    }
}
