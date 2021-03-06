/*******************************************************************************
 * Copyright (c) 2004-2008 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.runtime.rete.single;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.matchers.tuple.TupleMask;
import org.eclipse.incquery.runtime.rete.index.MemoryIdentityIndexer;
import org.eclipse.incquery.runtime.rete.index.MemoryNullIndexer;
import org.eclipse.incquery.runtime.rete.index.ProjectionIndexer;
import org.eclipse.incquery.runtime.rete.network.Direction;
import org.eclipse.incquery.runtime.rete.network.ReteContainer;
import org.eclipse.incquery.runtime.rete.network.StandardNode;
import org.eclipse.incquery.runtime.rete.network.Supplier;
import org.eclipse.incquery.runtime.rete.network.Tunnel;
import org.eclipse.incquery.runtime.rete.traceability.TraceInfo;
import org.eclipse.incquery.runtime.rete.tuple.TupleMemory;
import org.eclipse.incquery.runtime.rete.util.Options;

/**
 * Ensures that no identical copies get to the output. Only one replica of each pattern substitution may traverse this
 * node.
 * 
 * @author Gabor Bergmann
 */
public class UniquenessEnforcerNode extends StandardNode implements Tunnel {

    protected Collection<Supplier> parents;
    protected TupleMemory memory;

    public TupleMemory getMemory() {
        return memory;
    }

    protected MemoryNullIndexer memoryNullIndexer;
    protected MemoryIdentityIndexer memoryIdentityIndexer;
    protected final int tupleWidth;

    private final TupleMask nullMask;
    private final TupleMask identityMask;

    public UniquenessEnforcerNode(ReteContainer reteContainer, int tupleWidth) {
        super(reteContainer);
        parents = new ArrayList<Supplier>();
        memory = new TupleMemory();
        this.tupleWidth = tupleWidth;
        reteContainer.registerClearable(memory);
        nullMask = TupleMask.linear(0, tupleWidth);
        identityMask = TupleMask.identity(tupleWidth);
        // if (Options.employTrivialIndexers) {
        // memoryNullIndexer = new MemoryNullIndexer(reteContainer, tupleWidth, memory, this, this);
        // reteContainer.getLibrary().registerSpecializedProjectionIndexer(this, memoryNullIndexer);
        // memoryIdentityIndexer = new MemoryIdentityIndexer(reteContainer, tupleWidth, memory, this, this);
        // reteContainer.getLibrary().registerSpecializedProjectionIndexer(this, memoryIdentityIndexer);
        // }
    }

    @Override
    public void update(Direction direction, Tuple updateElement) {
        boolean change;
        if (direction == Direction.INSERT) {
            change = memory.add(updateElement);
        } else { // REVOKE
            try {
                change = memory.remove(updateElement);
            } catch (java.lang.NullPointerException ex) {
                // TODO UGLY, but will it find our problems?
                change = false;
                reteContainer
                        .getNetwork()
                        .getEngine()
                        .getLogger()
                        .error(
                                "[INTERNAL ERROR] Duplicate deletion of " + updateElement
                                        + " was detected in UniquenessEnforcer " + this 
                                        + " for pattern(s) " + getTraceInfoPatternsEnumerated(), ex);
            }
        }
        if (change) {
            propagateUpdate(direction, updateElement);

            // trivial projectionIndexers
            if (memoryIdentityIndexer != null)
                memoryIdentityIndexer.propagate(direction, updateElement);
            if (memoryNullIndexer != null)
                memoryNullIndexer.propagate(direction, updateElement);

        }
    }

    @Override
    public ProjectionIndexer constructIndex(TupleMask mask, TraceInfo... traces) {
        if (Options.employTrivialIndexers) {
            if (nullMask.equals(mask)) {
                final MemoryNullIndexer indexer = getNullIndexer();
                for (TraceInfo traceInfo : traces) indexer.assignTraceInfo(traceInfo);                
				return indexer;
            } if (identityMask.equals(mask)) {
                final MemoryIdentityIndexer indexer = getIdentityIndexer();
                for (TraceInfo traceInfo : traces) indexer.assignTraceInfo(traceInfo);                
				return indexer;
            }
        }
        return super.constructIndex(mask, traces);
    }

    @Override
    public void pullInto(Collection<Tuple> collector) {
        collector.addAll(memory);
    }

    public MemoryNullIndexer getNullIndexer() {
        if (memoryNullIndexer == null)
            memoryNullIndexer = new MemoryNullIndexer(reteContainer, tupleWidth, memory, this, this);
        return memoryNullIndexer;
    }

    public MemoryIdentityIndexer getIdentityIndexer() {
        if (memoryIdentityIndexer == null)
            memoryIdentityIndexer = new MemoryIdentityIndexer(reteContainer, tupleWidth, memory, this, this);
        return memoryIdentityIndexer;
    }

    @Override
    public void appendParent(Supplier supplier) {
        parents.add(supplier);
    }

    @Override
    public void removeParent(Supplier supplier) {
        parents.remove(supplier);
    }

    @Override
    public Collection<Supplier> getParents() {
        return parents;
    }

    @Override
    public void assignTraceInfo(TraceInfo traceInfo) {
    	super.assignTraceInfo(traceInfo);
    	if (traceInfo.propagateFromStandardNodeToSupplierParent())
    		for (Supplier parent : parents)
    			parent.acceptPropagatedTraceInfo(traceInfo);
    }

    
    //
    // public void tearOff() {
    // for (Supplier parent : new LinkedList<Supplier>(parents) )
    // {
    // network.disconnectAndDesynchronize(parent, this);
    // }
    // }
    //
    // /**
    // * @return the dirty
    // */
    // public boolean isDirty() {
    // return dirty;
    // }
    //
    // /**
    // * @param dirty the dirty to set
    // */
    // public void setDirty(boolean dirty) {
    // this.dirty = dirty;
    // }

}
