/**
 * 
 */
package it.polito.computervision.virtualscreen;

import org.opencv.core.Size;

/**
 * Implements the common functionality of a {@link VirtualScreen}
 * @author giovanni
 */
public abstract class AbstractVirtualScreen implements VirtualScreen {

	protected Size size;
	protected float depth;
	
	/**
	 * Initializes an empty virtual screen. Size and depth are set to illegal values.
	 * The will need to be set before using this virtual screen instance.
	 */
	public AbstractVirtualScreen() {
		this(null, -1.f);
	}
	
	/**
	 * Initializes the virtual screen with the given size and depth
	 * @param size The size of the virtual screen
	 * @param depth The depth of the virtual screen
	 */
	public AbstractVirtualScreen(Size size, float depth) {
		this.size = size;
		this.depth = depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Size getSize() {
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(Size size) {
		this.size = size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getDepth() {
		return depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDepth(float depth) {
		this.depth = depth;
	}
}
