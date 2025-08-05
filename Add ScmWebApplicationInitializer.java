package de.fms.scm.config;

import de.fms.scm.listener.ScmServletContextListener;
import de.fms.scm.listener.ScmSessionListener;
import de.fms.scm.persistence.HibernateFilter;
import de.fms.scm.utils.LogUtil;
import de.fms.scm.web.servlets.CustomDispatcherServlet;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionTrackingMode;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Properties;

public class ScmWebApplicationInitializer implements WebApplicationInitializer {
    private static final LogUtil LOG = new LogUtil(ScmWebApplicationInitializer.class);
	private static final String SCM_DISPATCHER_SERVLET_NAME = "scm";
	
	private static final String SCM_CONFIG_FILE = "configuration.properties";
	private static final String SECURE_COOKIE = "web.secureCookie";
	
	private final boolean getSecureCookieConfig(final String configFile, final String propertyKey) {
        final Properties prop = new Properties();
        final InputStream is = this.getClass().getResourceAsStream("/" + configFile);

        try {
            prop.load(is);
            final String propertyValue = prop.getProperty(propertyKey);
            LOG.info("flag value of secure-cookie configuration: " + propertyValue);
            return Boolean.valueOf(propertyValue);
        } catch (FileNotFoundException e) {
            LOG.error("Unable to get value web.secureCookie from configuration-properties file ->  return true", e.getMessage());
            return true;
        } catch (IOException e) {
            LOG.error("Unable to get value web.secureCookie from configuration-properties file ->  return true", e.getMessage());
            return true;
        }
	}
	
	@Override
    public void onStartup(final ServletContext servletContext) {
		LOG.info("Tomcat onStartup start");
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.setServletContext(servletContext);
        
        servletContext.addListener(new ContextLoaderListener(applicationContext));
        servletContext.addListener(new ScmServletContextListener());
        servletContext.addListener(new HttpSessionEventPublisher());
        servletContext.addListener(new ScmSessionListener());
        servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        servletContext.getSessionCookieConfig().setSecure(getSecureCookieConfig(SCM_CONFIG_FILE, SECURE_COOKIE));
        servletContext.getSessionCookieConfig().setHttpOnly(false); 
        
        setCharacterEncodingFilter(servletContext);
        setGeneralFilter(servletContext);
        setCsrfFilter(servletContext);
        setHibernateFilter(servletContext);
        setRequestContextFilter(servletContext);
        setSecurityContextPersistenceFilter(servletContext);
        setSpringSessionRepositoryFilter(servletContext);		//SCM-2017
        setSecurityContextPersistenceFilter(servletContext);	//SCM-2017

		DispatcherServlet servlet = dispatcherServlet(applicationContext);
		final ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet(SCM_DISPATCHER_SERVLET_NAME, servlet);
        
        if (dispatcherServlet  == null) {
        	System.out.println("ServletContext already contains a complete ServletRegistration for servlet 'scm'");
        } else {
        	dispatcherServlet.setLoadOnStartup(1);     
        	dispatcherServlet.addMapping(ServletMappings.nameArray());
        	dispatcherServlet.setInitParameter("dispatchOptionsRequest", "true");
        	dispatcherServlet.setInitParameter("log4jConfigLocation", "/WEB-INF/log4j.properties");
        }
		LOG.info("Tomcat onStartup end");
    }

	private DispatcherServlet dispatcherServlet(AnnotationConfigWebApplicationContext applicationContext) {
		DispatcherServlet dispatcherServlet = new CustomDispatcherServlet(applicationContext);
		dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
		return dispatcherServlet;
	}
	
	private void setGeneralFilter(final ServletContext servletContext) {
		servletContext
			.addFilter("generalFilter", new DelegatingFilterProxy("generalFilter"))
			.addMappingForUrlPatterns(null, true, ServletMappings.nameArray());
	}
	
	private void setCharacterEncodingFilter(final ServletContext servletContext) {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setForceEncoding(true);
		characterEncodingFilter.setEncoding("UTF-8");
		final FilterRegistration.Dynamic filter = servletContext.addFilter("characterEncodingFilter", characterEncodingFilter);
		filter.addMappingForUrlPatterns(null, true, "*.html",
				"/api/navitems.json", "/api/userinfo.json", "/api/mobilelogin.json", "/api/language.json", "/api/barcodeformats.json",
				"/api/findJwtPublicKey", "/api/obtainKpiJwt", "/api/kpiStandardReport");
	}

	private void setCsrfFilter(final ServletContext servletContext) {
		final FilterRegistration.Dynamic filter = servletContext.addFilter("csrfFilter", new DelegatingFilterProxy());
		filter.setAsyncSupported(true);
		filter.addMappingForUrlPatterns(null, true, "/*");
	}
	
	private void setHibernateFilter(final ServletContext servletContext) {
		final FilterRegistration.Dynamic filter = servletContext.addFilter("hibernateFilter", new HibernateFilter());
		filter.addMappingForUrlPatterns(null, true, ServletMappings.nameArray());
	}
	
	private void setRequestContextFilter(final ServletContext servletContext) {
		final FilterRegistration.Dynamic filter = servletContext.addFilter("requestContextFilter", new RequestContextFilter());
		filter.addMappingForUrlPatterns(null, true, "/*");
	}
	
	private void setSecurityContextPersistenceFilter(final ServletContext servletContext) {
		final FilterRegistration.Dynamic filter = servletContext.addFilter("securityContextPersistence", new SecurityContextPersistenceFilter());
		filter.addMappingForUrlPatterns(null, true, "/*");
	}
	
	//SCM-2017
	private void setSpringSessionRepositoryFilter(final ServletContext servletContext) {
	    final FilterRegistration.Dynamic filter = servletContext.addFilter(
	        "springSessionRepositoryFilter", new DelegatingFilterProxy("springSessionRepositoryFilter"));
	    filter.setAsyncSupported(true);
	    filter.addMappingForUrlPatterns(null, true, "/*");
	}
}
