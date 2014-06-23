package it.polito.computervision.gestures.impl;

import org.openni.Point2D;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.gestures.OneHandGesture;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreen;

/**
 * Detects a click with one hand. If multiple hands are being detected, the first one that is found touching the {@link VirtualScreen} will
 * be the one tracked for a click.
 * @author giovanni
 *
 */
public class ClickGesture extends OneHandGesture {

	private Point2D<Float> initialPosition;
	
	/**
	 * Creates a ClickGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public ClickGesture(String name) {
		super(name, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState doUpdateState(HandData currentlyTrackedHand, boolean touchReleased) {
		switch(currentState) {
		case NOT_DETECTED:
			if(currentlyTrackedHand != null) {
				initialPosition = currentlyTrackedHand.getProjectedPosition();
				return GestureState.POSSIBLE_DETECTION;
			}
			else
				return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			if(currentlyTrackedHand != null && currentlyTrackedHand.isTouching()) { 
				return GestureState.POSSIBLE_DETECTION;
			}
			else if(touchReleased) {
				data.put("initialPosition", initialPosition);
				return GestureState.IN_PROGRESS;
			}
			else
				return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			return GestureState.COMPLETED;

		default:	//COMPLETED or unknown state...
			return GestureState.NOT_DETECTED;
		}
	}
	
	@Override
	protected void doReset() {
		super.doReset();
		initialPosition = null;
		data.remove("initialPosition");
	}

}
