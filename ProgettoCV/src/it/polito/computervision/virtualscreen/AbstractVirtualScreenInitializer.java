package it.polito.computervision.virtualscreen;

import com.primesense.nite.HandTracker;

/**
 * Provides a base implementation, taking care of thread management and callback notification.
 * If the concrete implementation needs to run in a separate thread, this class must be constructed with the threaded parameter set to true.
 * In this case, the {@link #startInitialization()} call will be considered asynchronous, and it will be executed in a new thread. To notify
 * that the init procedure has completed, the {@link #initializationComplete(boolean)} method must be called from a separate thread.
 * With the threaded parameter set to false, the {@link #startInitialization()} call will be considered synchronous (i.e. after it returns
 * it's assumed the screen has been correctly initialized).
 * 
 * @author giovanni
 *
 */
public abstract class AbstractVirtualScreenInitializer implements VirtualScreenInitializer, Runnable {

	private InitializerCallback callback;
	protected VirtualScreen vscreen;
	protected HandTracker tracker;
	
	private boolean run, initResult, threaded;

	/**
	 * @param threaded Whether or not the initialization procedure should be run in a separate thread.
	 */
	public AbstractVirtualScreenInitializer(boolean threaded) {
		super();
		vscreen = null;
		callback = null;
		tracker = null;
		run = true;
		this.threaded = threaded;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean initialize(VirtualScreen vscreen, HandTracker tracker, InitializerCallback callback) {
		this.vscreen = vscreen;
		this.callback = callback;
		this.tracker = tracker;
		
		if(threaded) {
			new Thread(this).start();
			return false;
		}
		else {
			startInitialization();
			return true;
		}
	}
	
	/**
	 * Called by subclasses to notify the end of the init procedure
	 * @param ok True if initialization was successful, false otherwise
	 */
	protected final synchronized void initializationComplete(boolean ok) {
		if(threaded) {
			run = false;
			initResult = ok;
			notify();
		}
	}
	
	/**
	 * Subclasses should implement their initialization logic here
	 */
	protected abstract void startInitialization();

	@Override
	public final void run() {
		
		startInitialization();
		
		//wait until we're signaled from the subclass
		synchronized(this) {
			while(run) {
				try {
					wait();
					callback.initializationComplete(initResult);
				}
				catch(InterruptedException e) {}
			}
		}
	}
}