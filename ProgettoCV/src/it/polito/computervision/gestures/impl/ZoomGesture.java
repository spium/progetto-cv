package it.polito.computervision.gestures.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.AbstractGesture;
import it.polito.computervision.gestures.GestureState;
import it.polito.computervision.virtualscreen.HandData;

public class ZoomGesture extends AbstractGesture {

	public enum Direction { INWARD, OUTWARD, BOTH }

	public static final float MIN_DETECTION_DISTANCE = 35.f;
	public static final float THRESHOLD = 200.f;

	private float minDistance, initialDistance, threshold;
	private Direction direction;
	private ArrayList<Short> handIds;


	public ZoomGesture() {
		this("zoom");
	}

	/**
	 * Creates a ZoomGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public ZoomGesture(String name) {
		this(name, Direction.BOTH, MIN_DETECTION_DISTANCE, THRESHOLD, true);
	}

	public ZoomGesture(String name, Direction direction, float minDistance, float threshold, boolean live) {
		super(name, live);
		if(minDistance <= 0)
			throw new IllegalArgumentException("minDistance <= 0");
		if(threshold <= 0 || threshold < minDistance)
			throw new IllegalArgumentException("threshold <= 0 or threshold < minDistance");

		this.direction = direction;
		this.minDistance = minDistance;
		initialDistance = -1;
		this.threshold = threshold;
		handIds = new ArrayList<Short>(2);
	}

	private boolean allHands(Collection<HandData> hands) {
		int count = 0;
		for(HandData hd : hands) {
			if(handIds.contains(hd.getId())) {
				++count;
				if(count == handIds.size())
					return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState doUpdateState(Collection<HandData> hands, Collection<HandData> gestureHands) {


		if(handIds.size() == 2 && allHands(hands))
			for(HandData hd : hands)
				if(handIds.contains(hd.getId()))
					gestureHands.add(hd);

		Mat hand1 = null, hand2 = null;

		switch(getCurrentState()) {
		case NOT_DETECTED:
			if(hands.size() < 2)
				return GestureState.NOT_DETECTED;

			for(HandData hd : hands) {
				if(hd.isTouching()) {
					handIds.add(hd.getId());
					if(hand1 == null) hand1 = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
					else {
						hand2 = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
						Core.subtract(hand1, hand2, hand1);
						initialDistance = (float) Core.norm(hand1);
						return GestureState.POSSIBLE_DETECTION;
					}
				}
			}

			handIds.clear();
			return GestureState.NOT_DETECTED;


		case POSSIBLE_DETECTION:
			if(!allHands(hands)) {
				handIds.clear();
				initialDistance = -1;
				return GestureState.NOT_DETECTED;
			}

			for(HandData hd : hands) {
				if(handIds.contains(hd.getId()) && hd.isTouching()) {
					if(hand1 == null) hand1 = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
					else {
						hand2 = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
						Core.subtract(hand1, hand2, hand1);
						float currDistance = (float) Core.norm(hand1);
						float diff = currDistance - initialDistance;

						if(Math.abs(diff) >= minDistance) {
							if(direction == Direction.BOTH || (direction == Direction.OUTWARD && diff > 0) || (direction == Direction.INWARD && diff < 0)) {
								return GestureState.IN_PROGRESS;
							}
							else {
								handIds.clear();
								initialDistance = -1;
								return GestureState.NOT_DETECTED;
							}
						}
						else {
							return GestureState.POSSIBLE_DETECTION;
						}
					}	
				}
			}

			handIds.clear();
			initialDistance = -1;
			return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			if(!allHands(hands)) {
				if(isLive()) return GestureState.COMPLETED;
				else {
					handIds.clear();
					initialDistance = -1;
					return GestureState.NOT_DETECTED;
				}
			}

			for(HandData hd : hands) {
				if(handIds.contains(hd.getId())) {
					if(isLive() && !hd.isTouching())
						return GestureState.COMPLETED;

					if(!isLive() && hd.isTouching()) {
						if(hand1 == null) hand1 = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
						else {
							hand2 = new MatOfFloat(hd.getPosition().getX(), hd.getPosition().getY());
							Core.subtract(hand1, hand2, hand1);
							float currDistance = (float) Core.norm(hand1);
							float diff = currDistance - initialDistance;

							if(Math.abs(diff) >= threshold) {
								if(direction == Direction.BOTH || (direction == Direction.OUTWARD && diff > 0) || (direction == Direction.INWARD && diff < 0)) {
									return GestureState.COMPLETED;
								}
								else {
									handIds.clear();
									initialDistance = -1;
									return GestureState.NOT_DETECTED;
								}
							}
							else {
								return GestureState.IN_PROGRESS;
							}
						}
					}
				}
			}

			if(isLive()) return GestureState.IN_PROGRESS;

			handIds.clear();
			initialDistance = -1;
			return GestureState.NOT_DETECTED;

		case COMPLETED:
			if(isLive() || !allHands(hands)) {
				handIds.clear();
				initialDistance = -1;
				return GestureState.NOT_DETECTED;
			}
			else {
				for(HandData hd : hands)
					if(handIds.contains(hd.getId()) && !hd.isTouching()) {
						handIds.clear();
						initialDistance = -1;
						return GestureState.NOT_DETECTED;
					}
				
				return GestureState.COMPLETED;
			}
			
			
		default:	//unknown state...

			handIds.clear();
			initialDistance = -1;
			return GestureState.NOT_DETECTED;
		}
	}
}
