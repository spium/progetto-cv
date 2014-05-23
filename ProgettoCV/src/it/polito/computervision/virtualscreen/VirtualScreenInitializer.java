package it.polito.computervision.virtualscreen;

import java.util.Collection;

import com.primesense.nite.HandTracker;

/**
 * Classes implementing this interface will have to initialize the {@link VirtualScreen} parameters (namely size and depth)
 * @author giovanni
 *
 */
public interface VirtualScreenInitializer {
	/**
	 * Runs the {@link VirtualScreen} initialization procedure. In case the procedure creates {@link HandTracker}s (in order to run initialization),
	 * they will have to be added to the given collection.
	 * @param vscreen (In/Out) the virtual screen that needs to be initialized
	 * @param trackers (In/Out) collection to add {@link HandTracker}s to
	 * @return true if the initialization was successful, false otherwise
	 */
	public boolean initialize(VirtualScreen vscreen, Collection<HandTracker> trackers);
}
