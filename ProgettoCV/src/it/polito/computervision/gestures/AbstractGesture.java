package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.Collection;

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
	
	/**
	 * Creates a new gesture
	 * @param name The unique name of the gesture
	 * @param live Whether this is a live gesture or not
	 */
	public AbstractGesture(String name, boolean live) {
		this.name = name;
		this.live = live;
		currentState = GestureState.NOT_DETECTED;
	}
	
	@Override
	public GestureState updateState(Collection<HandData> hands, Collection<HandData> gestureHands) {
		return (currentState = doUpdateState(hands, gestureHands));
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
	
	/**
	 * Concrete gestures must implement this method. The semantics are the same of {@link Gesture#updateState(Collection, Collection)}.
	 * @param hands The {@link HandData} being tracked during this frame
	 * @param gestureHands The {@link HandData} this gesture is tracking
	 * @return The {@link GestureState} this gesture is in after the update.
	 */
	protected abstract GestureState doUpdateState(Collection<HandData> hands, Collection<HandData> gestureHands);
}
