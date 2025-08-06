package de.fms.scm.service;

import de.fms.scm.config.*;
import de.fms.scm.entities.scmtd.TransactionData;
import de.fms.scm.persistence.Dao;
import de.fms.scm.service.transactiondata.TransactionServiceImpl.REASON;
import de.fms.scm.utils.ScmPersistenceException;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ContextConfiguration(classes = {
		ScmCoreJPAIntTestConfig.class, 
		ScmCoreApplicationIntTestConfig.class,
        ScmCoreActivitiConfig.class, 
        ScmCoreUtilsConfig.class, 
        ScmCachingConfig.class, 
        ScmCoreMailConfig.class, 
        ScmCoreJcrConfig.class, 
        ScmMfaConfig.class, 
        KpiServiceConfig.class,
        ZendeskServiceTestConfig.class})
@ActiveProfiles(profiles = {"testCore"})
@EnableTransactionManagement
@RunWith(SpringRunner.class)
public abstract class AbstractServiceIT {
	
    @Autowired
    protected Dao hibernateDao;
    
	@Autowired
	protected ScmService scmService;
	
	protected Runnable newRunnable(final TransactionData transData, final EnumSet<REASON> resons) {
		return () -> {
			try {
				scmService.newTransaction(transData, resons);
			} catch (ScmPersistenceException e) {
				e.printStackTrace();
			}
		};
	}
	
	/**Execute scm-service with a list of runnable.*/
	protected void executeScmService(final List<Runnable> runnables) {
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		runnables.forEach(r -> executorService.execute(r));
		
		executorService.shutdown();
		try {
			executorService.awaitTermination(300, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}


