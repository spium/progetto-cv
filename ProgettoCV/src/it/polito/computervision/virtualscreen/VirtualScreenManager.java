package it.polito.computervision.virtualscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.primesense.nite.HandTracker;
import com.primesense.nite.HandTracker.NewFrameListener;
import com.primesense.nite.GestureData;
import com.primesense.nite.GestureType;
import com.primesense.nite.HandTrackerFrameRef;
import com.primesense.nite.Point3D;

/**
 * Singleton that manages the virtual screen. It needs to be initialized with a {@link VirtualScreen} and a {@link VirtualScreenInitializer}.
 * It updates {@link VirtualScreenListener}s on each frame with the updated {@link HandData}.
 * @author giovanni
 *
 */
public class VirtualScreenManager implements NewFrameListener {

	private static final GestureType gestureType = GestureType.HAND_RAISE;

	private static VirtualScreenManager instance = null;

	private VirtualScreen vscreen;
	private HandTracker tracker;
	private HandTrackerFrameRef lastFrame;
	private Collection<VirtualScreenListener> listeners;
	private boolean initialized, initDone, running;
	private int handsToTrack, trackingHands;

	/**
	 * Creates an empty, uninitialized VirtualScreenManager
	 */
	private VirtualScreenManager() {
		vscreen = null;
		lastFrame = null;
		initialized = initDone = running = false;
		tracker = HandTracker.create();
		listeners = new HashSet<VirtualScreenListener>();
		handsToTrack = -1;
		trackingHands = 0;
	}

	/**
	 * 
	 * @return the {@link VirtualScreenManager} instance
	 */
	public static VirtualScreenManager getInstance() {
		if(instance == null)
			instance = new VirtualScreenManager();

		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onNewFrame(HandTracker handTracker) {
		if(lastFrame != null) {
			lastFrame.release();
			lastFrame = null;
		}

		lastFrame = tracker.readFrame();

		//check for lost hands
		for(com.primesense.nite.HandData hd : lastFrame.getHands()) {
			if(hd.isLost()) {
				--trackingHands;
				tracker.startGestureDetection(gestureType);
			}
		}

		//check for newly found hands
		for(GestureData gd : lastFrame.getGestures()) {
			if(gd.isComplete()) {
				try {
					//start the tracker
					tracker.startHandTracking(gd.getCurrentPosition());
					++trackingHands;
				}
				catch(Exception e) {
					//do nothing... sometimes the native call will return ERROR, resulting in a runtime exception
				}
			}
		}

		//if we have all hands needed, stop gesture detection
		if(trackingHands == handsToTrack) {
			tracker.stopGestureDetection(gestureType);
		}

		notifyListeners();
	}

	/**
	 * Initializes the {@link VirtualScreen} with the given {@link VirtualScreenInitializer}
	 * @param vscreen The {@link VirtualScreen} to initialize
	 * @param vscreenInit The {@link VirtualScreenInitializer} to use
	 * @return true if the initialization was successful, false otherwise
	 */
	public boolean initialize(VirtualScreen vscreen, VirtualScreenInitializer vscreenInit) {
		final VirtualScreenManager vsm = this;
		vscreenInit.initialize(vscreen, tracker, new VirtualScreenInitializer.InitializerCallback() {

			@Override
			public void initializationComplete(boolean ok) {
				synchronized(vsm) {
					vsm.initDone = true;
					vsm.initialized = ok;
					vsm.notify();
				}
			}
		});

		//wait until the init procedure is done
		synchronized(this) {
			while(!initDone) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}

		return initialized;
	}

	/**
	 * Starts notifying listeners of new frames
	 * @param handsToTrack How many hands should we track
	 */
	public void start(int handsToTrack) {
		if(handsToTrack <= 0)
			throw new IllegalArgumentException("handsToTrack must be > 0");

		if(!running) {
			running = true;
			this.handsToTrack = handsToTrack;
			tracker.startGestureDetection(gestureType);
			tracker.addNewFrameListener(this);
		}
	}

	/**
	 * Stops notifying listeners of new frames
	 */
	public void stop() {
		if(running) {
			tracker.removeNewFrameListener(this);
			tracker.stopGestureDetection(gestureType);
			handsToTrack = -1;
			trackingHands = 0;
			running = false;
		}
	}

	/**
	 * 
	 * @return true if the VirtualScreenManager has been correctly initialized, false otherwise
	 */
	public boolean isInitialized() {
		return initDone && initialized;
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
		stop();
		vscreen = null;
		if(lastFrame != null) {
			lastFrame.release();
			lastFrame = null;
		}
		tracker.destroy();
		//deregister all listeners
		listeners.clear();
		initialized = initDone = false;
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
				if(hd.isTracking()) {
					Point3D<Float> pos = hd.getPosition();
					hands.add(new HandData(hd.getId(), vscreen.get2DProjection(pos), vscreen.isTouching(pos)));
				}
			}

			hands = Collections.unmodifiableCollection(hands);
			//notify all listeners
			for(VirtualScreenListener l : listeners) {
				l.onNewFrame(hands);
			}
		}
	}

}
