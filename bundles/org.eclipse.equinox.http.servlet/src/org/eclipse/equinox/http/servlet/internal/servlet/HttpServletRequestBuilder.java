/*******************************************************************************
 * Copyright (c) 2005, 2015 Cognos Incorporated, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Cognos Incorporated - initial API and implementation
 *     IBM Corporation - bug fixes and enhancements
 *     Raymond Augé <raymond.auge@liferay.com> - Bug 436698
 *******************************************************************************/
package org.eclipse.equinox.http.servlet.internal.servlet;

import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import org.eclipse.equinox.http.servlet.internal.context.DispatchTargets;
import org.eclipse.equinox.http.servlet.internal.registration.EndpointRegistration;
import org.eclipse.equinox.http.servlet.internal.util.Const;
import org.eclipse.equinox.http.servlet.internal.util.EventListeners;
import org.osgi.service.http.HttpContext;

public class HttpServletRequestBuilder extends HttpServletRequestWrapper {

	private DispatchTargets dispatchTargets;
	private EndpointRegistration<?> servletRegistration;

	public HttpServletRequestBuilder(HttpServletRequest request, DispatchTargets dispatchTargets) {
		super(request);

		this.dispatchTargets = dispatchTargets;
		this.servletRegistration = dispatchTargets.getServletRegistration();
	}

	public String getAuthType() {
		String authType = (String)super.getAttribute(HttpContext.AUTHENTICATION_TYPE);
		if (authType != null)
			return authType;

		return super.getAuthType();
	}

	public String getRemoteUser() {
		String remoteUser = (String)super.getAttribute(HttpContext.REMOTE_USER);
		if (remoteUser != null)
			return remoteUser;

		return super.getRemoteUser();
	}

	public String getPathInfo() {
		return dispatchTargets.getPathInfo();
	}

	public ServletContext getServletContext() {
		return servletRegistration.getServletContext();
	}

	public String getServletPath() {
		if (super.getDispatcherType() == DispatcherType.INCLUDE)
			return super.getServletPath();

		if (dispatchTargets.getServletPath().equals(Const.SLASH)) {
			return Const.BLANK;
		}
		return dispatchTargets.getServletPath();
	}

	public String getContextPath() {
		return dispatchTargets.getContextController().getFullContextPath();
	}

	public Object getAttribute(String attributeName) {
		String servletPath = dispatchTargets.getServletPath();
		if (super.getDispatcherType() == DispatcherType.INCLUDE) {
			if (attributeName.equals(RequestDispatcher.INCLUDE_CONTEXT_PATH)) {
				String contextPath = (String)super.getAttribute(RequestDispatcher.INCLUDE_CONTEXT_PATH);
				if (contextPath == null || contextPath.equals(Const.SLASH))
					contextPath = Const.BLANK;

				String includeServletPath = (String)super.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
				if (includeServletPath == null || includeServletPath.equals(Const.SLASH))
					includeServletPath = Const.BLANK;

				return contextPath + includeServletPath;
			} else if (attributeName.equals(RequestDispatcher.INCLUDE_SERVLET_PATH)) {
				if (servletPath.equals(Const.SLASH)) {
					return Const.BLANK;
				}
				return servletPath;
			} else if (attributeName.equals(RequestDispatcher.INCLUDE_PATH_INFO)) {
				String pathInfoAttribute = (String) super.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
				if (servletPath.equals(Const.SLASH)) {
					return pathInfoAttribute;
				}

				if ((pathInfoAttribute == null) || (pathInfoAttribute.length() == 0)) {
					return null;
				}

				if (pathInfoAttribute.startsWith(dispatchTargets.getContextController().getContextPath())) {
					pathInfoAttribute = pathInfoAttribute.substring(dispatchTargets.getContextController().getContextPath().length());
				}

				if (pathInfoAttribute.startsWith(servletPath)) {
					pathInfoAttribute = pathInfoAttribute.substring(servletPath.length());
				}

				if (pathInfoAttribute.length() == 0)
					return null;

				return pathInfoAttribute;
			}
		}
		else if (super.getDispatcherType() == DispatcherType.FORWARD) {
			if (attributeName.equals(RequestDispatcher.FORWARD_PATH_INFO)) {
				return super.getPathInfo();
			}
		}

		return super.getAttribute(attributeName);
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		if (!path.startsWith(getContextPath())) {
			path = getContextPath().substring(
				super.getContextPath().length()).concat(path);
		}

		return super.getRequestDispatcher(path);
	}

	public static String getDispatchPathInfo(HttpServletRequest req) {
		if (req.getDispatcherType() == DispatcherType.INCLUDE)
			return (String) req.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
		if (req.getDispatcherType() == DispatcherType.FORWARD)
			return (String) req.getAttribute(RequestDispatcher.FORWARD_PATH_INFO);

		return req.getPathInfo();
	}

	public static String getDispatchServletPath(HttpServletRequest req) {
		if (req.getDispatcherType() == DispatcherType.INCLUDE) {
			String servletPath = (String) req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
			return (servletPath == null) ? Const.BLANK : servletPath;
		}

		return req.getServletPath();
	}

	@Override
	public HttpServletRequest getRequest() {
		return (HttpServletRequest)super.getRequest();
	}

	public HttpSession getSession() {
		HttpSession session = super.getSession();
		if (session != null) {
			return dispatchTargets.getContextController().getSessionAdaptor(
				session, servletRegistration.getT().getServletConfig().getServletContext());
		}

		return null;
	}

	public HttpSession getSession(boolean create) {
		HttpSession session = super.getSession(create);
		if (session != null) {
			return dispatchTargets.getContextController().getSessionAdaptor(
				session, servletRegistration.getT().getServletConfig().getServletContext());
		}

		return null;
	}

	public void removeAttribute(String name) {
		super.removeAttribute(name);

		EventListeners eventListeners = dispatchTargets.getContextController().getEventListeners();

		List<ServletRequestAttributeListener> listeners = eventListeners.get(
			ServletRequestAttributeListener.class);

		if (listeners.isEmpty()) {
			return;
		}

		ServletRequestAttributeEvent servletRequestAttributeEvent =
			new ServletRequestAttributeEvent(
				servletRegistration.getServletContext(), getRequest(), name, null);

		for (ServletRequestAttributeListener servletRequestAttributeListener : listeners) {
			servletRequestAttributeListener.attributeRemoved(
				servletRequestAttributeEvent);
		}
	}

	public void setAttribute(String name, Object value) {
		boolean added = (super.getAttribute(name) == null);
		super.setAttribute(name, value);

		EventListeners eventListeners = dispatchTargets.getContextController().getEventListeners();

		List<ServletRequestAttributeListener> listeners = eventListeners.get(
			ServletRequestAttributeListener.class);

		if (listeners.isEmpty()) {
			return;
		}

		ServletRequestAttributeEvent servletRequestAttributeEvent =
			new ServletRequestAttributeEvent(
				servletRegistration.getServletContext(), getRequest(), name, value);

		for (ServletRequestAttributeListener servletRequestAttributeListener : listeners) {
			if (added) {
				servletRequestAttributeListener.attributeAdded(
					servletRequestAttributeEvent);
			}
			else {
				servletRequestAttributeListener.attributeReplaced(
					servletRequestAttributeEvent);
			}
		}
	}

}