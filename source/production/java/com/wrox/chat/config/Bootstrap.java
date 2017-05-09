package com.wrox.chat.config;

import com.wrox.AuthenticationFilter;
import com.wrox.LoggingFilter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Created by phuctran93 on 5/9/2017.
 */
public class Bootstrap implements WebApplicationInitializer
{

    @Override
    public void onStartup(ServletContext container) throws ServletException
    {
        container.getServletRegistration("default").addMapping("/resource/*");

        AnnotationConfigWebApplicationContext rootContext =
                new AnnotationConfigWebApplicationContext();
        rootContext.register(RootContextConfiguration.class);
        container.addListener(new ContextLoaderListener(rootContext));

        AnnotationConfigWebApplicationContext servletContext =
                new AnnotationConfigWebApplicationContext();
        servletContext.register(ServletContextConfiguration.class);
        ServletRegistration.Dynamic dispatcher = container.addServlet(
                "springDispatcher", new DispatcherServlet(servletContext)
        );
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        FilterRegistration.Dynamic registration = container.addFilter(
                "loggingFilter", new LoggingFilter()
        );
        registration.addMappingForUrlPatterns(null, false, "/*");
        registration = container.addFilter(
                "authenticationFilter", new AuthenticationFilter()
        );
        registration.addMappingForUrlPatterns(
                null,
                false,
                "/ticket", "/ticket/*", "/chat","/chat/*","/session","/session/*");
    }
}
