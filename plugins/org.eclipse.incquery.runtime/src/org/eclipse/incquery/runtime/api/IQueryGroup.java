/*******************************************************************************
 * Copyright (c) 2010-2012, Mark Czotter, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mark Czotter - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.api;

import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.exception.IncQueryException;

/**
 * Generic interface for group of query specifications.
 * 
 * <p>It handles more than one patterns as a group, and provides functionality to initialize the matchers together (which
 * has performance benefits).
 * 
 * @author Mark Czotter
 * 
 */
public interface IQueryGroup {

    /**
     * Initializes matchers for the group of patterns within an {@link IncQueryEngine}. If some of the pattern matchers are already
     * constructed in the engine, no task is performed for them.
     * 
     * <p>
     * This preparation step has the advantage that it prepares pattern matchers for an arbitrary number of patterns in a
     * single-pass traversal of the model. 
     * This is typically more efficient than traversing the model each time an individual pattern matcher is initialized on demand. 
     * The performance benefit only manifests itself if the engine is not in wildcard mode.
     * 
     * @param engine
     *            the existing IncQuery engine in which the matchers will be created.
     * @throws IncQueryException
     *             if there was an error in preparing the engine
     */
    public void prepare(IncQueryEngine engine) throws IncQueryException;

    /**
     * Initializes the contained patterns over a given EMF model root (recommended: Resource or ResourceSet). If a
     * pattern matcher engine with the same root already exists, it will be reused.
     * 
     * <p>The scope of pattern matching will be the given EMF model root and below (see FAQ for more precise definition).
     * The match set will be incrementally refreshed upon updates from this scope.
     * 
     * @param emfRoot
     *            the root of the EMF tree where the pattern matchers will operate. Recommended: Resource or
     *            ResourceSet.
     * @throws IncQueryException
     *             if an error occurs during pattern matcher creation
     * @deprecated use {@link #prepare(IncQueryEngine)} instead, e.g. in conjunction with {@link IncQueryEngine#on(Notifier)}            
     */
	@Deprecated
    public void prepare(Notifier emfRoot) throws IncQueryException;

    /**
     * Returns the currently assigned {@link IQuerySpecification}s.
     */
    public Set<IQuerySpecification<?>> getSpecifications();

}
