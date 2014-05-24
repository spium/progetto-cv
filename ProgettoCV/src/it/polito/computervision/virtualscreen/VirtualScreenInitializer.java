package it.polito.computervision.virtualscreen;

import java.util.Collection;

import com.primesense.nite.HandTracker;

/**
 * Classes implementing this interface will have to initialize the {@link VirtualScreen} parameters (namely size and depth).
 * When the init procedure has completed they'll have to invoke {@link InitializerCallback#initializationComplete(boolean, Collection)}
 * in order to let the caller know the init procedure has completed. 
 * @author giovanni
 *
 */
public interface VirtualScreenInitializer {
	/**
	 * Starts the {@link VirtualScreen} initialization procedure.
	 * This method should be called only once.
	 * @param vscreen (In/Out) the virtual screen that needs to be initialized
	 * @param callback The objecy providing the {@link InitializerCallback#initializationComplete(boolean, Collection)} callback.
	 */
	public void initialize(VirtualScreen vscreen, InitializerCallback callback);
	
	public interface InitializerCallback {
		
		/**
		 * Invoked by the {@link VirtualScreenInitializer} when the init procedure has completed.
		 * @param ok Whether the init procedure was successful or not
		 * @param trackers The {@link HandTracker}s that were created.
		 */
		public void initializationComplete(boolean ok, Collection<HandTracker> trackers);
	}
}
