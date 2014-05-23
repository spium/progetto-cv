package it.polito.computervision.virtualscreen;

import java.util.Collection;
import java.util.HashSet;

import com.primesense.nite.HandTracker;
import com.primesense.nite.HandTracker.NewFrameListener;
import com.primesense.nite.HandTrackerFrameRef;

/**
 * Singleton that manages the virtual screen. It needs to be initialized with a {@link VirtualScreen} and a {@link VirtualScreenInitializer}.
 * It updates {@link VirtualScreenListener}s on each frame with the updated {@link HandData}.
 * @author giovanni
 *
 */
public class VirtualScreenManager implements NewFrameListener {
	
	private static VirtualScreenManager instance = null;
	
	private VirtualScreen vscreen;
	private Collection<HandTracker> trackers;
	private HandTrackerFrameRef lastFrame;
	private Collection<VirtualScreenListener> listeners;
	
	

	/**
	 * Creates an empty, uninitialized VirtualScreenManager
	 */
	private VirtualScreenManager() {
		vscreen = null;
		lastFrame = null;
		trackers = new HashSet<HandTracker>();
		listeners = new HashSet<VirtualScreenListener>();
	}
	
	public static VirtualScreenManager getInstance() {
		if(instance == null)
			instance = new VirtualScreenManager();
		
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewFrame(HandTracker handTracker) {
		// TODO Auto-generated method stub

	}

}
