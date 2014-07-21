/**
 * 
 */
package it.polito.computervision.virtualscreen.impl;

import org.opencv.core.Size;
import org.openni.Point2D;

import com.primesense.nite.Point3D;

import it.polito.computervision.virtualscreen.VirtualScreen;
import it.polito.computervision.virtualscreen.AbstractVirtualScreen;

/**
 * A simple flat {@link VirtualScreen}. A {@link Point3D} is touching if its Z-coordinate is <= depth.
 * The 2D projection is simply the input point with z = 0.
 * This implementation does not take into consideration the screen size (i.e. size is infinite)
 * @author Giovanni Piumatti
 *
 */
public class FlatVirtualScreen extends AbstractVirtualScreen {

	public FlatVirtualScreen() {
		super();
	}
	
	public FlatVirtualScreen(Size size, float depth) {
		super(size, depth);
	}
	
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
	public Point2D<Float> get2DProjection(Point3D<Float> point) {
		return new Point2D<Float>(point.getX(), point.getY());
	}

}
