package it.polito.computervision.gestures.impl;

import java.util.Collection;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.AbstractGesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreen;

/**
 * Detects a click with one hand. If multiple hands are being detected, the first one that is found touching the {@link VirtualScreen} will
 * be the one tracked for a click.
 * @author giovanni
 *
 */
public class ClickGesture extends AbstractGesture {

	private short handId;

	/**
	 * Creates a ClickGesture named "click"
	 */
	public ClickGesture() {
		this("click");
	}
	
	/**
	 * Creates a ClickGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public ClickGesture(String name) {
		super(name, false);
		handId = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState doUpdateState(Collection<HandData> hands, Collection<HandData> gestureHands) {
		if(handId >= 0) {
			for(HandData hd : hands) {
				if(hd.getId() == handId) {
					gestureHands.add(hd);
					break;
				}
			}
		}
		
		switch(getCurrentState()) {
		case NOT_DETECTED:
			for(HandData hd : hands) {
				if(hd.isTouching()) {
					handId = hd.getId();
					return GestureState.POSSIBLE_DETECTION;
				}
			}

			return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			for(HandData hd : hands) {
				if(hd.getId() == handId) {
					return hd.isTouching() ? GestureState.POSSIBLE_DETECTION : GestureState.IN_PROGRESS;
				}
			}

			handId = -1;
			return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			return GestureState.COMPLETED;

		default:	//COMPLETED or unknown state...
			handId = -1;
			return GestureState.NOT_DETECTED;
		}
	}

}
