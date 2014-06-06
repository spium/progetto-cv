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
public class BoxGesture extends AbstractGesture {

	private float targetValue;
	private float differenceX=-1;
	private float differenceY=-1;
	private float Xaccumulator;
	private float Yaccumulator;
	private float currentDiffX=-1;
	private float currentDiffY=-1;
	
	
	/**
	 * Creates a ClickGesture named "click"
	 */
	public BoxGesture() {
		this("click");
	}
	
	/**
	 * Creates a ClickGesture with the given name
	 * @param name The name of this {@link Gesture}.
	 */
	public BoxGesture(String name) {
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
			Yaccumulator=0;
			Xaccumulator=0;
			for(HandData hd : hands) {
				if(!hd.isTouching()) {
					return GestureState.NOT_DETECTED;
				}
				else{
					differenceX=(differenceX == -1) ? hd.getPosition().getX() : Math.abs(differenceX - hd.getPosition().getX());
					differenceY=(differenceY == -1) ? hd.getPosition().getY() : Math.abs(differenceY - hd.getPosition().getY());
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

			Xaccumulator += Math.abs(currentDiffX - differenceX);
			Yaccumulator += Math.abs(currentDiffY - differenceY);
			differenceX=currentDiffX;
			differenceY=currentDiffY;
			
			if ((Xaccumulator > targetValue)&&(Yaccumulator>targetValue))
				return GestureState.IN_PROGRESS;
			else
				return GestureState.POSSIBLE_DETECTION;

		case IN_PROGRESS:
			Point2D Top=new Point2D(0,0);
			Point2D Bot=new Point2D(0,0);
			float XBox=500;
			float YBox=500;
			for(HandData hd : hands) {
				if (!hd.isTouching())
					return GestureState.COMPLETED;
				else{
					if (hd.getPosition().getX()<XBox)
						Top = hd.getPosition();
					if (hd.getPosition().getY()<YBox)
						Bot=hd.getPosition();
				
				}
			}
			
			System.out.println("Box: Top Point(" + Top.getX()+"," + Top.getY()+")  Bot Point("+Bot.getX()+","+Bot.getY()+")");
			return GestureState.IN_PROGRESS;
		
		default:	//COMPLETED or unknown state...
		
			return GestureState.NOT_DETECTED;
		}
	}
	else
		return GestureState.NOT_DETECTED;
}
	
}
