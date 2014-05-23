package it.polito.computervision.gestures;

/**
 * Represents the states a gesture could be in during a certain frame
 * @author giovanni
 *
 */
public enum GestureState {
	/**
	 * The gesture has not been detected yet
	 */
	NOT_DETECTED,
	/**
	 * The gesture might possibly be started, but there is not enough information to decide with certainty
	 */
	POSSIBLE_DETECTION,
	/**
	 * The gesture has been detected and is still in progress
	 */
	IN_PROGRESS,
	/**
	 * The gesture has completed
	 */
	COMPLETED
}
