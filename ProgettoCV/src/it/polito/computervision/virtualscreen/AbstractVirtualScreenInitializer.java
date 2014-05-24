package it.polito.computervision.virtualscreen;

import java.util.ArrayList;
import java.util.List;

import com.primesense.nite.HandTracker;

/**
 * Provides a base implementation, taking care of thread management and callback notification.
 * @author giovanni
 *
 */
public abstract class AbstractVirtualScreenInitializer extends Thread implements VirtualScreenInitializer {

	private InitializerCallback callback;
	protected VirtualScreen vscreen;
	protected List<HandTracker> trackers;
	
	private boolean run, initResult;

	public AbstractVirtualScreenInitializer() {
		this(2);
	}

	public AbstractVirtualScreenInitializer(int nTrackers) {
		super();
		vscreen = null;
		callback = null;
		trackers = new ArrayList<HandTracker>(nTrackers);
		run = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void initialize(VirtualScreen vscreen, InitializerCallback callback) {
		//do nothing if the thread has already been started
		if(getState() == State.NEW) {
			this.vscreen = vscreen;
			this.callback = callback;
			//start this thread
			start();
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
				}
				catch(InterruptedException e) {}
			}
		}
	
		callback.initializationComplete(initResult, trackers);
	}

}