package it.polito.computervision.gestures.impl;

import java.util.Collection;

import it.polito.computervision.gestures.AbstractGesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.virtualscreen.HandData;

public class ClickGesture extends AbstractGesture {

	private short handId;

	public ClickGesture() {
		super("click", false);
		handId = -1;
	}

	@Override
	public GestureState doUpdateState(Collection<HandData> hands, Collection<HandData> gestureHands) {
		switch(getCurrentState()) {
		case NOT_DETECTED:
			for(HandData hd : hands) {
				if(hd.isTouching()) {
					handId = hd.getId();
					gestureHands.add(hd);
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

		case COMPLETED:
			handId = -1;
			return GestureState.NOT_DETECTED;

		default:
			return GestureState.NOT_DETECTED;
		}
	}

}
