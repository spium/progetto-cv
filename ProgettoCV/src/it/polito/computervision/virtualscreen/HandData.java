package it.polito.computervision.virtualscreen;

import org.openni.Point2D;


/**
 * Represents data about a specific hand.
 * @author giovanni
 *
 */
public class HandData {
	
	private short id;
	private Point2D<Float> position, projectedPosition;
	private boolean touching;
	
	/**
	 * 
	 * @param id The unique ID of the hand
	 * @param position The 2D position of the hand on the virtual screen (in world coordinates)
	 * @param projectedPosition The 2D position of the hand in depth coordinates
	 * @param touching Whether the hand is touching the virtual screen or not
	 */
	public HandData(short id, Point2D<Float> position, Point2D<Float> projectedPosition, boolean touching) {
		this.id = id;
		this.position = position;
		this.projectedPosition = projectedPosition;
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
	 * @return the 2D position of the hand on the virtual screen (in real coordinates)
	 */
	public Point2D<Float> getPosition() {
		return position;
	}
	
	/**
	 * 
	 * @return the 2D position in depth coordinates
	 */
	public Point2D<Float> getProjectedPosition() {
		return projectedPosition;
	}

	/**
	 * 
	 * @return true if the hand is touching the virtual screen, false otherwise
	 */
	public boolean isTouching() {
		return touching;
	}
	
	@Override
	public String toString() {
		return id + ": (X:" + position.getX() + ",Y:" + position.getY() + ") " + touching;
	}
}
