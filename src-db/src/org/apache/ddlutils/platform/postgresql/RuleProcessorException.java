/*
 * RuleProcessorException.java
 *
 * Created on 6 de noviembre de 2007, 16:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.ddlutils.platform.postgresql;

/**
 *
 * @author adrian
 */
public class RuleProcessorException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>RuleProcessorException</code> without detail message.
     */
    public RuleProcessorException() {
    }
    
    
    /**
     * Constructs an instance of <code>RuleProcessorException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RuleProcessorException(String msg) {
        super(msg);
    }
}
