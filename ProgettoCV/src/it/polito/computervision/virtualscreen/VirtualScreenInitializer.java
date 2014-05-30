package it.polito.computervision.virtualscreen;

import com.primesense.nite.HandTracker;

/**
 * Classes implementing this interface will have to initialize the {@link VirtualScreen} parameters (namely size and depth).
 * The initialization procedure could be either synchronous or asynchronous. If it's synchronous, the initialization must take place
 * in the {@link VirtualScreenInitializer#initialize(VirtualScreen, HandTracker, InitializerCallback)} method, which must return true.
 * Otherwise the {@link VirtualScreenInitializer#initialize(VirtualScreen, HandTracker, InitializerCallback)} method must return false
 * immediatly, and when the init procedure has completed, {@link InitializerCallback#initializationComplete(boolean)} must be called
 * from another thread in order to let the caller know the init procedure has completed. 
 * @author giovanni
 *
 */
public interface VirtualScreenInitializer {
	/**
	 * Starts the {@link VirtualScreen} initialization procedure.
	 * This method should be called only once.
	 * @param vscreen (In/Out) the virtual screen that needs to be initialized
	 * @param tracker The {@link HandTracker}
	 * @param callback The object providing the {@link InitializerCallback#initializationComplete(boolean)} callback.
	 * 
	 * @return true if this call is synchronous (the screen has been initialized in this call), false if it's asynchronous
	 * (the screen has not been initialized, and the callback will be used to notify its completion).
	 */
	public boolean initialize(VirtualScreen vscreen, HandTracker tracker, InitializerCallback callback);
	
	public interface InitializerCallback {
		
		/**
		 * Invoked by the {@link VirtualScreenInitializer} when the init procedure has completed.
		 * @param ok Whether the init procedure was successful or not
		 */
		public void initializationComplete(boolean ok);
	}
}
