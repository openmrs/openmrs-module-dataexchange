package org.openmrs.module.dataexchange.filter;

import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DataExchangeAuthenticationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(DataExchangeAuthenticationFilter.class);

    /**
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing DataExchangeAuthenticationFilter...");
        }
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(!Context.isAuthenticated()){
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }


        chain.doFilter(request, response);
    }

    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying DataExchangeAuthentiationFilter...");
        }
    }

}
