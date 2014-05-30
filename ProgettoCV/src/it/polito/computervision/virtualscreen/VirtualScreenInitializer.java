package it.polito.computervision.virtualscreen;

import com.primesense.nite.HandTracker;

/**
 * Classes implementing this interface will have to initialize the {@link VirtualScreen} parameters (namely size and depth).
 * When the init procedure has completed they'll have to invoke {@link InitializerCallback#initializationComplete(boolean)}
 * in order to let the caller know the init procedure has completed. 
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
	 * @return true if the initialization is complete, false otherwise (the callback will be called when complete)
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
