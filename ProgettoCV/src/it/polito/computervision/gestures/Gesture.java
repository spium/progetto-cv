package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.List;
import java.util.Map;

/**
 * Represents a 2D gesture.
 * @author Giovanni Piumatti
 *
 */
public interface Gesture {
	/**
	 * Updates the state of the gesture
	 * @param hands The list of {@link HandData} of the new frame
	 * @param gestureHands (out) The list of {@link HandData} this gesture is tracking
	 * @return the {@link GestureState} this gesture is in after the call
	 */
	public GestureState updateState(List<HandData> hands, List<HandData> gestureHands);
	/**
	 * 
	 * @return The {@link GestureState} this gesture is currently in
	 */
	public GestureState getCurrentState();
	/**
	 * 
	 * @return the unique name of this gesture
	 */
	public String getName();
	/**
	 * 
	 * @return true if it's a live gesture, false otherwise
	 */
	public boolean isLive();
	
	/**
	 * 
	 * @return arbitrary data associated to the specific gesture
	 */
	public Map<String, Object> getData();
	
	/**
	 * Resets this gesture's {@link GestureState} to NOT_DETECTED
	 */
	public void reset();
}
