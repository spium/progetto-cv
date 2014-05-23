package it.polito.computervision.virtualscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.primesense.nite.HandTracker;
import com.primesense.nite.HandTracker.NewFrameListener;
import com.primesense.nite.HandTrackerFrameRef;
import com.primesense.nite.Point3D;

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
		// TODO get the HandTrackerFrameRef, notify listeners
		// need to manage callbacks from multiple trackers -> only issue one callback to the screen listeners

	}

	/**
	 * Initializes the {@link VirtualScreen} with the given {@link VirtualScreenInitializer}
	 * @param vscreen The {@link VirtualScreen} to initialize
	 * @param vscreenInit The {@link VirtualScreenInitializer} to use
	 * @return true if the initialization was successful, false otherwise
	 */
	public boolean initialize(VirtualScreen vscreen, VirtualScreenInitializer vscreenInit) {
		boolean ok = vscreenInit.initialize(vscreen, trackers);
		if(ok) this.vscreen = vscreen;
		return ok;
	}

	/**
	 * 
	 * @return true if the VirtualScreenManager has been correctly initialized, false otherwise
	 */
	public boolean isInitialized() {
		return vscreen != null;
	}

	/**
	 * 
	 * @return the {@link VirtualScreen} this manager is using, or null if it hasn't been initialized yet
	 */
	public VirtualScreen getVirtualScreen() {
		return vscreen;
	}
	
	/**
	 * Disposes of all resources this manager is using. After this call, the manager will be in its default uninitialized state.
	 * It is safe to call again {@link VirtualScreenManager#initialize(VirtualScreen, VirtualScreenInitializer)} after this call.
	 * This method is automatically called in the destructor. It is safe to invoke multiple times.
	 */
	public void destroy() {
		vscreen = null;
		if(lastFrame != null) {
			lastFrame.release();
			lastFrame = null;
		}
		//destroy hand trackers and remove them from the set
		for(Iterator<HandTracker> i = trackers.iterator(); i.hasNext();) {
			i.next().destroy();
			i.remove();
		}
		//deregister all listeners
		listeners.clear();
	}
	
	@Override
	protected void finalize() {
		destroy();
	}

	/**
	 * Adds a new {@link VirtualScreenListener}
	 * @param listener the {@link VirtualScreenListener}
	 */
	public void addVirtualScreenListener(VirtualScreenListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a {@link VirtualScreenListener}
	 * @param listener the {@link VirtualScreenListener}
	 */
	public void removeVirtualScreenListener(VirtualScreenListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies the {@link VirtualScreenListener}s that a new frame is available
	 */
	private void notifyListeners() {
		if(lastFrame != null) {
			//get the original NiTE HandData collection
			List<com.primesense.nite.HandData> handsOrig = lastFrame.getHands();
			//convert to our own HandData
			Collection<HandData> hands = new ArrayList<HandData>(handsOrig.size());
			for(com.primesense.nite.HandData hd : handsOrig) {
				Point3D<Float> pos = hd.getPosition();
				hands.add(new HandData(hd.getId(), vscreen.get2DProjection(pos), vscreen.isTouching(pos)));
			}

			//notify all listeners
			for(VirtualScreenListener l : listeners) {
				l.onNewFrame(hands);
			}
		}
	}

}
