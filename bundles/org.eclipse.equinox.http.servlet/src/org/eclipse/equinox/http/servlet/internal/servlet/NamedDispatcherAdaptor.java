/*******************************************************************************
 * Copyright (c) 2005, 2014 Cognos Incorporated, IBM Corporation and others.
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

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.eclipse.equinox.http.servlet.internal.context.DispatchTargets;

//This class unwraps the request so it can be processed by the underlying servlet container.
public class NamedDispatcherAdaptor implements RequestDispatcher {

	private final DispatchTargets dispatchTargets;

	public NamedDispatcherAdaptor(DispatchTargets dispatchTargets) {
		this.dispatchTargets = dispatchTargets;
	}

	public void forward(ServletRequest req, ServletResponse resp)
		throws IOException, ServletException {

		doDispatch((HttpServletRequest)req, (HttpServletResponse)resp, DispatcherType.FORWARD);
	}

	public void include(ServletRequest req, ServletResponse resp)
		throws IOException, ServletException {

		doDispatch((HttpServletRequest)req, (HttpServletResponse)resp, DispatcherType.INCLUDE);
	}

	private void doDispatch(
			HttpServletRequest request, HttpServletResponse response,
			DispatcherType dispatcherType)
		throws IOException, ServletException {

		if (!(request instanceof HttpServletRequestWrapper) ||
			(request instanceof HttpServletRequestBuilder)) {
			request = new HttpServletRequestBuilder(request, dispatchTargets);
		}
		else {
			HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper)request;
			HttpServletRequest wrapped = (HttpServletRequest)wrapper.getRequest();
			while (wrapped instanceof HttpServletRequestWrapper) {
				if (wrapped instanceof HttpServletRequestBuilder) {
					break;
				}

				wrapper = (HttpServletRequestWrapper)wrapped;
				wrapped = (HttpServletRequest)wrapper.getRequest();
			}
			wrapped = new HttpServletRequestBuilder(wrapped, dispatchTargets);
			wrapper.setRequest(wrapped);
		}

		if (!(response instanceof HttpServletResponseWrapper) ||
			(response instanceof HttpServletResponseWrapperImpl)) {
			response = new HttpServletResponseWrapperImpl(response);
		}
		else {
			HttpServletResponseWrapper wrapper = (HttpServletResponseWrapper)response;
			HttpServletResponse wrapped = (HttpServletResponse)wrapper.getResponse();
			while (wrapped instanceof HttpServletResponseWrapper) {
				if (wrapped instanceof HttpServletResponseWrapperImpl) {
					break;
				}

				wrapper = (HttpServletResponseWrapper)wrapped;
				wrapped = (HttpServletResponse)wrapper.getResponse();
			}
			wrapped = new HttpServletResponseWrapperImpl(wrapped);
			wrapper.setResponse(wrapped);
		}

		ResponseStateHandler responseStateHandler = new ResponseStateHandler(
			request, response, dispatchTargets, dispatcherType);

		responseStateHandler.processRequest();
	}

}
