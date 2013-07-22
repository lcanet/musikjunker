package org.tekila.musikjunker.web.init;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.tekila.musikjunker.context.AppContext;
import org.tekila.musikjunker.context.ApplicationContextHolder;
import org.tekila.musikjunker.context.WebappContext;


@Slf4j
public class WebConfigurer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		log.info("Starting up MusikJunker Application");

		// SPRING
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.getEnvironment().addActiveProfile("web");
		applicationContext.register(AppContext.class, WebappContext.class);
	    new ContextLoader(applicationContext).initWebApplicationContext(servletContext);


		// SPRING MVC
		DispatcherServlet springServlet = new DispatcherServlet(applicationContext);
		springServlet.setDispatchOptionsRequest(true);
		
		ServletRegistration.Dynamic dispatcherServlet = servletContext
				.addServlet("dispatcher", springServlet);
		dispatcherServlet.addMapping("/services/*");
		dispatcherServlet.setLoadOnStartup(2);

		// FILTRES

		registerFilter(servletContext, new RequestContextFilter(), "/*");

		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		registerFilter(servletContext, encodingFilter, "/*");

		OpenSessionInViewFilter osiv = new OpenSessionInViewFilter();
		osiv.setSingleSession(true);
		registerFilter(servletContext, osiv, "/services/*");

	}

	private void registerFilter(ServletContext sce, Filter filter,
			String... mapTo) {
		FilterRegistration.Dynamic filterRegistration = sce.addFilter(filter
				.getClass().getName(), filter);
		EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST,
				DispatcherType.FORWARD);
		for (String s : mapTo) {
			filterRegistration.addMappingForUrlPatterns(disps, true, s);

		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ApplicationContext applicationContext = ApplicationContextHolder
				.getValue();
		if (applicationContext != null) {
			if (applicationContext instanceof ConfigurableApplicationContext) {
				ConfigurableApplicationContext cfgApplicationContext = (ConfigurableApplicationContext) applicationContext;
				cfgApplicationContext.close();
				ApplicationContextHolder.setValue(null);
			}
		}
	}

}
