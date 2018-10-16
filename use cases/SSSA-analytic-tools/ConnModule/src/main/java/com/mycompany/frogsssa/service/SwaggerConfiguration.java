package com.mycompany.frogsssa.service;

import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by gabriele on 23/10/17.
 */

public class SwaggerConfiguration extends HttpServlet{
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setTitle("Search engine");
        beanConfig.setVersion("1.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:1527");
        beanConfig.setBasePath("/api_docs/");
        beanConfig.setResourcePackage("com.mycompany.frogsssa");
        //beanConfig.setScan(true);
        beanConfig.setDescription("State controller for multiple SDN applications");
    }
}
