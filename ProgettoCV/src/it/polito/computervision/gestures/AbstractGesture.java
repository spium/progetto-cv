package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.Collection;
import java.util.HashSet;

/**
 * Implements the common functionality of a gesture. Namely: listener management and notification, getters.
 * In particular, this class will notify {@link GestureListener}s based on the return value of the {@link #doUpdateState(Collection)} abstract method.
 * Concrete implementations will need only to override the {@link #doUpdateState(Collection)} abstract method.
 * @author giovanni
 *
 */
public abstract class AbstractGesture implements Gesture {

	private String name;
	private GestureState currentState;
	private boolean live;
	private Collection<GestureListener> listeners;
	
	/**
	 * Creates a new gesture
	 * @param name The unique name of the gesture
	 * @param live Whether this is a live gesture or not
	 */
	public AbstractGesture(String name, boolean live) {
		this.name = name;
		this.live = live;
		currentState = GestureState.NOT_DETECTED;
		listeners = new HashSet<GestureListener>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addGestureListener(GestureListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeGestureListener(GestureListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState updateState(Collection<HandData> hands) {
		GestureState oldState = currentState;
		currentState = doUpdateState(hands);
		
		if(currentState == GestureState.IN_PROGRESS) {
			if(oldState == GestureState.IN_PROGRESS)
				notifyGestureInProgress(hands);
			else if(oldState == GestureState.POSSIBLE_DETECTION)
				notifyGestureStarted(hands);
			else
				throw new IllegalStateException("Illegal state transition from: " + oldState + " to: " + currentState);
		}
		else if(currentState == GestureState.COMPLETED) {
			if(oldState == GestureState.IN_PROGRESS)
				notifyGestureCompleted(hands);
			else if(oldState != GestureState.COMPLETED)
				throw new IllegalStateException("Illegal state transition from: " + oldState + " to: " + currentState);
		}
		//else we don't care (no notification when entering other states)

		return currentState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		currentState = GestureState.NOT_DETECTED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState getCurrentState() {
		return currentState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLive() {
		return live;
	}
	
	@Override
	protected void finalize() {
		listeners.clear();
	}
	
	/**
	 * Notifies listeners that the gesture is started (i.e. invokes {@link GestureListener#onGestureStarted(GestureData)})
	 * @param hands The {@link HandData} of the current frame
	 */
	private void notifyGestureStarted(Collection<HandData> hands) {
		GestureData gd = new GestureData(name, currentState, hands, live);
		for(GestureListener l : listeners) {
			l.onGestureStarted(gd);
		}
	}
	
	/**
	 * Notifies listeners that the gesture is in progress (i.e. invokes {@link GestureListener#onGestureInProgress(GestureData)})
	 * @param hands The {@link HandData} of the current frame
	 */
	private void notifyGestureInProgress(Collection<HandData> hands) {
		GestureData gd = new GestureData(name, currentState, hands, live);
		for(GestureListener l : listeners) {
			l.onGestureInProgress(gd);
		}
	}
	
	/**
	 * Notifies listeners that the gesture has completed (i.e. invokes {@link GestureListener#onGestureCompleted(GestureData)})
	 * @param hands The {@link HandData} of the current frame
	 */
	private void notifyGestureCompleted(Collection<HandData> hands) {
		GestureData gd = new GestureData(name, currentState, hands, live);
		for(GestureListener l : listeners) {
			l.onGestureCompleted(gd);
		}
	}

	/**
	 * Determines which {@link GestureState} should this gesture move to
	 * @param hands The {@link HandData} of the current frame
	 * @return The {@link GestureState} this gesture should move to after the call.
	 */
	protected abstract GestureState doUpdateState(Collection<HandData> hands);
}
