package org.openbravo.scheduling.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.listeners.JobListenerSupport;

public class JobInitializationListener extends JobListenerSupport {

	private static final String LISTENER_NAME = "OBScheduler.JobInitializationListener";
	
	private static Logger logger = LogManager.getLogger(JobInitializationListener.class);

	@Override
	public String getName() {
		return LISTENER_NAME;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext ctx) {
		super.jobToBeExecuted(ctx);
		JobDataMap dataMap = ctx.getJobDetail().getJobDataMap();
		if (dataMap == null) {
			logger.warn("Executing job " + ctx.getJobDetail().getKey().getName() + " without a ConnectionProvider because the DataMap was null.");
			return;
		}
		ProcessBundle bundle = (ProcessBundle)dataMap.get(ProcessBundle.KEY);
		if (bundle == null) {
			logger.warn("Executing job " + ctx.getJobDetail().getKey().getName() + " without a ConnectionProvider because the ProcessBundle was null.");
			return;
		}
		if (bundle.getConnection() == null) {
			// Set the ConnectionProvider if it was lost during serialization/deserialization
			bundle.setConnection((ConnectionProvider) ctx.get(ConnectionProviderContextListener.POOL_ATTRIBUTE));
		}
		bundle.setLog(new ProcessLogger(bundle.getConnection()));
	}
	
}
