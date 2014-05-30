/**
 * 
 */
package it.polito.computervision.virtualscreen.impl;

import com.primesense.nite.HandTracker;
import org.openni.Point2D;
import com.primesense.nite.Point3D;

import it.polito.computervision.virtualscreen.AbstractVirtualScreen;

/**
 * A simple flat {@link VirtualScreen}. A {@link Point3D} is touching if its z coordinate is <= depth.
 * The 2D projection is simply the input point with z = 0.
 * @author giovanni
 *
 */
public class FlatVirtualScreen extends AbstractVirtualScreen {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTouching(Point3D<Float> point) {
		return point.getZ() <= depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D<Float> get2DProjection(Point3D<Float> point, HandTracker tracker) {
		com.primesense.nite.Point2D<Float> pos = tracker.convertHandCoordinatesToDepth(point);
		//mirror the X coordinate
		return new Point2D<Float>(tracker.readFrame().getDepthFrame().getWidth() - pos.getX(), pos.getY());
	}

}
