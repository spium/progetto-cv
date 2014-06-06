package it.polito.computervision.gestures.impl;

import java.util.Collection;

import org.openni.Point2D;

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
public class PanGesture extends AbstractGesture {

	private short handId;
	private int targetValue;
	private float XLast=-1;
	private float YLast=-1;
	private float movementAccumulator;
	/**
	 * Creates a ClickGesture named "click"
	 */
	public PanGesture() {
		this("click");
	}
	
	/**
	 * Creates a ClickGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public PanGesture(String name) {
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
			movementAccumulator=0;
			for(HandData hd : hands) {
				if(hd.isTouching()) {
					handId = hd.getId();
					XLast=hd.getPosition().getX();
					YLast=hd.getPosition().getY();
					return GestureState.POSSIBLE_DETECTION;
				}
			}

			return GestureState.NOT_DETECTED;

		case POSSIBLE_DETECTION:
			for(HandData hd : hands) {
				if((hd.getId() == handId)&&(hd.isTouching())) {
					movementAccumulator+=Math.abs(hd.getPosition().getX()-XLast)+
							Math.abs(hd.getPosition().getY()-YLast);
					
					if (movementAccumulator>=targetValue)
						return GestureState.IN_PROGRESS;
				}
			}
			
			handId = -1;
			return GestureState.NOT_DETECTED;

		case IN_PROGRESS:
			for(HandData hd : hands) {
				if((hd.getId() == handId)) {
				if (hd.isTouching()){
					System.out.println("X:" + hd.getPosition().getX().toString() + ", Y:" + hd.getPosition().getY().toString());
					return GestureState.IN_PROGRESS;
					
				}
				else
					return GestureState.COMPLETED;
					
				}
			}
			
			handId = -1;
			return GestureState.NOT_DETECTED;


		default:	//COMPLETED or unknown state...
			handId = -1;
			return GestureState.NOT_DETECTED;
		}
	}

}
