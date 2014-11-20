/*******************************************************************************
 * Copyright (c) Nov 14, 2014 Liferay, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Liferay, Inc. - initial API and implementation and/or initial
 *                    documentation
 ******************************************************************************/

package org.eclipse.equinox.http.servlet.internal.context;

import org.osgi.framework.Bundle;
import org.osgi.service.http.context.ServletContextHelper;

/**
 * @author Raymond Augé
 */
public class NamedServletContextHelper extends ServletContextHelper {

	public NamedServletContextHelper(Bundle bundle, String contextName) {
		super(bundle);

		this.contextName = contextName;
	}

	public String getContextName() {
		return contextName;
	}

	private String contextName;

}