package it.polito.computervision.virtualscreen;

import java.util.List;

/**
 * This interface is implemented by classes who want to be updated on each frame about the 2D hand positions projected on the virtual screen
 * @author giovanni
 *
 */
public interface VirtualScreenListener {
	/**
	 * Invoked on each frame
	 * @param hands The hands currently being tracked.
	 */
	public void onNewFrame(List<HandData> hands);
}
