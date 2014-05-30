package it.polito.computervision.virtualscreen;

import org.opencv.core.Size;

import com.primesense.nite.HandTracker;
import org.openni.Point2D;
import com.primesense.nite.Point3D;

/**
 * Represents a generic 2D virtual screen in front of the user.
 * @author giovanni
 *
 */
public interface VirtualScreen {
	/**
	 * 
	 * @return the size of the virtual screen
	 */
	public Size getSize();
	/**
	 * Sets the size of the virtual screen
	 * @param size The new size of the virtual screen
	 */
	public void setSize(Size size);
	/**
	 * 
	 * @return the depth of the virtual screen (i.e. its distance from the origin of the world coordinate system along the Z axis)
	 */
	public float getDepth();
	/**
	 * Sets the depth of the virtual screen
	 * @param depth The new depth of the virtual screen
	 */
	public void setDepth(float depth);
	/**
	 * Checks if the given 3D point is touching the 2D virtual screen
	 * @param point The 3D point to check
	 * @return true if it is touching the virtual screen, false otherwise.
	 */
	public boolean isTouching(Point3D<Float> point);
	/**
	 * Converts the 3D point into its 2D projection on the virtual screen.
	 * Notice that Point3D is in package com.primesense.nite whereas Point2D is in package org.openni.
	 * The reason is that com.primesense.nite.Point2D's constructor is not visible.
	 * @param point The 3D point to convert
	 * @param tracker the {@link HandTracker}
	 * @return the 2D projection of the point on the virtual screen
	 */
	public Point2D<Float> get2DProjection(Point3D<Float> point, HandTracker tracker);
}
