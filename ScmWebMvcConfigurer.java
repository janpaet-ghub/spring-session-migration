package de.fms.scm.config;

import de.fms.scm.i18n.CustomMessageSource;
import de.fms.scm.utils.LanguageUtil;
import de.fms.scm.web.filters.CorsAllowFilterHandlerInterceptor;
import de.fms.scm.web.filters.ValidatingLocaleChangeInterceptor;
import tiles3.TilesConfigurer;
import tiles3.TilesViewResolver;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Locale;

/**
 * SCM-2269 
 */

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {"de.fms.scm"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.fms.scm.daemon.*"))
public class ScmWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("login");
    }

    /**
     * In Spring MVC applications, we also need a ViewResolver.<br/>
     * Spring comes with a Tiles specific ViewResolver named TilesViewResolver.<br/>
     * Once configured, the view names returned from controller methods will be treated as tiles view
     * and Spring will look for a definition having the same name in definitions XML files.
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        final TilesViewResolver viewResolver = new TilesViewResolver();
        viewResolver.setOrder(1);
        registry.viewResolver(viewResolver);
    }

    /**
     * mvc:resources
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docroot/**").addResourceLocations("/docroot/");
    }

    /**
     * mvc:interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CorsAllowFilterHandlerInterceptor());
        // SCM-1110 - Datums in Interaktive Emails für die Eingabe neuer Trackings falsch dargestellt
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean(name = "configurationProperties")
    public PropertiesFactoryBean configurationProperties() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("configuration.properties"));
        return bean;
    }

    @Bean(name = "versionProperties")
    public PropertiesFactoryBean versionProperties() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("scm_version.properties"));
        return bean;
    }

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
        final PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        final Resource[] resources = new ClassPathResource[]{
                new ClassPathResource("configuration.properties")
                //new ClassPathResource("scm_version.properties")
        };
        ppc.setLocations(resources);
        ppc.setIgnoreUnresolvablePlaceholders(true);
        return ppc;
    }

    /**
     * Registers a ViewResolver returning .jsp views located in directory /WEB-INF/jsp.
     *
     * @implNote VB: This seems to be deprecated and not used, as the main resolver seems to be TilesViewResolver - maybe it should be removed; in my local environment, the project works without this bean definition
     */
    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    /**
     * Registers a ViewResolver of type TilesViewResolver returning .jsp views according to the tiles.xml configuration
     *
     * @see ./seastep-scm-parent/seastep-scm-main/src/main/webapp/WEB-INF/tiles.xml
     * @see ScmWebMvcConfigurer#tilesConfigurer()
     */
    @Bean("tilesViewResolver")
    public ViewResolver tilesViewResolver() {
        final TilesViewResolver viewResolver = new TilesViewResolver();
        viewResolver.setOrder(1);
        return viewResolver;
    }

    /**
     * Registers a ViewResolver of type JsonViewResolver to render JSON data
     *
     * @see JsonViewResolver
     */
    @Bean("jsonViewResolver")
    public ViewResolver jsonViewResolver() {
        final JsonViewResolver viewResolver = new JsonViewResolver();
        viewResolver.setOrder(2);
        return viewResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        //SCM-2421 ValidatingLocaleChangeInterceptor
        final LocaleChangeInterceptor localeChangeInterceptor = new ValidatingLocaleChangeInterceptor();
        // SCM-1110 - Datums in Interaktive Emails für die Eingabe neuer Trackings falsch dargestellt
        localeChangeInterceptor.setParamName(LanguageUtil.LANGUAGE_QUERY_PARAMETER);
        return localeChangeInterceptor;
    }

    @Bean
    public RequestMappingHandlerMapping handlerMapping(final LocaleChangeInterceptor localeChangeInterceptor) {
        //Bug 23421 - Spring 5 Upgrade
        final RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setInterceptors(localeChangeInterceptor);
        handlerMapping.setUseTrailingSlashMatch(false);
        return handlerMapping;
    }

    @Bean
    public CustomMessageSource messageSource() {
        final CustomMessageSource messageSource = new CustomMessageSource();
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        final CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        cookieLocaleResolver.setCookieHttpOnly(false);
        cookieLocaleResolver.setCookieSecure(true);
        return cookieLocaleResolver;
    }

    @Bean
    public RequestMappingHandlerMapping requestMapping(final LocaleChangeInterceptor localeChangeInterceptor) {
        final RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
        requestMappingHandlerMapping.setInterceptors(localeChangeInterceptor);
        requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
        return requestMappingHandlerMapping;
    }

    //SCM-2269
    //CommonsMultipartResolver ersetzt durch StandardServletMultipartResolver
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * A TilesConfigurer simply configures a TilesContainer using a set of files containing definitions
     * to be accessed by TilesView instances.
     */
    @Bean
    public TilesConfigurer tilesConfigurer() {
        final TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions("/WEB-INF/tiles.xml");
        //setCheckRefresh nicht im Produktionsbetrieb verwenden: default ist false
        //tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }

}
