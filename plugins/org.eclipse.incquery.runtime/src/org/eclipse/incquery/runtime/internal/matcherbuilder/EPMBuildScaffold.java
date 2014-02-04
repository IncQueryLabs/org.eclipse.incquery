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

package org.eclipse.incquery.runtime.internal.matcherbuilder;

import org.eclipse.incquery.runtime.matchers.IPatternMatcherContext;
import org.eclipse.incquery.runtime.matchers.planning.IOperationCompiler;
import org.eclipse.incquery.runtime.matchers.planning.QueryPlannerException;
import org.eclipse.incquery.runtime.matchers.planning.SubPlan;
import org.eclipse.incquery.runtime.matchers.planning.helpers.BuildHelper;
import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PQuery;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.rete.util.Options;
import org.eclipse.incquery.runtime.rete.util.Options.BuilderMethod;

/**
 * @author Bergmann Gábor
 * 
 */
public class EPMBuildScaffold<Collector> {

    protected IOperationCompiler<Collector> operationCompiler;
    protected IPatternMatcherContext context;

    public EPMBuildScaffold(IOperationCompiler<Collector> operationCompiler,
            IPatternMatcherContext context) {
        super();
        this.operationCompiler = operationCompiler;
        this.context = context;
    }

    public Collector construct(PQuery pattern) throws QueryPlannerException {
        Collector production = operationCompiler.putOnTab(pattern, context).patternCollector(pattern);
        // TODO check annotations for reinterpret

        context.logDebug("EPMBuilder starts construction of: " + pattern.getFullyQualifiedName());
        for (PBody body : pattern.getContainedBodies()) {
            IOperationCompiler<Collector> currentBuildable = operationCompiler.getNextContainer().putOnTab(
                    pattern, context);
            if (Options.builderMethod == BuilderMethod.LEGACY) {
                throw new UnsupportedOperationException();
            } else {
                SubPlan bodyFinal = Options.builderMethod.layoutStrategy()
                        .layout(body, currentBuildable, context);
                BuildHelper.projectIntoCollector(currentBuildable, bodyFinal, production,
                        body.getSymbolicParameters().toArray(new PVariable[body.getSymbolicParameters().size()]));
            }
        }
        operationCompiler.patternFinished(pattern, context, production);

        return null;
    }

}
