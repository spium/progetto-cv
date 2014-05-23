package it.polito.computervision.gestures;

import java.util.Collection;
import java.util.HashSet;

import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenListener;
import it.polito.computervision.virtualscreen.VirtualScreenManager;

/**
 * This singleton manages a set of {@link Gesture}s. Invokes on each {@link Gesture#updateState(Collection)} with the {@link HandData} of the current frame.
 * If a {@link Gesture} is in state IN_PROGRESS, then only that gesture will be updated, and all the others will be {@link Gesture#reset()}.
 * @author giovanni
 *
 */
public class GestureManager implements VirtualScreenListener {
	
	private static GestureManager instance = null;
	
	private Collection<Gesture> gestures;
	private Gesture gestureInProgress;
	
	private GestureManager() {
		gestures = new HashSet<Gesture>();
		gestureInProgress = null;
	}
	
	public static GestureManager getInstance() {
		if(instance == null)
			instance = new GestureManager();
		
		return instance;
	}

	@Override
	public void onNewFrame(Collection<HandData> hands) {
		if(gestureInProgress != null) {
			//we have a gesture in progress, only update this one
			GestureState state = gestureInProgress.updateState(hands);
			
			if(state != GestureState.IN_PROGRESS) {
				//remove it if it's no longer in progress
				gestureInProgress = null;
			}
		}
		else {
			//there's no gesture in progress, update them all
			boolean reset = false;
			for(Gesture g : gestures) {
				GestureState state = g.updateState(hands);
				
				if(state == GestureState.IN_PROGRESS) {
					//if a gesture is in progress, remember it, break out of the loop and reset all other gestures
					gestureInProgress = g;
					reset = true;
					break;
				}
			}
			
			//reset other gestures
			if(reset) {
				for(Gesture g : gestures)
					if(g != gestureInProgress)
						g.reset();
			}
			
		}
	}
	
	/**
	 * Starts updating gestures on each frame
	 */
	public void start() {
		VirtualScreenManager.getInstance().addVirtualScreenListener(this);
	}
	
	/**
	 * Stops updating gestures on each frame
	 */
	public void stop() {
		VirtualScreenManager.getInstance().removeVirtualScreenListener(this);
		for(Gesture g : gestures)
			g.reset();
	}
	
	/**
	 * Adds a {@link GestureListener} to all registered gestures.
	 * @param listener The listener to add
	 */
	public void addGestureListener(GestureListener listener) {
		for(Gesture g : gestures) {
			g.addGestureListener(listener);
		}
	}
	
	/**
	 * Removes the {@link GestureListener} from all registered gestures.
	 * @param listener The listener to remove
	 */
	public void removeGestureListener(GestureListener listener) {
		for(Gesture g : gestures) {
			g.removeGestureListener(listener);
		}
	}
	
	/**
	 * Registers a {@link Gesture} to be updated at each new frame
	 * @param gesture The {@link Gesture} to register
	 */
	public void registerGesture(Gesture gesture) {
		gestures.add(gesture);
	}
	
	/**
	 * Unregisters the {@link Gesture}. It will no longer be updated on new frames
	 * @param gesture The {@link Gesture} to unregister
	 */
	public void unregisterGesture(Gesture gesture) {
		gestures.remove(gesture);
		if(gestureInProgress == gesture)
			gestureInProgress = null;
	}
	
	/**
	 * Registers a collection of {@link Gesture}s
	 * @param gestures The {@link Gestures} to register
	 * @see #registerGesture(Gesture)
	 */
	public void registerGestures(Collection<Gesture> gestures) {
		this.gestures.addAll(gestures);
	}
	
	/**
	 * Unregisters a collection of {@link Gesture}s
	 * @param gestures The {@link Gesture}s to unregister
	 * @see #unregisterGesture(Gesture)
	 */
	public void unregisterGestures(Collection<Gesture> gestures) {
		this.gestures.removeAll(gestures);
		if(gestureInProgress != null && gestures.contains(gestureInProgress))
			gestureInProgress = null;
	}
	
	/**
	 * Unregisters all {@link Gesture}s
	 */
	public void unregisterAllGestures() {
		gestures.clear();
		gestureInProgress = null;
	}
	
	@Override
	protected void finalize() {
		stop();
		unregisterAllGestures();
	}

}
