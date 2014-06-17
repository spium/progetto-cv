package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.List;

public abstract class TwoHandGesture extends AbstractGesture {

	private short[] handIds;
	private HandData[] lastKnownHands;

	public TwoHandGesture(String name, boolean live) {
		super(name, live);
		handIds = null;
		lastKnownHands = null;
	}

	private boolean trackingBothHands(List<HandData> hands) {
		if(handIds != null && hands.size() == 2) {
			short id0 = hands.get(0).getId(), id1 = hands.get(1).getId();
			if((id0 == handIds[0] && id1 == handIds[1]) || (id0 == handIds[1] && id1 == handIds[0]))
				return true;
		}

		return false;
	}

	@Override
	public GestureState updateState(List<HandData> hands, List<HandData> gestureHands) {
		HandData[] currentlyTrackedHands = null;
		List<HandData> touching = getTouchingHands(hands);
		boolean touchReleased = false;
		if(touching.size() == 2) {
			if(handIds != null) {
				//we were tracking 2 hands, check if they're both there
				//two hands touching, but at least one is new, something odd is happening, just reset
				if(trackingBothHands(touching)) {
					currentlyTrackedHands = new HandData[2];
					currentlyTrackedHands[0] = touching.get(0);
					currentlyTrackedHands[1] = touching.get(1);
				}
				else
					reset();
			}
			else {
				//we were not tracking, take the 2 hands that are touching now
				handIds = new short[2];
				currentlyTrackedHands = new HandData[2];
				for(int i = 0; i < 2; ++i) {
					currentlyTrackedHands[i] = touching.get(i);
					handIds[i] = currentlyTrackedHands[i].getId();
				}
			}
		}
		else {
			if(handIds != null && touching.size() < 2) {
				//we were tracking two hands, now they're not touching anymore, see if we find them
				if(trackingBothHands(hands)) {
					//if we are still tracking the two hands, send the touchReleased
					touchReleased = true;
					currentlyTrackedHands = new HandData[2];
					currentlyTrackedHands[0] = hands.get(0);
					currentlyTrackedHands[1] = hands.get(1);
				}
				else {
					//we lost tracking on at least a hand
					reset();
				}

			}
			else if(touching.size() > 2)
				reset();

		}

		if(currentlyTrackedHands != null) {
			lastKnownHands = currentlyTrackedHands;
		}
		
		if(lastKnownHands != null) {
			gestureHands.add(lastKnownHands[0]);
			gestureHands.add(lastKnownHands[1]);
		}

		currentState = doUpdateState(currentlyTrackedHands, touchReleased);
		if(currentState == GestureState.NOT_DETECTED)
			doReset();

		return currentState;
	}

	/**
	 * Concrete gestures must implement this method. The semantics are the same of {@link Gesture#updateState(List, List)}.
	 * @param currentlyTrackedHands The {@link HandData} currently touching the virtual screen or null if not exactly 2 hands are touching the screen
	 * @return The {@link GestureState} this gesture is in after the update.
	 */
	protected abstract GestureState doUpdateState(HandData[] currentlyTrackedHands, boolean touchReleased);

	@Override
	protected void doReset() {
		handIds = null;
		lastKnownHands = null;
	}

}
