/**
 * 
 */
package it.polito.computervision.virtualscreen.impl;

import org.opencv.core.Size;

import com.primesense.nite.GestureData;
import com.primesense.nite.GestureType;
import com.primesense.nite.HandTracker;
import com.primesense.nite.HandTrackerFrameRef;

import it.polito.computervision.virtualscreen.AbstractVirtualScreenInitializer;
import it.polito.computervision.virtualscreen.VirtualScreen;
import it.polito.computervision.virtualscreen.VirtualScreenInitializer;

/**
 * A simple {@link VirtualScreenInitializer} that initializes the {@link VirtualScreen} in a fixed position.
 * @author giovanni
 *
 */
public class StaticVirtualScreenInitializer extends AbstractVirtualScreenInitializer implements HandTracker.NewFrameListener {

	private Size size;
	private float depth;
	private int handsToTrack;
	
	private HandTrackerFrameRef lastFrame;
	private int trackingHands;

	/**
	 * 
	 * @param size The size of the {@link VirtualScreen}
	 * @param depth The depth of the {@link VirtualScreen}
	 * @param handsToTrack How many hands to track
	 */
	public StaticVirtualScreenInitializer(Size size, float depth, int handsToTrack) {
		super(handsToTrack);
		this.size = size;
		this.depth = depth;
		this.handsToTrack = handsToTrack;
		lastFrame = null;
		trackingHands = 0;
	}

	@Override
	public synchronized void onNewFrame(HandTracker handTracker) {
		if(lastFrame != null) {
			lastFrame.release();
			lastFrame = null;
		}

		lastFrame = handTracker.readFrame();

		for(GestureData gd : lastFrame.getGestures()) {
			if(gd.isComplete()) {
				handTracker.stopGestureDetection(gd.getType());
				//start the tracker
				handTracker.startHandTracking(gd.getCurrentPosition());
				++trackingHands;
			}
		}

		if(trackingHands == handsToTrack) {
			//we're done, stop receiving new frame callbacks
			for(HandTracker t : trackers)
				t.removeNewFrameListener(this);
			
			//notify upstairs
			super.initializationComplete(true);
		}
		else {
			//initialize the next tracker
			trackers.get(trackingHands).startGestureDetection(GestureType.CLICK);
		}
	}

	@Override
	protected void startInitialization() {
		//statically initialize screen params
		vscreen.setDepth(depth);
		vscreen.setSize(size);
		
		//create all trackers needed
		for(int i = 0; i < handsToTrack; ++i) {
			HandTracker t = HandTracker.create();
			//TODO again, does this work ok with multiple trackers??
			t.addNewFrameListener(this);
			trackers.add(t);
		}
		
		//init one tracker at a time
		if(trackers.size() > 0)
			trackers.get(0).startGestureDetection(GestureType.CLICK);
	}
}
