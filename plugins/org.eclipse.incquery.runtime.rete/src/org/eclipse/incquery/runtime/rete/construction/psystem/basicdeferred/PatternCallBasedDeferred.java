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

package org.eclipse.incquery.runtime.rete.construction.psystem.basicdeferred;

import java.util.Collections;
import java.util.Set;

import org.eclipse.incquery.runtime.rete.collections.CollectionsFactory;
import org.eclipse.incquery.runtime.rete.construction.QueryPlannerException;
import org.eclipse.incquery.runtime.rete.construction.SubPlan;
import org.eclipse.incquery.runtime.rete.construction.helpers.BuildHelper;
import org.eclipse.incquery.runtime.rete.construction.psystem.PConstraint;
import org.eclipse.incquery.runtime.rete.construction.psystem.PSystem;
import org.eclipse.incquery.runtime.rete.construction.psystem.PVariable;
import org.eclipse.incquery.runtime.rete.construction.psystem.VariableDeferredPConstraint;
import org.eclipse.incquery.runtime.rete.tuple.Tuple;

/**
 * @author Gabor Bergmann
 * 
 */
public abstract class PatternCallBasedDeferred extends VariableDeferredPConstraint {

    protected Tuple actualParametersTuple;

    protected abstract void doDoReplaceVariables(PVariable obsolete, PVariable replacement);

    protected abstract Set<PVariable> getCandidateQuantifiedVariables();

    protected Object pattern;
    private Set<PVariable> deferringVariables;

    /**
     * @param buildable
     * @param additionalAffectedVariables
     */
    public PatternCallBasedDeferred(PSystem pSystem, Tuple actualParametersTuple, Object pattern,
            Set<PVariable> additionalAffectedVariables) {
        super(pSystem, union(actualParametersTuple.<PVariable> getDistinctElements(), additionalAffectedVariables));
        this.actualParametersTuple = actualParametersTuple;
        this.pattern = pattern;
    }

    public PatternCallBasedDeferred(PSystem pSystem, Tuple actualParametersTuple, Object pattern) {
        this(pSystem, actualParametersTuple, pattern, Collections.<PVariable> emptySet());
    }

    private static Set<PVariable> union(Set<PVariable> a, Set<PVariable> b) {
        Set<PVariable> result = CollectionsFactory.getSet();//new HashSet<PVariable>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    @Override
    protected Set<PVariable> getDeferringVariables() {
        if (deferringVariables == null) {
            deferringVariables = CollectionsFactory.getSet();//new HashSet<PVariable>();
            for (PVariable var : getCandidateQuantifiedVariables()) {
                if (var.isDeducable())
                    deferringVariables.add(var);
            }
        }
        return deferringVariables;
    }

    @Override
    public void checkSanity() throws QueryPlannerException {
        super.checkSanity();
        for (Object obj : this.actualParametersTuple.getDistinctElements()) {
            PVariable var = (PVariable) obj;
            if (!getDeferringVariables().contains(var)) {
                // so this is a free variable of the NAC / aggregation?
                for (PConstraint pConstraint : var.getReferringConstraints()) {
                    if (pConstraint != this && !(pConstraint instanceof Equality && ((Equality) pConstraint).isMoot()))
                        throw new QueryPlannerException(
                                "Variable {1} of constraint {2} is not a positively determined part of the pattern, yet it is also affected by {3}.",
                                new String[] { var.toString(), this.toString(), pConstraint.toString() },
                                "Read-only variable can not be deduced", null);
                }
            }
        }

    }

    /**
     * @param subPlan
     * @param sideStub
     * @return
     */
    protected BuildHelper.JoinHelper getJoinHelper(SubPlan subPlan, SubPlan sideStub) {
        BuildHelper.JoinHelper joinHelper = new BuildHelper.JoinHelper(subPlan, sideStub);
        return joinHelper;
    }

    /**
     * @return
     * @throws QueryPlannerException
     */
    protected SubPlan getSideStub() throws QueryPlannerException {
        // Stub sideStub = buildable.patternCallStub(actualParametersTuple, pattern);
        // sideStub = BuildHelper.enforceVariableCoincidences(buildable, sideStub);
        // return sideStub;
        return null;
    }

    @Override
    protected void doReplaceVariable(PVariable obsolete, PVariable replacement) {
        if (deferringVariables != null) {
            throw new IllegalStateException("Cannot replace variables on " + this
                    + " when deferring variables have already been identified.");
        }
        actualParametersTuple.replaceAll(obsolete, replacement);
        doDoReplaceVariables(obsolete, replacement);
    }

}