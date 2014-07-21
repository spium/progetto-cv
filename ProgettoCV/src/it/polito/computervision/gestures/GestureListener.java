package it.polito.computervision.gestures;

/**
 * This interface is implemented by classes who want to be updated about a gesture's current state at each frame.
 * @author Giovanni Piumatti
 *
 */
public interface GestureListener {
	/**
	 * Invoked when the {@link Gesture} has just started (i.e. it passed from state POSSIBLE_DETECTION to state IN_PROGRESS).
	 * This callback is invoked only for live gestures.
	 * @param gesture The data associated to the gesture
	 */
	public void onGestureStarted(GestureData gesture);
	/**
	 * Invoked on each frame while the {@link Gesture} is in state IN_PROGRESS.
	 * This callback is invoked only for live gestures.
	 * @param gesture The data associated to the gesture
	 */
	public void onGestureInProgress(GestureData gesture);
	/**
	 * Invoked when the {@link Gesture} has completed (i.e. it passed from state IN_PROGESS to state COMPLETED).
	 * This callback is always invoked (i.e. both on live and non-live gestures).
	 * @param gesture The data associated to the gesture
	 */
	public void onGestureCompleted(GestureData gesture);
}
