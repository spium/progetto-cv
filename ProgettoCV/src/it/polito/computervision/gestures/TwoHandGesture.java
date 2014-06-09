package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.Collection;
import java.util.List;

public abstract class TwoHandGesture extends AbstractGesture {

	private short[] handIds;
	private HandData[] currentlyTrackedHands;

	public TwoHandGesture(String name, boolean live) {
		super(name, live);
		handIds = null;
		currentlyTrackedHands = null;
	}

	private boolean trackingBothHands(Collection<HandData> hands) {
		if(handIds != null) {
			short tracking = 0;
			for(int i = 0; i < 2; ++i) {
				for(HandData hd : hands) {
					if(hd.getId() == handIds[i]) {
						++tracking;
						currentlyTrackedHands[i] = hd;
						break;
					}
				}
			}
			
			if(tracking == 2) return true;
		}
		
		return false;
	}

	@Override
	public GestureState updateState(Collection<HandData> hands, Collection<HandData> gestureHands) {
		List<HandData> touching = getTouchingHands(hands);
		boolean touchReleased = false;
		if(touching.size() == 2) {
			if(handIds != null) {
				//we were tracking 2 hands, check if they're both there
				//two hands touching, but at least one is new, something odd is happening, just reset
				if(!trackingBothHands(touching))
					doReset();
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
				}
				else {
					//we lost tracking on at least a hand
					doReset();
				}

			}
			else if(touching.size() > 2)
				doReset();

		}

		if(currentlyTrackedHands != null) {
			gestureHands.add(currentlyTrackedHands[0]);
			gestureHands.add(currentlyTrackedHands[1]);
		}

		currentState = doUpdateState(currentlyTrackedHands, touchReleased);
		if(currentState == GestureState.NOT_DETECTED)
			doReset();

		return currentState;
	}

	/**
	 * Concrete gestures must implement this method. The semantics are the same of {@link Gesture#updateState(Collection, Collection)}.
	 * @param currentlyTrackedHands The {@link HandData} currently touching the virtual screen or null if not exactly 2 hands are touching the screen
	 * @return The {@link GestureState} this gesture is in after the update.
	 */
	protected abstract GestureState doUpdateState(HandData[] currentlyTrackedHands, boolean touchReleased);

	@Override
	protected void doReset() {
		handIds = null;
		currentlyTrackedHands = null;
	}

}