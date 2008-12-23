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
package org.openbravo.scheduling;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.openbravo.base.ConfigParameters;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_process.JasperProcess;
import org.openbravo.erpCommon.ad_process.PinstanceProcedure;
import org.openbravo.erpCommon.ad_process.ProcedureProcess;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * @author awolski
 * 
 */
public class ProcessBundle {

    public static final String KEY = "org.openbravo.scheduling.ProcessBundle.KEY";

    public static final String PINSTANCE = "process.param.pinstance";

    public static final String CONNECTION = "process.param.connection";

    public static final String CONFIG_PARAMS = "process.param.configParams";

    private String processId;

    private String impl;

    private Map<String, Object> params;

    private Class<? extends Process> processClass;

    private ProcessContext context;

    private ConnectionProvider connection;

    private ConfigParameters config;

    private ProcessLogger logger;

    private Object result;

    private Channel channel;

    public ProcessBundle(String processId, VariablesSecureApp vars) {
        this(processId, vars, Channel.DIRECT);
    }

    public ProcessBundle(String processId, VariablesSecureApp vars,
            Channel channel) {
        this.processId = processId;
        this.context = new ProcessContext(vars);
        this.channel = channel;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getPinstanceId() {
        return (String) getParams().get(PINSTANCE);
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(String impl) {
        this.impl = impl;
    }

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        return params;
    }

    /**
     * @return
     */
    public String getParamsDefalated() {
        final XStream xstream = new XStream(new JettisonMappedXmlDriver());
        return xstream.toXML(getParams());
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Class<? extends Process> getProcessClass() {
        return processClass;
    }

    public void setProcessClass(Class<? extends Process> processClass) {
        this.processClass = processClass;
    }

    public ProcessContext getContext() {
        return context;
    }

    public void setContext(ProcessContext context) {
        this.context = context;
    }

    public ConnectionProvider getConnection() {
        return connection;
    }

    public void setConnection(ConnectionProvider connection) {
        this.connection = connection;
    }

    public ConfigParameters getConfig() {
        return config;
    }

    public void setConfig(ConfigParameters config) {
        this.config = config;
    }

    public ProcessLogger getLogger() {
        return logger;
    }

    public void setLog(ProcessLogger logger) {
        this.logger = logger;
    }

    public String getLog() {
        return logger.getLog();
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * @param conn
     * @return
     * @throws Exception
     */
    public ProcessBundle init(ConnectionProvider conn) throws ServletException {
        if (processId == null) {
            throw new ServletException("Process Id cannot be null");
        }
        final ProcessData data = ProcessData.select(conn, processId);
        if (data.isbackground != null && data.isbackground.equals("Y")) {
            try {
                setProcessClass(Class.forName(data.classname).asSubclass(
                        Process.class));
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
                throw new ServletException(e.getMessage(), e);
            }

        } else if (data.isjasper != null && data.isjasper.equals("Y")) {
            setImpl(data.procedurename);
            setProcessClass(JasperProcess.class);

        } else if (data.procedurename != null && !data.procedurename.equals("")) {
            setImpl(data.procedurename);
            setProcessClass(ProcedureProcess.class);
        }
        // TODO Load parameters - not required as we're still using pinstanceId
        setParams(new HashMap<String, Object>());
        setConnection(conn);
        setLog(new ProcessLogger(conn));

        return this;
    }

    /**
     * Utility method to create a new Process bundle from the details of in
     * AD_PINSTANCE at the specified pinstanceId.
     * 
     * @param pinstanceId
     *            Pinstance Id.
     * @param vars
     *            VariablesSecureApp to be converted into the ProcessContext
     * @param conn
     *            ConnectionProvider
     * @return
     * @throws ServletException
     */
    public static final ProcessBundle pinstance(String pinstanceId,
            VariablesSecureApp vars, ConnectionProvider conn)
            throws ServletException {
        final String processId = PinstanceData.select(conn, pinstanceId).adProcessId;

        final ProcessBundle bundle = new ProcessBundle(processId, vars)
                .init(conn);
        bundle.setProcessClass(PinstanceProcedure.class);
        bundle.getParams().put(PINSTANCE, pinstanceId);

        return bundle;
    }

    /**
     * @param requestId
     * @param vars
     * @param conn
     * @return
     * @throws ServletException
     */
    public static final ProcessBundle request(String requestId,
            VariablesSecureApp vars, ConnectionProvider conn)
            throws ServletException {
        final ProcessRequestData data = ProcessRequestData.select(conn,
                requestId);

        final String processId = data.processId;
        final ProcessBundle bundle = new ProcessBundle(processId, vars,
                Channel.SCHEDULED).init(conn);
        bundle.setContext(new ProcessContext(vars));

        final String paramString = data.params;
        if (paramString == null || paramString.trim().equals("")) {
            bundle.setParams(new HashMap<String, Object>());
        } else {
            final XStream xstream = new XStream(new JettisonMappedXmlDriver());
            bundle.setParams((HashMap<String, Object>) xstream
                    .fromXML(paramString));
        }

        return bundle;
    }

    /**
     * @author awolski
     * 
     */
    public enum Channel {
        DIRECT {
            @Override
            public String toString() {
                return "Direct";
            }
        },
        BACKGROUND {
            @Override
            public String toString() {
                return "Background";
            }
        },
        SCHEDULED {
            @Override
            public String toString() {
                return "Process Scheduler";
            }
        },
        WEBSERVICE {
            @Override
            public String toString() {
                return "Webservice";
            }
        },
    }

}
