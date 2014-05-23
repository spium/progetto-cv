package it.polito.computervision.gestures;

/**
 * This interface is implemented by classes who want to be updated about a gesture's current state at each frame.
 * @author giovanni
 *
 */
public interface GestureListener {
	/**
	 * Invoked when the gesture has just started (i.e. it passed from state POSSIBLE_DETECTION to state IN_PROGRESS)
	 * @param gesture The data associated to the gesture
	 */
	public void onGestureStarted(GestureData gesture);
	/**
	 * Invoked on each frame while the gesture is in state IN_PROGRESS
	 * @param gesture The data associated to the gesture
	 */
	public void onGestureInProgress(GestureData gesture);
	/**
	 * Invoked when the gesture has completed (i.e. it passed from state IN_PROGESS to state COMPLETED)
	 * @param gesture The data associated to the gesture
	 */
	public void onGestureCompleted(GestureData gesture);
}
