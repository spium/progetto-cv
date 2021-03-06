package it.polito.computervision.virtualscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.opencv.core.Size;

import com.primesense.nite.HandTracker;
import com.primesense.nite.HandTracker.NewFrameListener;
import com.primesense.nite.GestureData;
import com.primesense.nite.GestureType;
import com.primesense.nite.HandTrackerFrameRef;
import com.primesense.nite.Point3D;

/**
 * Singleton that manages the virtual screen. It needs to be initialized with a {@link VirtualScreen} and a {@link VirtualScreenInitializer}.
 * It updates {@link VirtualScreenListener}s on each frame with the current {@link HandData}.
 * @author giovanni
 *
 */
public class VirtualScreenManager implements NewFrameListener {

	private static final GestureType GESTURE_TYPE = GestureType.HAND_RAISE;
	public static final float PROJECTED_POSITION_MULTIPLIER = 2.f;

	private static VirtualScreenManager instance = null;

	private VirtualScreen vscreen;
	private HandTracker tracker;
	private HandTrackerFrameRef lastFrame;
	private Collection<VirtualScreenListener> listeners;
	private boolean initialized, initDone, running, detecting;
	private int handsToTrack, handsTracked;

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
		handsTracked = 0;
		detecting = false;
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

		//check for newly found hands
		for(GestureData gd : lastFrame.getGestures()) {
			if(handsTracked < handsToTrack && gd.isComplete()) {
				try {
					//start the tracker
					tracker.startHandTracking(gd.getCurrentPosition());
					++handsTracked;
				}
				catch(Exception e) {
					//do nothing... sometimes the native call will return ERROR, resulting in a runtime exception
				}
			}
		}
		
		for(com.primesense.nite.HandData hd : lastFrame.getHands()) {
			if(hd.isLost()) {
				--handsTracked;
			}
		}
		
		if(!detecting && handsTracked < handsToTrack) {
			tracker.startGestureDetection(GESTURE_TYPE);
			detecting = true;
		}
		else if(handsTracked == handsToTrack){
			tracker.stopGestureDetection(GESTURE_TYPE);
			detecting = false;
		}

		if(isInitialized())
			notifyListeners();
	}

	/**
	 * Initializes the {@link VirtualScreen} with the given {@link VirtualScreenInitializer}
	 * @param vscreen The {@link VirtualScreen} to initialize
	 * @param vscreenInit The {@link VirtualScreenInitializer} to use
	 * @return true if the initialization was successful, false otherwise
	 */
	public boolean initialize(VirtualScreen vscreen, VirtualScreenInitializer vscreenInit) {
		if(!running)
			throw new IllegalStateException("VirtualScreenManager must be started before initialization");

		this.vscreen = vscreen;
		
		final VirtualScreenManager vsm = this;
		boolean sync = vscreenInit.initialize(vscreen, tracker, new VirtualScreenInitializer.InitializerCallback() {

			@Override
			public void initializationComplete(boolean ok) {
				synchronized(vsm) {
					vsm.initDone = true;
					vsm.initialized = ok;
					vsm.notify();
				}
			}
		});

		if(!sync) {

			//wait until the init procedure is done
			synchronized(this) {
				while(!initDone) {
					try {
						wait();
					} catch (InterruptedException e) {}
				}
			}

		}
		else {
			initialized = initDone = true;
		}

		return initialized;
	}

	public synchronized Size getFrameSize() {
		return lastFrame != null ? new Size(lastFrame.getDepthFrame().getWidth()*PROJECTED_POSITION_MULTIPLIER, lastFrame.getDepthFrame().getHeight()*PROJECTED_POSITION_MULTIPLIER) : new Size(0,0);
	}
	
	/**
	 * Starts notifying listeners of new frames
	 * @param handsToTrack How many hands should we track
	 */
	public synchronized void start(int handsToTrack) {
		if(handsToTrack <= 0)
			throw new IllegalArgumentException("handsToTrack must be > 0");

		if(!running) {
			running = true;
			this.handsToTrack = handsToTrack;
			handsTracked = 0;
			tracker.startGestureDetection(GESTURE_TYPE);
			detecting = true;
			tracker.addNewFrameListener(this);
		}
	}

	/**
	 * Stops notifying listeners of new frames
	 */
	public synchronized void stop() {
		if(running) {
			tracker.removeNewFrameListener(this);
			if(detecting) {
				tracker.stopGestureDetection(GESTURE_TYPE);
				detecting = false;
			}
			handsToTrack = -1;
			handsTracked = 0;
			running = false;
		}
	}

	/**
	 * 
	 * @return true if the VirtualScreenManager has been correctly initialized, false otherwise
	 */
	public synchronized boolean isInitialized() {
		return initDone && initialized;
	}

	/**
	 * 
	 * @return the {@link VirtualScreen} this manager is using, or null if it hasn't been initialized yet
	 */
	public synchronized VirtualScreen getVirtualScreen() {
		return vscreen;
	}

	/**
	 * Disposes of all resources this manager is using. After this call, the manager will be in its default uninitialized state.
	 * It is safe to call again {@link VirtualScreenManager#initialize(VirtualScreen, VirtualScreenInitializer)} after this call.
	 * This method is automatically called in the destructor. It is safe to invoke multiple times.
	 */
	public synchronized void destroy() {
		stop();
		vscreen = null;
		if(lastFrame != null) {
			lastFrame.release();
			lastFrame = null;
		}
		tracker.destroy();
		tracker = null;
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
	public synchronized void addVirtualScreenListener(VirtualScreenListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a {@link VirtualScreenListener}
	 * @param listener the {@link VirtualScreenListener}
	 */
	public synchronized void removeVirtualScreenListener(VirtualScreenListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies the {@link VirtualScreenListener}s that a new frame is available
	 */
	private void notifyListeners() {
		if(lastFrame != null && vscreen != null) {
			//get the original NiTE HandData collection
			List<com.primesense.nite.HandData> handsOrig = lastFrame.getHands();
			//convert to our own HandData
			List<HandData> hands = new ArrayList<HandData>(handsOrig.size());
			for(com.primesense.nite.HandData hd : handsOrig) {
				if(hd.isTracking()) {
					Point3D<Float> pos = hd.getPosition();
					com.primesense.nite.Point2D<Float> depthPos = tracker.convertHandCoordinatesToDepth(pos);
					//mirror on the X axis
					org.openni.Point2D<Float> projPos = new org.openni.Point2D<Float>((lastFrame.getDepthFrame().getWidth() - depthPos.getX())*PROJECTED_POSITION_MULTIPLIER, depthPos.getY()*PROJECTED_POSITION_MULTIPLIER);
					hands.add(new HandData(hd.getId(), vscreen.get2DProjection(pos), projPos, vscreen.isTouching(pos)));
				}
			}

			hands = Collections.unmodifiableList(hands);
			//notify all listeners
			for(VirtualScreenListener l : listeners) {
				l.onNewFrame(hands);
			}
		}
	}

}
