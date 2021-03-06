package it.polito.computervision.gestures.impl;

import java.util.EnumSet;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.openni.Point2D;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.gestures.OneHandGesture;
import it.polito.computervision.virtualscreen.HandData;

/**
 * Implements a pan or swipe gesture. Can be configured with allowed directions, thresholds and can be either live or non-live.
 * Custom data:
 * 	- initialPosition: Point2D<Float> (the projected position where the hand touched the screen first)
 * 
 * @author Giovanni Piumatti
 *
 */
public class PanGesture extends OneHandGesture {

	public enum Direction { LEFT, RIGHT, UP, DOWN }
	 
	public static final float DETECTION_THRESHOLD = 100.f;
	public static final float COMPLETION_THRESHOLD = 300.f;
	
	private static final double PI_4 = Math.PI / 4;

	private MatOfFloat startPoint;
	private float detectionThreshold, completionThreshold;
	private EnumSet<Direction> directions;
	private Point2D<Float> initialPosition;

	/**
	 * Creates a pan gesture with the given name, all directions allowed, default thresholds, live (pan).
	 * @param name The name of this gesture
	 */
	public PanGesture(String name) {
		this(name, EnumSet.allOf(Direction.class), DETECTION_THRESHOLD, COMPLETION_THRESHOLD, true);
	}
	
	/**
	 * Creates a pan gesture with the given name, allowed directions, live or not. Uses default thresholds.
	 * @param name The name of the gesture
	 * @param directions The set of allowed directions
	 * @param live Whether this is a live gesture or not
	 */
	public PanGesture(String name, EnumSet<Direction> directions, boolean live) {
		this(name, directions, DETECTION_THRESHOLD, COMPLETION_THRESHOLD, live);
	}
	/**
	 * Creates a PanGesture with the given name, all other options configurable
	 * @param name The name of this {@link Gesture}.
	 * @param directions The directions allowed for this gesture
	 * @param detectionThreshold The distance (in mm in real world coordinates) the hand has to travel before triggering detection
	 * @param completionThreshold The distance (in mm in real world coordinates) the hand has to travel before triggering completion
	 * @param live Whether this is a live gesture or not
	 */
	public PanGesture(String name, EnumSet<Direction> directions, float detectionThreshold, float completionThreshold, boolean live) {
		super(name, live);
		if(detectionThreshold <= 0 || completionThreshold < detectionThreshold)
			throw new IllegalArgumentException("detectionThreshold <= 0 or completionThreshold < detectionThreshold");

		this.detectionThreshold = detectionThreshold;
		this.directions = directions;
		this.completionThreshold = completionThreshold;
		startPoint = null;
		initialPosition = null;
	}

	@Override
	protected void doReset() {
		super.doReset();
		startPoint = null;
		initialPosition = null;
		data.remove("initialPosition");
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
					initialPosition = currentlyTrackedHand.getProjectedPosition();
					return GestureState.POSSIBLE_DETECTION;
				}
				else
					return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			if(currentlyTrackedHand != null && currentlyTrackedHand.isTouching()) {
				MatOfFloat currPoint = new MatOfFloat(currentlyTrackedHand.getPosition().getX(), currentlyTrackedHand.getPosition().getY());
				Mat vector = new Mat();
				Core.subtract(currPoint, startPoint, vector);
				if(Core.norm(vector) >= detectionThreshold) {
					double angleRad = Math.atan2(currPoint.get(1, 0)[0] - startPoint.get(1, 0)[0], startPoint.get(0, 0)[0] - currPoint.get(0, 0)[0]);
					if((directions.contains(Direction.RIGHT) && angleRad <= PI_4 && angleRad >= -PI_4) ||
							(directions.contains(Direction.UP) && angleRad > PI_4 && angleRad < 3*PI_4) ||
							(directions.contains(Direction.LEFT) && (angleRad > 3*PI_4 || angleRad < -3*PI_4)) ||
							(directions.contains(Direction.DOWN) && angleRad >= -3*PI_4 && angleRad <= -PI_4)) {
						
						data.put("initialPosition", initialPosition);
						return GestureState.IN_PROGRESS;
					}
					else
						return GestureState.NOT_DETECTED;
				}
				else
					return GestureState.POSSIBLE_DETECTION;
			}
			else 
				return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			//we complete the gesture even if we lose the hand (touchReleased == false)
			if(currentlyTrackedHand != null && currentlyTrackedHand.isTouching()) {
				if(isLive()) return GestureState.IN_PROGRESS;
				
				MatOfFloat currPoint = new MatOfFloat(currentlyTrackedHand.getPosition().getX(), currentlyTrackedHand.getPosition().getY());
				Mat distance = new Mat();
				Core.subtract(currPoint, startPoint, distance);
				if(Core.norm(distance) >= completionThreshold) {
					double angleRad = Math.atan2(currPoint.get(1, 0)[0] - startPoint.get(1, 0)[0], startPoint.get(0, 0)[0] - currPoint.get(0, 0)[0]);
					if((directions.contains(Direction.RIGHT) && angleRad <= PI_4 && angleRad >= -PI_4) ||
							(directions.contains(Direction.UP) && angleRad > PI_4 && angleRad < 3*PI_4) ||
							(directions.contains(Direction.LEFT) && (angleRad > 3*PI_4 || angleRad < -3*PI_4)) ||
							(directions.contains(Direction.DOWN) && angleRad >= -3*PI_4 && angleRad <= -PI_4)) {
						
						return GestureState.COMPLETED;
					}
					else
						return GestureState.NOT_DETECTED;
				}
				else
					return GestureState.IN_PROGRESS;
				
			}
			else if(isLive()) {
				return GestureState.COMPLETED;
			}
			else {
				return GestureState.NOT_DETECTED;
			}

		case COMPLETED:
			if(touchReleased || currentlyTrackedHand == null)
				return GestureState.NOT_DETECTED;
			else
				return GestureState.COMPLETED;

		default:	//unknown state...
			return GestureState.NOT_DETECTED;
		}
	}

}
