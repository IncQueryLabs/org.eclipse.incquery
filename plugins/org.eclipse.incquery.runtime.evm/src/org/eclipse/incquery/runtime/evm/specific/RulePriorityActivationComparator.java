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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.evm.api.Activation;
import org.eclipse.incquery.runtime.evm.api.RuleInstance;
import org.eclipse.incquery.runtime.evm.api.RuleSpecification;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * 
 * 
 * @author Abel Hegedus
 *
 */
public class RulePriorityActivationComparator implements Comparator<Activation<?>> {

    private Map<RuleSpecification<? extends IPatternMatch>, Integer> rulePriority;
    private Map<RuleSpecification<? extends IPatternMatch>, Comparator<Activation<? extends IPatternMatch>>> comparators;
    private Ordering<Object> arbitraryOrdering = Ordering.arbitrary();
    private Ordering<Activation<?>> arbitraryActivation = new Ordering<Activation<?>>() {
        public int compare(Activation<?> left, Activation<?> right) {
            return arbitraryOrdering.compare(left, right);
        };
    };
    
    public RulePriorityActivationComparator() {
        rulePriority = Maps.newHashMap();
        comparators = Maps.newHashMap();
    }
    
    public static RulePriorityActivationComparator copyOf(RulePriorityActivationComparator comparator) {
        RulePriorityActivationComparator activationComparator = new RulePriorityActivationComparator();
        activationComparator.rulePriority.putAll(comparator.rulePriority);
        activationComparator.comparators.putAll(comparator.comparators);
        return activationComparator;
    }
    
    /**
     * Higher priority means earlier placement in the collection!
     * 
     * Existing priority can only be changed if it does not change the ordering of existing elements!
     * 
     * @param specification
     * @param priority
     * @return new priority after setting, if it is different from the parameter, the setting was unsuccessful
     * as it would alter the order of existing elements.
     */
    public int setRuleSpecificationPriority(RuleSpecification<? extends IPatternMatch> specification, int priority) {
        Preconditions.checkNotNull(specification, "Specification must not be null!");
        Preconditions.checkNotNull(priority, "Priority must not be null!");
        Integer oldPriority = rulePriority.get(specification);
        if(oldPriority != null && !oldPriority.equals(priority)) {
            Range<Integer> range = Ranges.lessThan(oldPriority);
            if(oldPriority < priority) {
                range = Ranges.greaterThan(oldPriority);
            }
            // find closest lower or higher priority
            Collection<Integer> values = rulePriority.values();
            boolean foundSame = false;
            for (Integer value : values) {
                if(range.contains(value)) {
                    if(value < oldPriority) {
                        range = Ranges.open(value, oldPriority);
                    } else {
                        range = Ranges.open(oldPriority, value);
                    }
                    if(!range.contains(priority)) {
                        // outside of range, cannot change
                        return oldPriority;
                    }
                } else if(value.equals(oldPriority)) {
                    if(!foundSame) {
                        foundSame = true;
                    } else {
                        // identical priority, cannot change
                        return oldPriority;
                    }
                }
            }
        }
        // priority is range-checked already, so it is fine
        rulePriority.put(specification, priority);
        return priority;
    }
    
    /**
     * Rule specification with no explicit priority are considered as 0 priority
     *  (and this is also recorded in the comparator for later).
     * 
     * @param specification
     * @return
     */
    public int getRuleSpecificationPriority(RuleSpecification<? extends IPatternMatch> specification) {
        Preconditions.checkNotNull(specification, "Specification must not be null!");
        Integer priority = rulePriority.get(specification);
        if(priority != null) {
            return priority;
        } else {
            rulePriority.put(specification, 0);
            return 0;
        }
    }
    
    public void setActivationComparator(RuleSpecification<? extends IPatternMatch> specification, 
            Comparator<Activation<? extends IPatternMatch>> comparator) {
        Preconditions.checkNotNull(specification, "Specification must not be null!");
        Preconditions.checkNotNull(comparator, "Comparator must not be null!");
        Preconditions.checkArgument(comparators.get(specification).equals(comparator), "Cannot replace existing comparator");
        comparators.put(specification, comparator);
    }
    
    /**
     * 
     * @param specification
     * @return the comparator if exists, null otherwise
     */
    public Comparator<Activation<? extends IPatternMatch>> getActivationComparator(RuleSpecification<? extends IPatternMatch> specification) {
        Preconditions.checkNotNull(specification, "Specification must not be null!");
        
        Comparator<Activation<? extends IPatternMatch>> comparator = comparators.get(specification);
        return comparator;
    }

    @Override
    public int compare(Activation<?> o1, Activation<?> o2) {
        if(o1 == o2) {
            return 0;
        }
        RuleInstance<?> instance = o1.getRule();
        RuleSpecification<?> specification = instance.getSpecification();
        RuleInstance<?> instance2 = o2.getRule();
        RuleSpecification<?> specification2 = instance2.getSpecification();
        if(instance.equals(instance2)) {
            Comparator<Activation<? extends IPatternMatch>> comparator = comparators.get(specification); 
            if(comparator != null) {
                return comparator.compare(o1, o2);
            } else {/*
                 * TODO this would violate a part of the compare() contract:
                 * "Finally, the implementor must ensure that compare(x, y)==0 
                 * implies that sgn(compare(x, z))==sgn(compare(y, z)) for all z."
                 */
                comparators.put(specification, arbitraryActivation);
                return arbitraryActivation.compare(o1, o2);
            }
        } else {
            int priority1 = getRuleSpecificationPriority(specification);
            int priority2 = getRuleSpecificationPriority(specification2);
            if(priority1 == priority2) {
                /*
                 * TODO this would violate a part of the compare() contract:
                 * "Finally, the implementor must ensure that compare(x, y)==0 
                 * implies that sgn(compare(x, z))==sgn(compare(y, z)) for all z."
                 */
                return arbitraryOrdering.compare(o1, o2);
            } else {
                return priority2 - priority1; // no Java7
                // Integer.compare(priority2, priority1); // higher number means bigger priority means earlier in order
            }
        }
        
    }
    
}