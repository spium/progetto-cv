package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.Collection;

/**
 * Represents a 2D gesture.
 * @author giovanni
 *
 */
public interface Gesture {
	/**
	 * Adds a new {@link GestureListener}
	 * @param listener The listener to add
	 */
	public void addGestureListener(GestureListener listener);
	/**
	 * Removes the {@link GestureListener}
	 * @param listener The listener to remove
	 */
	public void removeGestureListener(GestureListener listener);
	/**
	 * Updates the state of the gesture
	 * @param hands The set of {@link HandData} of the new frame
	 * @return the {@link GestureState} this gesture is in after the call
	 */
	public GestureState updateState(Collection<HandData> hands);
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
	 * Resets this gesture's {@link GestureState} to NOT_DETECTED
	 */
	public void reset();
}
