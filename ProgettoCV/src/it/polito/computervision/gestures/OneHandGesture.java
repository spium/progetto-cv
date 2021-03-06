package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.List;

/**
 * Implements common functionality of one-handed gestures.
 * One hand gestures work only if there is exactly one hand touching the virtual screen.
 * @author giovanni
 *
 */
public abstract class OneHandGesture extends AbstractGesture {

	private short handId;
	private HandData lastKnownHand;

	public OneHandGesture(String name, boolean live) {
		super(name, live);
		handId = -1;
		lastKnownHand = null;
	}
	
	@Override
	protected void doReset() {
		handId = -1;
		lastKnownHand = null;
	}


	@Override
	public GestureState updateState(List<HandData> hands, List<HandData> gestureHands) {
		List<HandData> touching = getTouchingHands(hands);
		boolean touchReleased = false;
		HandData currentlyTrackedHand = null;
		if(touching.size() == 1) {
			HandData hd = touching.get(0);
			
			if(handId >= 0 && hd.getId() == handId) {
				//we are tracking the only hand
				currentlyTrackedHand = hd;
			}
			else if(handId < 0) {
				//currently not tracking any hand, track the only hand touching the screen
				handId = hd.getId();
				currentlyTrackedHand = hd;
			}
			else {
				//we are here because we had an ID but it's not the one touching anymore -> reset
				//at the next frame we'll track it if it's still there
				reset();
			}

		}
		else {
			//if we were tracking a hand and now it's not touching anymore
			if(handId >= 0 && touching.isEmpty()) {				
				//check if we find the hand we were tracking
				for(HandData hd : hands)
					if(hd.getId() == handId) {
						currentlyTrackedHand = hd;
						touchReleased = true;
					}

				//otherwise it was lost
			}
		}
		
		if(currentlyTrackedHand != null) {
			lastKnownHand = currentlyTrackedHand;
		}
		
		if(lastKnownHand != null)
			gestureHands.add(lastKnownHand);

		currentState = doUpdateState(currentlyTrackedHand, touchReleased);
		//let the implementation decide when to reset
		if(currentState == GestureState.NOT_DETECTED)
			doReset();

		return currentState;
	}


	/**
	 * Concrete gestures must implement this method. The semantics are the same of {@link Gesture#updateState(List,List)}.
	 * When a hand touches the screen, that becomes the currently tracked hand, and is passed to this method until either a {@link #reset()} is
	 * called or the hand is lost (not tracked anymore).
	 * @param currentlyTrackedHand The {@link HandData} currently being tracked for this gesture, or null if no hand is being tracked yet
	 * @param touchReleased true if the touch was released, false otherwise (i.e. there are more than 1 hand touching)
	 * @return The {@link GestureState} this gesture is in after the update.
	 */
	protected abstract GestureState doUpdateState(HandData currentlyTrackedHand, boolean touchReleased);

}
