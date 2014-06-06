package it.polito.computervision.gestures.impl;

import java.util.Collection;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.AbstractGesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.virtualscreen.HandData;

/**
 * 
 * @author giovanni
 *
 */
public class PanGesture extends AbstractGesture {

	public static final float MIN_DETECTION_DISTANCE = 50.f;
	public static final int DELAY_FRAMES = 30;

	private short handId;
	private Mat startPoint;
	private float minDistance;
	private int delayFrames, framesDelayed;

	public PanGesture() {
		this("pan");
	}

	public PanGesture(String name) {
		this(name, MIN_DETECTION_DISTANCE, DELAY_FRAMES);
	}
	/**
	 * Creates a PanGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 * @param minDistance The minimum distance the hand has to travel before triggering detection
	 * @param delayFrames The number of frames to delay detection after the gesture has completed
	 */
	public PanGesture(String name, float minDistance, int delayFrames) {
		super(name, true);
		if(minDistance <= 0)
			throw new IllegalArgumentException("minDistance <= 0");
		handId = -1;
		this.minDistance = minDistance;
		this.delayFrames = delayFrames;
		framesDelayed = 0;
		startPoint = null;
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
					startPoint = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
					return GestureState.POSSIBLE_DETECTION;
				}
			}

			return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			for(HandData hd : hands) {
				if((hd.getId() == handId)&&(hd.isTouching())) {
					Mat currPoint = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
					Core.subtract(currPoint, startPoint, currPoint);
					return (Core.norm(currPoint) >= minDistance) ? GestureState.IN_PROGRESS : GestureState.POSSIBLE_DETECTION;
				}
			}

			handId = -1;
			startPoint = null;
			return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			for(HandData hd : hands) {
				if((hd.getId() == handId)) {
					if (hd.isTouching()) {
						return GestureState.IN_PROGRESS;
					}
				}
			}

			return GestureState.COMPLETED;

		case COMPLETED:
			if(++framesDelayed == delayFrames) {
				handId = -1;
				startPoint = null;
				framesDelayed = 0;
				return GestureState.NOT_DETECTED;
			}
			
			return GestureState.COMPLETED;

		default:	//COMPLETED or unknown state...
			handId = -1;
			startPoint = null;
			return GestureState.NOT_DETECTED;
		}
	}

}
