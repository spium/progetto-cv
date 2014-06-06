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
public class HandsXPan extends AbstractGesture {

	
	private float tollerance;
	private float movementAccumulator;
	private float currentDiffX=-1;
	private float distance=-1;
	private float lastX;
	private float currentX;
	private float targetValue;
	
	/**
	 * Creates a ClickGesture named "click"
	 */
	public HandsXPan() {
		this("click");
	}
	
	/**
	 * Creates a ClickGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public HandsXPan(String name) {
		super(name, false);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GestureState doUpdateState(Collection<HandData> hands, Collection<HandData> gestureHands) {
	if (hands.size()==2){
		
		for(HandData hd : hands) 
			gestureHands.add(hd);	
		
		
		switch(getCurrentState()) {
		case NOT_DETECTED:
			
			lastX=-1;
			for(HandData hd : hands) {
				if(!hd.isTouching()) 
					return GestureState.NOT_DETECTED;
					
				else {
					if (hd.getPosition().getX()>lastX)
						lastX=hd.getPosition().getX();
				}
			}
			movementAccumulator=0;
			return GestureState.POSSIBLE_DETECTION;
			

		case POSSIBLE_DETECTION:
			currentDiffX=-1;
			currentX=-1;
			for(HandData hd : hands) {
				if(!hd.isTouching()) 
					return GestureState.NOT_DETECTED;
				
				else {
					if (hd.getPosition().getX()>currentX)
						currentX=hd.getPosition().getX();
					
					currentDiffX=(currentDiffX == -1) ? hd.getPosition().getX() : Math.abs(currentDiffX - hd.getPosition().getX());
					
					
				}
			}

			if (distance==-1)
				distance=currentDiffX;
			
			movementAccumulator += Math.abs(currentX-lastX);
			lastX=currentX;
			
			if ((movementAccumulator > targetValue)&&(currentDiffX>distance-tollerance)&&(currentDiffX<distance+tollerance))
				return GestureState.IN_PROGRESS;
			else
				return GestureState.POSSIBLE_DETECTION;

		case IN_PROGRESS:
			currentX=-1;
			currentDiffX=-1;
			for(HandData hd : hands) {
				if (!hd.isTouching())
					return GestureState.NOT_DETECTED;
					
				else{
					if (hd.getPosition().getX()>currentX)
						currentX=hd.getPosition().getX();
			
					currentDiffX=(currentDiffX == -1) ? hd.getPosition().getX() : Math.abs(currentDiffX - hd.getPosition().getX());
					
				}
			}
			
			movementAccumulator += Math.abs(currentX-lastX);
			lastX=currentX;
			if (movementAccumulator>3*targetValue)
				return GestureState.COMPLETED;
			
			return GestureState.IN_PROGRESS;
		
		default:	//COMPLETED or unknown state...
		
			return GestureState.NOT_DETECTED;
		}
	}
	else
		return GestureState.NOT_DETECTED;
}
	
}
