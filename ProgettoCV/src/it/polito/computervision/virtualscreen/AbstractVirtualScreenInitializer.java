package it.polito.computervision.virtualscreen;

import com.primesense.nite.HandTracker;

/**
 * Provides a base implementation, taking care of thread management and callback notification.
 * @author giovanni
 *
 */
public abstract class AbstractVirtualScreenInitializer implements VirtualScreenInitializer, Runnable {

	private InitializerCallback callback;
	protected VirtualScreen vscreen;
	protected HandTracker tracker;
	
	private boolean run, initResult, threaded;

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
			return startInitialization();
		}
	}
	
	/**
	 * Called by subclasses to notify the end of the init procedure
	 * @param ok True if initialization was successful, false otherwise
	 */
	protected final synchronized void initializationComplete(boolean ok) {
		run = false;
		initResult = ok;
		notify();
	}
	
	/**
	 * Subclasses should implement their initialization logic here
	 * 
	 * @return true if initialization has completed, false otherwise (will be notified by the callback
	 */
	protected abstract boolean startInitialization();

	@Override
	public final void run() {
		
		startInitialization();
		
		//wait until we're signaled from the subclass
		synchronized(this) {
			while(run) {
				try {
					wait();
				}
				catch(InterruptedException e) {}
			}
		}
	
		callback.initializationComplete(initResult);
	}

}