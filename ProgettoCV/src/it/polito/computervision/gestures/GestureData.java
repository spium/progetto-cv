package it.polito.computervision.gestures;

import java.util.Collection;

/**
 * Represents data about a 2D gesture
 * @author giovanni
 *
 */
public class GestureData {
	
	private String name;
	private GestureState state;
	private Collection<HandData> hands;
	private boolean live;
	
	/**
	 * @param name The unique name of this gesture
	 * @param state The state this gesture is in during the current frame
	 * @param hands The hands this gesture is tracking
	 * @param live Whether the gesture is live or not
	 */
	public GestureData(String name, GestureState state, Collection<HandData> hands, boolean live) {
		this.name = name;
		this.state = state;
		this.hands = hands;
		this.live = live;
	}

	/**
	 * @return the unique name of this gesture
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the state this gesture is in during the current frame
	 */
	public GestureState getState() {
		return state;
	}

	/**
	 * @return the hands this gesture is tracking
	 */
	public Collection<HandData> getHands() {
		return hands;
	}

	/**
	 * @return true if the gesture is a live gesture, false otherwise
	 */
	public boolean isLive() {
		return live;
	}

	/**
	 * 
	 * @return true if the gesture is in progress, false otherwise
	 */
	public boolean isInProgress() {
		return state == GestureState.IN_PROGRESS;
	}
	
	/**
	 * 
	 * @return true if the gesture has completed, false otherwise
	 */
	public boolean isComplete() {
		return state == GestureState.COMPLETED;
	}
	

}
