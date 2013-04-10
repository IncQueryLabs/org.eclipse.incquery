/*******************************************************************************
 * Copyright (c) 2004-2010 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.runtime.rete.construction.psystem;

import java.util.Set;

import org.eclipse.incquery.runtime.rete.construction.RetePatternBuildException;
import org.eclipse.incquery.runtime.rete.construction.Stub;

/**
 * Any constraint that can only be checked on certain stubs (e.g. those stubs that already contain some variables).
 * 
 * @author Gabor Bergmann
 * 
 */
public abstract class DeferredPConstraint<PatternDescription> extends BasePConstraint<PatternDescription> {

    public DeferredPConstraint(PSystem<PatternDescription> pSystem, Set<PVariable> affectedVariables) {
        super(pSystem, affectedVariables);
    }

    public abstract boolean isReadyAt(Stub stub);

    /**
     * @pre this.isReadyAt(stub);
     */
    public Stub checkOn(Stub stub) throws RetePatternBuildException {
        Stub newStub = doCheckOn(stub);
        newStub.addConstraint(this);
        return newStub;
    }

    protected abstract Stub doCheckOn(Stub stub) throws RetePatternBuildException;

    /**
     * Called when the constraint is not ready, but cannot be deferred further.
     * 
     * @param stub
     * @throws RetePatternBuildException
     *             to indicate the error in detail. PRE: !isReady(stub)
     */
    public abstract void raiseForeverDeferredError(Stub stub) throws RetePatternBuildException;
}
