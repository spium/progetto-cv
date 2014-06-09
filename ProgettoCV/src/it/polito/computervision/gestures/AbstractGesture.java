package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements the common functionality of a gesture (getters and {@link GestureState} update).
 * Concrete implementations will need only to override the {@link #doUpdateState(Collection, Collection)} abstract method.
 * @author giovanni
 *
 */
public abstract class AbstractGesture implements Gesture {

	private String name;
	protected GestureState currentState;
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
	 * Implementations should place their reset logic here
	 */
	protected abstract void doReset();
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final void reset() {
		doReset();
		currentState = GestureState.NOT_DETECTED;
	}
	
	protected List<HandData> getTouchingHands(Collection<HandData> hands) {
		ArrayList<HandData> touchingHands = new ArrayList<HandData>();
		for(HandData hd : hands)
			if(hd.isTouching())
				touchingHands.add(hd);
		
		return touchingHands;
	}
}