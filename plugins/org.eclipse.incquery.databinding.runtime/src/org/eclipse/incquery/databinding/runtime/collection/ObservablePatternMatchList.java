/*******************************************************************************
 * Copyright (c) 2010-2012, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.databinding.runtime.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.runtime.Assert;
import org.eclipse.incquery.databinding.runtime.api.IncQueryObservables;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.evm.api.ExecutionSchema;
import org.eclipse.incquery.runtime.evm.api.RuleEngine;
import org.eclipse.incquery.runtime.evm.api.RuleSpecification;
import org.eclipse.incquery.runtime.evm.api.event.EventFilter;
import org.eclipse.incquery.runtime.evm.specific.Rules;
import org.eclipse.incquery.runtime.evm.specific.event.IncQueryFilterSemantics;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Observable view of a match set for a given {@link IncQueryMatcher} on a model (match sets of an
 * {@link IncQueryMatcher} are ordered by the order of their appearance).
 * 
 * <p>
 * This implementation uses the {@link ExecutionSchema} to get notifications for match set changes, and can be
 * instantiated using either an existing {@link IncQueryMatcher}, or an {@link IQuerySpecification} and either a
 * {@link IncQueryEngine} or {@link ExecutionSchema}.
 * 
 * @author Abel Hegedus
 * 
 */
public class ObservablePatternMatchList<Match extends IPatternMatch> extends AbstractObservableList {

    private final List<Match> cache = Collections.synchronizedList(new LinkedList<Match>());
    private final ListCollectionUpdate updater;
    private RuleSpecification<Match> specification;
    private EventFilter<Match> matchFilter;
    private RuleEngine ruleEngine;
    
    /**
     * Creates an observable view of the match set of the given {@link IQuerySpecification} initialized on the given
     * {@link IncQueryEngine}.
     * 
     * <p>
     * Consider using {@link IncQueryObservables#observeMatchesAsList} instead!
     * 
     * @param querySpecification
     *            the {@link IQuerySpecification} used to create a matcher
     * @param engine
     *            the {@link IncQueryEngine} on which the matcher is created
     * @throws IncQueryException
     *             if the {@link IncQueryEngine} base index is not available
     */
    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, IncQueryEngine engine) {
        this(querySpecification);
        ruleEngine = ObservableCollectionHelper.prepareRuleEngine(engine, specification, null);
    }

    /**
     * Creates an observable view of the match set of the given {@link IQuerySpecification} initialized on the given
     * {@link IncQueryEngine}. The given converter function is used on each match and the end result is put into the
     * view. The given comparator is used to define the ordering between the elements in the list. The additional filter
     * parameter can be used to filter the match set of the {@link IQuerySpecification}.
     * 
     * @param querySpecification
     *            the {@link IQuerySpecification} used to create a matcher
     * @param engine
     *            the {@link IncQueryEngine} on which the matcher is created
     * @param converter
     *            the {@link Function} that is executed on each match to create the items in the list
     * @param comparator
     *            the {@link Comparator} that is used to define the ordering between the matches
     * @param filter
     *            the partial match to be used as a filter
     * @throws IncQueryException
     *             if the {@link IncQueryEngine} base index is not available
     */
    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, IncQueryEngine engine, Function<Match, Object> converter,
            Comparator<Match> comparator, Match filter) {
        this(querySpecification, converter, comparator);
        matchFilter = Rules.newSingleMatchFilter(filter);
        ruleEngine = ObservableCollectionHelper.prepareRuleEngine(engine, specification, matchFilter);
    }

    /**
     * Creates an observable view of the match set of the given {@link IQuerySpecification} initialized on the given
     * {@link IncQueryEngine}.
     * 
     * <p>
     * Consider using {@link IncQueryObservables#observeMatchesAsList} instead!
     * 
     * @param querySpecification
     *            the {@link IQuerySpecification} used to create a matcher
     * @param engine
     *            the {@link IncQueryEngine} on which the matcher is created
     * @param filter
     *            the partial match to be used as filter
     * @throws IncQueryException
     *             if the {@link IncQueryEngine} base index is not available
     */
    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, IncQueryEngine engine, Match filter) {
        this(querySpecification);
        matchFilter = Rules.newSingleMatchFilter(filter);
        ruleEngine = ObservableCollectionHelper.prepareRuleEngine(engine, specification, matchFilter);
    }

    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, IncQueryEngine engine, Collection<Match> multifilters,
            IncQueryFilterSemantics semantics) {
        this(querySpecification);
        matchFilter = Rules.newMultiMatchFilter(multifilters, semantics);
        ruleEngine = ObservableCollectionHelper.prepareRuleEngine(engine, specification, matchFilter);
    }

    /**
     * Creates an observable view of the match set of the given {@link IQuerySpecification} initialized on the given
     * {@link RuleEngine}.
     * 
     * <p>
     * Consider using {@link IncQueryObservables#observeMatchesAsList} instead!
     * 
     * @param querySpecification
     *            the {@link IQuerySpecification} used to create a matcher
     * @param engine
     *            an existing {@link RuleEngine} that specifies the used model
     */
    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, RuleEngine engine) {
        this(querySpecification);
        ruleEngine = engine;
        engine.addRule(specification);
        ObservableCollectionHelper.fireActivations(engine, specification, specification.createEmptyFilter());
    }

    /**
     * Creates an observable view of the match set of the given {@link IQuerySpecification} initialized on the given
     * {@link RuleEngine}.
     * 
     * <p>
     * Consider using {@link IncQueryObservables#observeMatchesAsList} instead!
     * 
     * @param querySpecification
     *            the {@link IQuerySpecification} used to create a matcher
     * @param engine
     *            an existing {@link RuleEngine} that specifies the used model
     * @param filter
     *            the partial match to be used as filter
     */
    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, RuleEngine engine, Match filter) {
        this(querySpecification);
        ruleEngine = engine;
        matchFilter = Rules.newSingleMatchFilter(filter);
        engine.addRule(specification, matchFilter);
        ObservableCollectionHelper.fireActivations(engine, specification, matchFilter);
    }

    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, RuleEngine engine, Collection<Match> multifilter,
            IncQueryFilterSemantics semantics) {
        this(querySpecification);
        ruleEngine = engine;
        matchFilter = Rules.newMultiMatchFilter(multifilter, semantics);
        engine.addRule(specification, matchFilter);
        ObservableCollectionHelper.fireActivations(engine, specification, matchFilter);
    }

    protected <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification, Function<Match, Object> converter,
            Comparator<Match> comparator) {
        super();
        updater = new ListCollectionUpdate(converter, comparator);
        if(comparator != null) {
            this.specification = ObservableCollectionHelper.createUpdatingRuleSpecification(updater, querySpecification);
        } else {
            this.specification = ObservableCollectionHelper.createRuleSpecification(updater, querySpecification);
        }
    }

    protected <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(
            IQuerySpecification<Matcher> querySpecification) {
        super();
        updater = new ListCollectionUpdate(null, null);
        this.specification = ObservableCollectionHelper.createRuleSpecification(updater, querySpecification);
    }

    /**
     * Creates an observable view of the match set of the given {@link IncQueryMatcher}.
     * 
     * <p>
     * Consider using {@link IncQueryObservables#observeMatchesAsList} instead!
     * 
     * @param matcher
     *            the {@link IncQueryMatcher} to use as the source of the observable list
     */
    public <Matcher extends IncQueryMatcher<Match>> ObservablePatternMatchList(Matcher matcher) {
        super();
        updater = new ListCollectionUpdate(null, null);
        this.specification = ObservableCollectionHelper.createRuleSpecification(updater, matcher);
        ObservableCollectionHelper.prepareRuleEngine(matcher.getEngine(), specification, null);
    }

    @Override
    public Object getElementType() {
        if (updater.converter != null) {
            return Object.class;
        }
        return IPatternMatch.class;
    }

    @Override
    protected int doGetSize() {
        return cache.size();
    }

    @Override
    public Object get(int index) {
        if (updater.converter != null) {
            return updater.matchToItem.get(cache.get(index));
        }
        return cache.get(index);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator iterator() {
        if (updater.converter != null) {
            final Iterator<Match> iterator = cache.iterator();
            return new Iterator<Object>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Object next() {
                    Match next = iterator.next();
                    return updater.matchToItem.get(next);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("This iterator does not support element removal!");
                }

            };
        }
        return cache.iterator();
    }

    /**
     * @return the specification
     */
    public RuleSpecification<Match> getSpecification() {
        return specification;
    }
    
    public void setFilter(Match filter) {
        
        EventFilter<Match> oldFilter = matchFilter;
        matchFilter = Rules.newSingleMatchFilter(filter);
        if(Objects.equal(matchFilter, oldFilter)) {
            return; // same filter, do nothing
        }
        
        if(oldFilter == null) {
            ruleEngine.removeRule(specification);
        } else {
            ruleEngine.removeRule(specification, oldFilter);
        }
        
        List<Match> oldCache = updater.pauseUpdates();
        // delay removal to maintain list order
        updater.removed.addAll(oldCache);
        if(filter == null) {
            ruleEngine.addRule(specification);
        } else {
            ruleEngine.addRule(specification, matchFilter);
        }
        // new items are added back through updater
        ObservableCollectionHelper.fireActivations(ruleEngine, specification, matchFilter);
        // send combined notification
        updater.resumeUpdates();
        
    }

    public class ListCollectionUpdate implements IObservablePatternMatchCollectionUpdate<Match> {

        protected static final String DATA_BINDING_REALM_MUST_NOT_BE_NULL = "Data binding Realm must not be null";
        protected final Function<Match, Object> converter;
        protected final Comparator<Match> comparator;
        protected final Map<Match, Object> matchToItem;
        protected ListDiff nextDiff = null;
        private List<Match> oldCache = null;
        private Set<Match> removed;

        public ListCollectionUpdate(Function<Match, Object> converter, Comparator<Match> comparator) {
            if (converter != null) {
                this.converter = converter;
                matchToItem = new HashMap<Match, Object>();
            } else {
                this.converter = null;
                matchToItem = null;
            }
            this.comparator = comparator;
        }

        private int placeOf(Match match) {
            if (cache.size() == 0) {
                return 0;
            }
            // tradeoff between the ArrayList get(int) and add(int, E) vs LinkedList get(int) and add(int, E)
            // stick to LinkedList and binary search so get(int) will be invoked not that many times (log2) and add(int,
            // E) is more efficient
            int left = 0, right = cache.size() - 1;
            while (left <= right) {
                int mid = (left + right) >> 1;
                int cv = comparator.compare(match, cache.get(mid));
                if (cv == 0) {
                    return mid;
                }
                if (cv < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return left;
        }

        @Override
        public void addMatch(Match match) {
            ListDiffEntry diffentry = addItem(match);
            sendListUpdates(diffentry);
        }

        @Override
        public void removeMatch(Match match) {
            ListDiffEntry diffentry = removeItem(match);
            sendListUpdates(diffentry);
        }

        private void sendListUpdates(ListDiffEntry diffentry) {
            if(nextDiff == null) {
                final ListDiff diff = Diffs.createListDiff(diffentry);
                sendListUpdate(diff);
            }
        }

        private void sendListUpdate(final ListDiff diff) {
            Realm realm = getRealm();
            Assert.isNotNull(realm, DATA_BINDING_REALM_MUST_NOT_BE_NULL);
            realm.exec(new Runnable() {
        
                @Override
                public void run() {
                    if (!isDisposed()) {
                        fireListChange(diff);
                    }
                }
            });
        }

        private ListDiffEntry addItem(Match match) {
            if(removed != null && removed.remove(match)) {
                // item remains in list
                return null;
            } else {
                Object item = match;
                if (converter != null) {
                    item = converter.apply(match);
                    matchToItem.put(match, item);
                }
                final int index = (comparator == null ? cache.size() : placeOf(match));
                ListDiffEntry diffentry = Diffs.createListDiffEntry(index, true, item);
                cache.add(index, match);
                return diffentry;
            }
        }

        private ListDiffEntry removeItem(Match match) {
            Object item = match;
            if (converter != null) {
                item = matchToItem.remove(match);
            }
            final int index = cache.indexOf(match);
            ListDiffEntry diffentry = Diffs.createListDiffEntry(index, false, item);
            cache.remove(match);
            return diffentry;
        }
        
        private List<Match> pauseUpdates() {
            if(nextDiff == null) {
                oldCache = Lists.newArrayList(cache);
                nextDiff = Diffs.computeLazyListDiff(oldCache,cache);
                removed = Sets.newHashSet();
                return oldCache;
            }
            return null;
        }
        
        private void resumeUpdates() {
            if(nextDiff != null) {
                List<ListDiffEntry> entries = Lists.newArrayListWithCapacity(removed.size());
                for (Match match : removed) {
                    // delayed removal of items
                    ListDiffEntry diffEntry = removeItem(match);
                    entries.add(diffEntry);
                }
                // convert lazy diff to real diff
                nextDiff = Diffs.createListDiff(nextDiff.getDifferences());
                if(!nextDiff.isEmpty()) {
                    for (ListDiffEntry entry : nextDiff.getDifferences()) {
                        // removals already processed
                        if(entry.isAddition()) {
                            // items already in cache at the proper place
                            int index = entry.getPosition();
                            Object item = get(index);
                            ListDiffEntry diffentry = Diffs.createListDiffEntry(index, true, item);
                            entries.add(diffentry);
                        }
                    }
                    ListDiff diff = Diffs.createListDiff(entries.toArray(new ListDiffEntry[entries.size()]));
                    sendListUpdate(diff);
                }
            }
            nextDiff = null; 
            oldCache = null;
            removed = null;
        }
    }
}
