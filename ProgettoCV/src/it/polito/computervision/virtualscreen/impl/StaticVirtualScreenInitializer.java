/**
 * 
 */
package it.polito.computervision.virtualscreen.impl;

import org.opencv.core.Size;

import it.polito.computervision.virtualscreen.AbstractVirtualScreenInitializer;
import it.polito.computervision.virtualscreen.VirtualScreen;
import it.polito.computervision.virtualscreen.VirtualScreenInitializer;

/**
 * A simple {@link VirtualScreenInitializer} that initializes the {@link VirtualScreen} in a fixed position.
 * @author giovanni
 *
 */
public class StaticVirtualScreenInitializer extends AbstractVirtualScreenInitializer {

	private Size size;
	private float depth;
	
	/**
	 * 
	 * @param size The size of the {@link VirtualScreen}
	 * @param depth The depth of the {@link VirtualScreen}
	 * @param handsToTrack How many hands to track
	 */
	public StaticVirtualScreenInitializer(Size size, float depth) {
		super(false);
		this.size = size;
		this.depth = depth;
	}

	@Override
	protected boolean startInitialization() {
		//statically initialize screen params
		vscreen.setDepth(depth);
		vscreen.setSize(size);
		
		return true;
	}
}
