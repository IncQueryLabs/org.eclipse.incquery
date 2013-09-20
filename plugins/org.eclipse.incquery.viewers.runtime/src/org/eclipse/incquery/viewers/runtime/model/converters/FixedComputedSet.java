/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   abelhegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.viewers.runtime.model.converters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.ComputedSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetDiff;

/**
 * This code is a verbatim copy of {@link ComputedSet}, 
 * with the patch from https://bugs.eclipse.org/bugs/show_bug.cgi?id=414297
 * @author abelhegedus
 *
 */
public abstract class FixedComputedSet extends AbstractObservableSet {
	private Set cachedSet = new HashSet();

	private boolean dirty = true;
	private boolean stale = false;

	private IObservable[] dependencies = new IObservable[0];

	/**
	 * Creates a computed set in the default realm and with an unknown (null)
	 * element type.
	 */
	public FixedComputedSet() {
		this(Realm.getDefault(), null);
	}

	/**
	 * Creates a computed set in the default realm and with the given element
	 * type.
	 * 
	 * @param elementType
	 *            the element type, may be <code>null</code> to indicate unknown
	 *            element type
	 */
	public FixedComputedSet(Object elementType) {
		this(Realm.getDefault(), elementType);
	}

	/**
	 * Creates a computed set in given realm and with an unknown (null) element
	 * type.
	 * 
	 * @param realm
	 *            the realm
	 * 
	 */
	public FixedComputedSet(Realm realm) {
		this(realm, null);
	}

	/**
	 * Creates a computed set in the given realm and with the given element
	 * type.
	 * 
	 * @param realm
	 *            the realm
	 * @param elementType
	 *            the element type, may be <code>null</code> to indicate unknown
	 *            element type
	 */
	public FixedComputedSet(Realm realm, Object elementType) {
		super(realm);
		this.elementType = elementType;
	}

	/**
	 * Inner class that implements interfaces that we don't want to expose as
	 * public API. Each interface could have been implemented using a separate
	 * anonymous class, but we combine them here to reduce the memory overhead
	 * and number of classes.
	 * 
	 * <p>
	 * The Runnable calls calculate and stores the result in cachedSet.
	 * </p>
	 * 
	 * <p>
	 * The IChangeListener stores each observable in the dependencies list. This
	 * is registered as the listener when calling ObservableTracker, to detect
	 * every observable that is used by computeValue.
	 * </p>
	 * 
	 * <p>
	 * The IChangeListener is attached to every dependency.
	 * </p>
	 * 
	 */
	private class PrivateInterface implements Runnable, IChangeListener,
			IStaleListener {
		public void run() {
			cachedSet = calculate();
			if (cachedSet == null)
				cachedSet = Collections.EMPTY_SET;
		}

		public void handleStale(StaleEvent event) {
			if (!dirty)
				makeStale();
		}

		public void handleChange(ChangeEvent event) {
			makeDirty();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Object elementType;

	protected int doGetSize() {
		return doGetSet().size();
	}

	private final Set getSet() {
		getterCalled();
		return doGetSet();
	}

	protected Set getWrappedSet() {
		return doGetSet();
	}

	final Set doGetSet() {
		if (dirty) {
			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(
					privateInterface, privateInterface, null);

			// If any dependencies are stale, a stale event will be fired here
			// even if we were already stale before recomputing. This is in case
			// clients assume that a set change is indicative of non-staleness.
			stale = false;
			for (int i = 0; i < newDependencies.length; i++) {
				if (newDependencies[i].isStale()) {
					makeStale();
					break;
				}
			}

			if (!stale) {
				for (int i = 0; i < newDependencies.length; i++) {
					newDependencies[i].addStaleListener(privateInterface);
				}
			}

			dependencies = newDependencies;

			dirty = false;
		}

		return cachedSet;
	}

	/**
	 * Subclasses must override this method to calculate the set contents. Any
	 * dependencies used to calculate the set must be {@link IObservable}, and
	 * implementers must use one of the interface methods tagged TrackedGetter
	 * for FixedComputedSet to recognize it as a dependency.
	 * 
	 * @return the object's set.
	 */
	protected abstract Set calculate();

	private void makeDirty() {
		if (!dirty) {
			dirty = true;

			// copy the old set
			// bug 414297: moved before makeStale(), as cachedSet may be
			// overwritten
			// in makeStale() if a listener calls isStale()
			final Set oldSet = new HashSet(cachedSet);
			makeStale();

			stopListening();

			// copy the old set
			//final Set oldSet = new HashSet(cachedSet);
			// Fire the "dirty" event. This implementation recomputes the new
			// set lazily.
			fireSetChange(new SetDiff() {
				SetDiff delegate;

				private SetDiff getDelegate() {
					if (delegate == null)
						delegate = Diffs.computeSetDiff(oldSet, getSet());
					return delegate;
				}

				public Set getAdditions() {
					return getDelegate().getAdditions();
				}

				public Set getRemovals() {
					return getDelegate().getRemovals();
				}
			});
		}
	}

	private void stopListening() {
		if (dependencies != null) {
			for (int i = 0; i < dependencies.length; i++) {
				IObservable observable = dependencies[i];

				observable.removeChangeListener(privateInterface);
				observable.removeStaleListener(privateInterface);
			}
			dependencies = null;
		}
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	public boolean isStale() {
		// recalculate set if dirty, to ensure staleness is correct.
		getSet();
		return stale;
	}

	public Object getElementType() {
		return elementType;
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	public synchronized void addSetChangeListener(ISetChangeListener listener) {
		super.addSetChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	private void computeSetForListeners() {
		// Some clients just add a listener and expect to get notified even if
		// they never called getValue(), so we have to call getValue() ourselves
		// here to be sure. Need to be careful about realms though, this method
		// can be called outside of our realm.
		// See also bug 198211. If a client calls this outside of our realm,
		// they may receive change notifications before the runnable below has
		// been executed. It is their job to figure out what to do with those
		// notifications.
		getRealm().exec(new Runnable() {
			public void run() {
				if (dependencies == null) {
					// We are not currently listening.
					// But someone is listening for changes. Call getValue()
					// to make sure we start listening to the observables we
					// depend on.
					getSet();
				}
			}
		});
	}

	public synchronized void dispose() {
		stopListening();
		super.dispose();
	}
}
