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
public class ZoomGesture extends AbstractGesture {

	private float targetValue;
	private float differenceX=-1;
	private float movementAccumulator;
	private float currentDiffX=-1;
	private float currentDiffY=-1;
	private float threshold;
	
	/**
	 * Creates a ClickGesture named "click"
	 */
	public ZoomGesture() {
		this("click");
	}
	
	/**
	 * Creates a ClickGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public ZoomGesture(String name) {
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
			movementAccumulator=0;
			for(HandData hd : hands) {
				if(!hd.isTouching()) {
					return GestureState.NOT_DETECTED;
				}
				else{
					differenceX=(differenceX == -1) ? hd.getPosition().getX() : Math.abs(differenceX - hd.getPosition().getX());
				
				}
				
			
			}
			return GestureState.POSSIBLE_DETECTION;
			

		case POSSIBLE_DETECTION:
			currentDiffX=-1;
			currentDiffY=-1;
			for(HandData hd : hands) {
				if(!hd.isTouching()) 
					return GestureState.NOT_DETECTED;
				
				else {
					currentDiffX=(currentDiffX == -1) ? hd.getPosition().getX() : Math.abs(currentDiffX - hd.getPosition().getX());
					currentDiffY=(currentDiffY == -1) ? hd.getPosition().getY() : Math.abs(currentDiffY - hd.getPosition().getY());
				}
			}

			movementAccumulator += currentDiffX - differenceX;
			differenceX=currentDiffX;
			
			
			if ((movementAccumulator > targetValue)&&(currentDiffY < threshold))
				return GestureState.IN_PROGRESS;
			else
				return GestureState.POSSIBLE_DETECTION;

		case IN_PROGRESS:
			currentDiffX=-1;
			for(HandData hd : hands) {
				if (!hd.isTouching())
					return GestureState.COMPLETED;
					
				else{
					currentDiffX=(currentDiffX == -1) ? hd.getPosition().getX() : Math.abs(currentDiffX - hd.getPosition().getX());
					
				}
			}
			
			System.out.println((currentDiffX - differenceX>0) ? "positivo" : "negativo");
			differenceX=currentDiffX;
			return GestureState.IN_PROGRESS;
		
		default:	//COMPLETED or unknown state...
		
			return GestureState.NOT_DETECTED;
		}
	}
	else
		return GestureState.NOT_DETECTED;
}
	
}
