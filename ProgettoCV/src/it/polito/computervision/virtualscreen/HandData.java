package it.polito.computervision.virtualscreen;

import org.openni.Point2D;


/**
 * Represents data about a specific hand.
 * @author giovanni
 *
 */
public class HandData {
	
	private short id;
	private Point2D<Float> position;
	private boolean touching;
	
	/**
	 * 
	 * @param id The unique ID of the hand
	 * @param position The 2D position of the hand on the virtual screen
	 * @param touching Whether the hand is touching the virtual screen or not
	 */
	public HandData(short id, Point2D<Float> position, boolean touching) {
		this.id = id;
		this.position = position;
		this.touching = touching;
	}

	/**
	 * 
	 * @return the unique ID of the hand
	 */
	public short getId() {
		return id;
	}

	/**
	 * 
	 * @return the 2D position of the hand on the virtual screen
	 */
	public Point2D<Float> getPosition() {
		return position;
	}

	/**
	 * 
	 * @return true if the hand is touching the virtual screen, false otherwise
	 */
	public boolean isTouching() {
		return touching;
	}
}
