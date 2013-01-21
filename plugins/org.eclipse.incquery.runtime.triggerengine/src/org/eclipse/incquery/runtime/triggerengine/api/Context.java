/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.triggerengine.api;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Abel Hegedus
 *
 */
public class Context {

    private Map<String, Object> sessionData;
    
    protected Context() {
        this.sessionData = new HashMap<String, Object>();
    }
    
    public static Context create() {
        return new Context();
    }
    
    public Object get(final String key) {
        return sessionData.get(key);
    }
    
    public Object put(final String key, final Object value) {
        return sessionData.put(key, value);
    }
    
    public Object remove(final String key) {
        return sessionData.remove(key);
    }
    
    protected void clear() {
        sessionData.clear();
    }
}