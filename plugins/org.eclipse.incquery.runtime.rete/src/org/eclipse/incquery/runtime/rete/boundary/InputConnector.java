/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.rete.boundary;

import java.util.Collection;

import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.rete.network.Network;
import org.eclipse.incquery.runtime.rete.network.Node;
import org.eclipse.incquery.runtime.rete.recipes.InputFilterRecipe;
import org.eclipse.incquery.runtime.rete.recipes.InputRecipe;
import org.eclipse.incquery.runtime.rete.remote.Address;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * A class responsible for connecting input nodes to the runtime context.
 * 
 * @author Bergmann Gabor
 *
 */
public final class InputConnector {
	Network network;
	
    protected Table<IInputKey, Tuple, Address<ExternalInputEnumeratorNode>> externalInputRoots = HashBasedTable.create(100, 1);
	
//    /*
//     * arity:1 used as simple entity constraints label is the object representing the type null label means all entities
//     * regardless of type (global supertype), if allowed
//     */
//    protected Map<Object, Address<? extends Tunnel>> unaryRoots = CollectionsFactory.getMap();
//    /*
//     * arity:3 (rel, from, to) used as VPM relation constraints null label means all relations regardless of type
//     * (global supertype)
//     */
//    protected Map<Object, Address<? extends Tunnel>> ternaryEdgeRoots = CollectionsFactory.getMap();
//    /*
//     * arity:2 (from, to) not used over VPM; can be used as EMF references for instance label is the object representing
//     * the type null label means all entities regardless of type if allowed (global supertype), if allowed
//     */
//    protected Map<Object, Address<? extends Tunnel>> binaryEdgeRoots = CollectionsFactory.getMap();
//	
//    protected Address<? extends Tunnel> containmentRoot = null;
//    protected Address<? extends Supplier> containmentTransitiveRoot = null;
//    protected Address<? extends Tunnel> instantiationRoot = null;
//    protected Address<? extends Supplier> instantiationTransitiveRoot = null;
//    protected Address<? extends Tunnel> generalizationRoot = null;
//    protected Address<? extends Supplier> generalizationTransitiveRoot = null;
	

	public InputConnector(Network network) {
		super();
		this.network = network;
	}
	

	public Network getNetwork() {
		return network;
	}


	/**
	 * Connects a given input filter node to the external input source.
	 */
	public void connectInputFilter(InputFilterRecipe recipe, Node freshNode) {
		final ExternalInputStatelessFilterNode inputNode = (ExternalInputStatelessFilterNode)freshNode;
		
		IInputKey inputKey = (IInputKey) recipe.getInputKey();
		inputNode.connectThroughContext(network.getEngine(), inputKey);
	}


	/**
	 * Connects a given input enumerator node to the external input source.
	 */
	public void connectInput(InputRecipe recipe, Node freshNode) {
		final ExternalInputEnumeratorNode inputNode = (ExternalInputEnumeratorNode)freshNode;
		
		IInputKey inputKey = (IInputKey) recipe.getInputKey();
		Tuple seed = nopSeed(inputKey); // no preseeding as of now
		final Address<ExternalInputEnumeratorNode> freshAddress = Address.of(inputNode);
		externalInputRoots.put(inputKey, seed, freshAddress);
		inputNode.connectThroughContext(network.getEngine(), inputKey, seed);
		
//		final Address<Tunnel> freshAddress = Address.of((Tunnel)freshNode);
//		if (recipe instanceof TypeInputRecipe) {
//			final Object typeKey = ((TypeInputRecipe) recipe).getTypeKey();
//			
//			if (recipe instanceof UnaryInputRecipe) {
//				unaryRoots.put(typeKey, freshAddress);
//				new EntityFeeder(freshAddress, this, typeKey).feed();
////		        if (typeObject != null && generalizationQueryDirection == GeneralizationQueryDirection.BOTH) {
////		            Collection<? extends Object> subTypes = context.enumerateDirectUnarySubtypes(typeObject);
////		
////		            for (Object subType : subTypes) {
////		                Address<? extends Tunnel> subRoot = accessUnaryRoot(subType);
////		                network.connectRemoteNodes(subRoot, tn, true);
////		            }
////		        }
//			} else if (recipe instanceof BinaryInputRecipe) {
//				binaryEdgeRoots.put(typeKey, freshAddress);
//				externalInputRoots.put(rowKey, columnKey, freshAddress);
//				new ReferenceFeeder(freshAddress, this, typeKey).feed();
//				//        if (typeObject != null && generalizationQueryDirection == GeneralizationQueryDirection.BOTH) {
//				//            Collection<? extends Object> subTypes = context.enumerateDirectTernaryEdgeSubtypes(typeObject);
//				//
//				//            for (Object subType : subTypes) {
//				//                Address<? extends Tunnel> subRoot = accessTernaryEdgeRoot(subType);
//				//                network.connectRemoteNodes(subRoot, tn, true);
//				//            }
//				//        }
//			}
//			
//			
//		}
		
	}
		
//    /**
//     * fetches the entity Root node under specified label; returns null if it doesn't exist yet
//     */
//    public Address<? extends Tunnel> getUnaryRoot(Object label) {
//        return unaryRoots.get(label);
//    }
//
//    public Collection<Address<? extends Tunnel>> getAllUnaryRoots() {
//        return unaryRoots.values();
//    }
//
//    /**
//     * fetches the relation Root node under specified label; returns null if it doesn't exist yet
//     */
//    public Address<? extends Tunnel> getTernaryEdgeRoot(Object label) {
//        return ternaryEdgeRoots.get(label);
//    }
//
//    public Collection<Address<? extends Tunnel>> getAllTernaryEdgeRoots() {
//        return ternaryEdgeRoots.values();
//    }
//    
//    /**
//     * fetches the reference Root node under specified label; returns null if it doesn't exist yet
//     */
//    public Address<? extends Tunnel> getBinaryEdgeRoot(Object label) {
//        return binaryEdgeRoots.get(label);
//    }
//
//    public Collection<Address<? extends Tunnel>> getAllBinaryEdgeRoots() {
//        return binaryEdgeRoots.values();
//    }
//
//
//	public Address<? extends Tunnel> getContainmentRoot() {
//		return containmentRoot;
//	}
//
//
//	public Address<? extends Supplier> getContainmentTransitiveRoot() {
//		return containmentTransitiveRoot;
//	}
//
//
//	public Address<? extends Tunnel> getInstantiationRoot() {
//		return instantiationRoot;
//	}
//
//
//	public Address<? extends Supplier> getInstantiationTransitiveRoot() {
//		return instantiationTransitiveRoot;
//	}
//
//
//	public Address<? extends Tunnel> getGeneralizationRoot() {
//		return generalizationRoot;
//	}

    
    public Collection<Address<ExternalInputEnumeratorNode>> getAllExternalInputNodes() {
    	return externalInputRoots.values();
    }
    public Collection<Address<ExternalInputEnumeratorNode>> getAllExternalInputNodesForKey(IInputKey inputKey) {
    	return externalInputRoots.row(inputKey).values();
    }
    public Address<ExternalInputEnumeratorNode> getExternalInputNodeForKeyUnseeded(IInputKey inputKey) {
    	return externalInputRoots.get(inputKey, null);
    }
    public Address<ExternalInputEnumeratorNode> getExternalInputNode(IInputKey inputKey, Tuple seed) {
    	if (seed == null) seed = nopSeed(inputKey);
    	return externalInputRoots.get(inputKey, seed);
    }


	Tuple nopSeed(IInputKey inputKey) {
		return new FlatTuple(new Object[inputKey.getArity()]);
	}
    
    
}
