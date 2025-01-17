package com.intuit.tank.util;

/*
 * #%L
 * JSF Support Beans
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intuit.tank.auth.TankSecurityContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.intuit.tank.dao.UserDao;
import com.intuit.tank.project.User;
import com.intuit.tank.vm.settings.TankConfig;

@WebFilter(urlPatterns = "/rest/*")
public class RestSecurityFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(RestSecurityFilter.class);

    private TankConfig config;

    @Inject
    private TankSecurityContext securityContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = new TankConfig();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (config.isRestSecurityEnabled()) {
            User user = getUser((HttpServletRequest) request);
            if (user == null) {
                // send 401 unauthorized and return
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return; // break filter chain, requested JSP/servlet will not be executed
            }
        }
        chain.doFilter(request, response);
    }

    public User getUser(HttpServletRequest req) {
        User user = null;
        // first try the session
        if (securityContext.getCallerPrincipal() != null) {
            user = new UserDao().findByUserName(securityContext.getCallerPrincipal().getName());
        }
        if (user == null) {
            String authHeader = req.getHeader("authorization");
            try {
                if (authHeader != null) {
                    String[] split = StringUtils.split(authHeader, ' ');
                    if (split.length == 2) {
                        String s = new String(Base64.decodeBase64(split[1]), StandardCharsets.UTF_8);
                        String[] upass = StringUtils.split(s, ":", 2);
                        if (upass.length == 2) {
                            String name = upass[0];
                            String token = upass[1];
                            UserDao userDao = new UserDao();
                            user = userDao.findByApiToken(token);
                            if (user == null || user.getName().equals(name)) {
                                user = userDao.authenticate(name, token);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error getting user: " + e, e);
            }
        }
        return user;
    }
}
