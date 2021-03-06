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
package org.eclipse.incquery.runtime.evm.specific;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.evm.api.Activation;
import org.eclipse.incquery.runtime.evm.api.Context;
import org.eclipse.incquery.runtime.evm.api.Job;
import org.eclipse.incquery.runtime.evm.api.event.ActivationState;
import org.eclipse.incquery.runtime.evm.specific.event.IncQueryActivationStateEnum;
import org.eclipse.incquery.runtime.evm.specific.job.EnableJob;
import org.eclipse.incquery.runtime.evm.specific.job.ErrorLoggingJob;
import org.eclipse.incquery.runtime.evm.specific.job.StatelessJob;

/**
 * Provides static methods acting on or generating a {@link Job}.
 *
 * @author Abel Hegedus
 *
 */
public final class Jobs {

    /**
     *
     */
    private Jobs() {
    }

    /**
     * Creates a {@link StatelessJob} for the given state with the given processor. A stateless job simply processes the
     * match in the activation during execution.
     *
     * @param incQueryActivationStateEnum
     * @param processor
     */
    public static <Match extends IPatternMatch> Job<Match> newStatelessJob(
            final IncQueryActivationStateEnum incQueryActivationStateEnum, final IMatchProcessor<Match> processor) {
        return new StatelessJob<Match>(incQueryActivationStateEnum, processor);
    }

    /**
     * Creates a {@link Job} that does not have any effect. Useful when you don't want to do anything for a given
     * activation state but you want to fire.
     *
     * Consider using your own LifeCycle instead of Nop jobs!
     *
     * @param activationState
     */
    public static final <EventAtom> Job<EventAtom> newNopJob(final ActivationState activationState) {
        return new Job<EventAtom>(activationState) {
            @Override
            protected void execute(final Activation<? extends EventAtom> activation, final Context context) {
                // do nothing
            }

            @Override
            protected void handleError(final Activation<? extends EventAtom> activation, final Exception exception,
                    final Context context) {
                // never happens!
                checkState(false, "NopJob should never cause errors!");
            }
        };
    }

    public static <EventAtom> Job<EventAtom> newEnableJob(final Job<EventAtom> job) {
        return new EnableJob<EventAtom>(job);
    }

    public static <EventAtom> Job<EventAtom> newErrorLoggingJob(final Job<EventAtom> job) {
        return new ErrorLoggingJob<EventAtom>(job);
    }

}
