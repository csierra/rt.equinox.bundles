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

package org.eclipse.equinox.http.servlet.internal.context;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public class DefaultServletContextHelper extends NamedServletContextHelper
	implements HttpContext {

	public DefaultServletContextHelper(Bundle bundle, String contextName) {
		super(bundle, contextName);
	}

}