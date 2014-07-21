package it.polito.computervision.gestures.impl;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.gestures.TwoHandGesture;
import it.polito.computervision.virtualscreen.HandData;

/**
 * Implements a zoom gesture. Can be configured with allowed directions, thresholds and whether it is live or not.
 * Custom data:
 * 	-initialDistance: float (the initial distance between the two hands, in mm, when both have touched the screen)
 * 	-currentDistance: float (the current distance between the two hands, in mm)
 * 
 * @author Giovanni Piumatti
 *
 */
public class ZoomGesture extends TwoHandGesture {

	public enum Direction { INWARD, OUTWARD, BOTH }

	public static final float DETECTION_THRESHOLD = 35.f;
	public static final float COMPLETION_THRESHOLD = 300.f;

	private float detectionThreshold, initialDistance, completionThreshold;
	private Direction direction;

	/**
	 * Creates a ZoomGesture with the given name, both directions allowed, default thresholds, live (zoom).
	 * @param name The name of this {@link Gesture}.
	 */
	public ZoomGesture(String name) {
		this(name, Direction.BOTH, true);
	}

	/**
	 * Creates a zoom gesture with the given name, direction, live or not, using default thresholds.
	 * @param name The name of the gesture
	 * @param direction The allowed direction
	 * @param live Whether it is a live gesture or not
	 */
	public ZoomGesture(String name, Direction direction, boolean live) {
		this(name, direction, DETECTION_THRESHOLD, COMPLETION_THRESHOLD, live);
	}
	
	/**
	 * Creates a completely configurable zoom gesture.
	 * @param name The name of the gesture
	 * @param direction The allowed directions
	 * @param detectionThreshold The difference (in mm in real world coordinates) between initial distance and current distance of the two hands in order to trigger detection
	 * @param completionThreshold The difference (in mm in real world coordinates) between initial distance and current distance of the two hands in order to trigger completion
	 * @param live Whether it is a live gesture or not
	 */
	public ZoomGesture(String name, Direction direction, float detectionThreshold, float completionThreshold, boolean live) {
		super(name, live);
		if(detectionThreshold <= 0)
			throw new IllegalArgumentException("detectionThreshold <= 0");
		if(completionThreshold <= 0 || completionThreshold < detectionThreshold)
			throw new IllegalArgumentException("completionThreshold <= 0 or completionThreshold < detectionThreshold");

		this.direction = direction;
		this.detectionThreshold = detectionThreshold;
		initialDistance = -1;
		this.completionThreshold = completionThreshold;
	}

	@Override
	protected void doReset() {
		super.doReset();
		initialDistance = -1;
		data.remove("initialDistance");
		data.remove("currentDistance");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState doUpdateState(HandData[] hands, boolean touchReleased) {
		Mat[] handPoints = new Mat[2];
		switch(currentState) {
		case NOT_DETECTED:
			if(hands != null) {
				for(int i = 0; i < 2; ++i)
					handPoints[i] = new MatOfFloat(hands[i].getPosition().getX(), hands[i].getPosition().getY());
				
				Core.subtract(handPoints[0], handPoints[1], handPoints[0]);
				initialDistance = (float) Core.norm(handPoints[0]);
				return GestureState.POSSIBLE_DETECTION;
			}
			else
				return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			if(hands != null) {
				for(int i = 0; i < 2; ++i) {
					if(!hands[i].isTouching()) return GestureState.NOT_DETECTED;
					handPoints[i] = new MatOfFloat(hands[i].getPosition().getX(), hands[i].getPosition().getY());
				}
				
				Core.subtract(handPoints[0], handPoints[1], handPoints[0]);
				float currDistance = (float) Core.norm(handPoints[0]);
				float diff = currDistance - initialDistance;
				if(Math.abs(diff) >= detectionThreshold) {
					if(direction == Direction.BOTH || (direction == Direction.OUTWARD && diff > 0) || (direction == Direction.INWARD && diff < 0)) {
						data.put("initialDistance", initialDistance);
						return GestureState.IN_PROGRESS;
					}
					else {
						return GestureState.NOT_DETECTED;
					}
				}
				else {
					return GestureState.POSSIBLE_DETECTION;
				}
			}
			else
				return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			if(hands != null) {
				for(int i = 0; i < 2; ++i)
					if(!hands[i].isTouching())
						return isLive() ? GestureState.COMPLETED : GestureState.NOT_DETECTED;
				
				//check if we reached the threshold
				for(int i = 0; i < 2; ++i)
					handPoints[i] = new MatOfFloat(hands[i].getPosition().getX(), hands[i].getPosition().getY());
				
				Core.subtract(handPoints[0], handPoints[1], handPoints[0]);
				float currDistance = (float) Core.norm(handPoints[0]);
				
				data.put("currentDistance", currDistance);
				if(isLive()) return GestureState.IN_PROGRESS;
				
				float diff = currDistance - initialDistance;
				
				if(Math.abs(diff) >= completionThreshold) {
					if(direction == Direction.BOTH || (direction == Direction.OUTWARD && diff > 0) || (direction == Direction.INWARD && diff < 0)) {
						return GestureState.COMPLETED;
					}
					else {
						return GestureState.NOT_DETECTED;
					}
				}
				else {
					return GestureState.IN_PROGRESS;
				}
			}
			else if(isLive())
				return GestureState.COMPLETED;	//even if touchReleased == false consider it complete
			else
				return GestureState.NOT_DETECTED;

		case COMPLETED:
			if(hands == null || (!hands[0].isTouching() && !hands[1].isTouching()))
				return GestureState.NOT_DETECTED;
			else
				return GestureState.COMPLETED;
			
			
		default:	//unknown state...

			return GestureState.NOT_DETECTED;
		}
	}
}
