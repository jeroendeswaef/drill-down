package com.recallq.drilldown.ui;

import com.recallq.drilldown.ui.service.SearchResultsService;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Implements common methods used by multiple servlets.
 * Sets up the connection for the Search Service.
 * 
 * 
 * @author Jeroen De Swaef <j@recallq.com>
 */
public class CommonDrillDownServlet  extends HttpServlet {

    private String connectionUrl;
    private String solrUserName;
    private String solrPassword;
    
     @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(); 
        ServletContext servletContext = config.getServletContext();
        connectionUrl = servletContext.getInitParameter("solrUrl");
        solrUserName = servletContext.getInitParameter("solrUsername");
        solrPassword = servletContext.getInitParameter("solrPassword");
    }
    
    protected SearchResultsService getSearchService() {
        System.out.println(">>" + solrUserName + solrPassword + connectionUrl);
        SearchResultsService service = SearchResultsService.getInstance(solrUserName, solrPassword, connectionUrl);
        return service;
    }
}
