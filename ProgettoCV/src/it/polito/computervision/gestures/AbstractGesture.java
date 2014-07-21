package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreen;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the common functionality of a gesture (getters and {@link GestureState} update).
 * Concrete implementations will need to implement the {@link #updateState(List, List)} and {@link #doReset()} methods.
 * @author Giovanni Piumatti
 *
 */
public abstract class AbstractGesture implements Gesture {

	private String name;
	protected GestureState currentState;
	private boolean live;
	protected Map<String, Object> data;
	
	/**
	 * Creates a new gesture
	 * @param name The unique name of the gesture
	 * @param live Whether this is a live gesture or not
	 */
	public AbstractGesture(String name, boolean live) {
		this.name = name;
		this.live = live;
		data = new HashMap<String, Object>();
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
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getData() {
		return Collections.unmodifiableMap(data);
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
	
	/**
	 * Convenience method for obtaining a list of all hands that are currently touching the {@link VirtualScreen}.
	 * @param hands The collection of {@link HandData} to check.
	 * @return a list of {@link HandData} of only the hands touching the screen.
	 */
	protected List<HandData> getTouchingHands(Collection<HandData> hands) {
		ArrayList<HandData> touchingHands = new ArrayList<HandData>();
		for(HandData hd : hands)
			if(hd.isTouching())
				touchingHands.add(hd);
		
		return touchingHands;
	}
}