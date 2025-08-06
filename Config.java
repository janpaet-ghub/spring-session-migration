package de.fms.scm.config;

import de.fms.scm.web.session.CustomMapSessionRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableSpringHttpSession
public class Config extends AbstractHttpSessionApplicationInitializer {

	private static final Log LOG = LogFactory.getLog(Config.class);
			
    @Bean
    public MapSessionRepository sessionRepository() {
    	LOG.info("Initializing MapSessionRepository...");
        return new CustomMapSessionRepository(new ConcurrentHashMap<>());
    }
}
