package com.example.adminpanelbackend;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CorsFilter implements Filter {

    // This is to be replaced with a list of domains allowed to access the server
    //You can include more than one origin here
    private final List<String> allowedOrigins = Arrays.asList("http://185.31.160.131:3000");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // Lets make sure that we are working with HTTP (that is, against HttpServletRequest and HttpServletResponse objects)
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            // Access-Control-Allow-Origin
            String origin = request.getHeader("Origin");
            //response.setHeader("Access-Control-Allow-Origin", allowedOrigins.contains(origin) ? origin : "");
            response.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Vary", "Origin");

            // Access-Control-Max-Age
            response.setHeader("Access-Control-Max-Age", "3600");
            // Access-Control-Allow-Credentials
            response.setHeader("Access-Control-Allow-Credentials", "true");

            // Access-Control-Allow-Methods
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");

            // Access-Control-Allow-Headers Origin, X-Requested-With, Content-Type, Accept, " + "X-CSRF-TOKEN
            response.setHeader("Access-Control-Allow-Headers",
                    "Accept, Content-Type, X-Requested-With");
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }
}