package it.polito.computervision.gestures.impl;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.gestures.OneHandGesture;
import it.polito.computervision.virtualscreen.HandData;

/**
 * 
 * @author giovanni
 *
 */
public class PanGesture extends OneHandGesture {

	public static final float DETECTION_THRESHOLD = 80.f;
	public static final int DELAY_FRAMES = 30;

	private Mat startPoint;
	private float detectionThreshold;
	private int delayFrames, framesDelayed;

	public PanGesture(String name) {
		this(name, DETECTION_THRESHOLD, DELAY_FRAMES);
	}
	/**
	 * Creates a PanGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 * @param detectionThreshold The distance (in mm in real world coordinates) the hand has to travel before triggering detection
	 * @param delayFrames The number of frames to delay detection after the gesture has completed
	 */
	public PanGesture(String name, float detectionThreshold, int delayFrames) {
		super(name, true);
		if(detectionThreshold <= 0 || delayFrames < 0)
			throw new IllegalArgumentException("detectionThreshold <= 0 or delayFrames < 0");

		this.detectionThreshold = detectionThreshold;
		this.delayFrames = delayFrames;
		framesDelayed = 0;
		startPoint = null;
	}

	@Override
	protected void doReset() {
		super.doReset();
		startPoint = null;
		framesDelayed = 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState doUpdateState(HandData currentlyTrackedHand, boolean touchReleased) {
		switch(currentState) {
		case NOT_DETECTED:
				if(currentlyTrackedHand != null) {
					startPoint = new MatOfFloat(currentlyTrackedHand.getPosition().getX(), currentlyTrackedHand.getPosition().getY());
					return GestureState.POSSIBLE_DETECTION;
				}
				else
					return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			if(currentlyTrackedHand != null && currentlyTrackedHand.isTouching()) {
				Mat currPoint = new MatOfFloat(currentlyTrackedHand.getPosition().getX(), currentlyTrackedHand.getPosition().getY());
				Core.subtract(currPoint, startPoint, currPoint);
				return (Core.norm(currPoint) >= detectionThreshold) ? GestureState.IN_PROGRESS : GestureState.POSSIBLE_DETECTION;
			}
			else 
				return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			//we complete the gesture even if we lose the hand (touchReleased == false)
			return (currentlyTrackedHand != null && currentlyTrackedHand.isTouching()) ? GestureState.IN_PROGRESS : GestureState.COMPLETED;

		case COMPLETED:
			if(++framesDelayed == delayFrames) {
				return GestureState.NOT_DETECTED;
			}
			else 
				return GestureState.COMPLETED;

		default:	//unknown state...
			return GestureState.NOT_DETECTED;
		}
	}

}
