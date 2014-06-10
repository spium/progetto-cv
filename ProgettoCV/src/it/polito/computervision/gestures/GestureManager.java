package it.polito.computervision.gestures;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.virtualscreen.HandData;
import it.polito.computervision.virtualscreen.VirtualScreenListener;
import it.polito.computervision.virtualscreen.VirtualScreenManager;

/**
 * This singleton manages a set of {@link Gesture}s. Invokes on each {@link Gesture#updateState(Collection,Collection)} with the {@link HandData} of the current frame.
 * If a {@link Gesture} is in state IN_PROGRESS, then only that gesture will be updated, and all the others will be {@link Gesture#reset()}.
 * It also fires {@link GestureListener} callbacks based on the {@link GestureState} a {@link Gesture} is in after the update.
 * @author giovanni
 *
 */
public class GestureManager implements VirtualScreenListener {
	
	private static GestureManager instance = null;
	
	private Collection<Gesture> gestures;
	private Gesture gestureInProgress;
	
	private Collection<GestureListener> listeners;
	
	private GestureManager() {
		gestures = new HashSet<Gesture>();
		gestureInProgress = null;
		listeners = new HashSet<GestureListener>();
	}
	
	public static GestureManager getInstance() {
		if(instance == null)
			instance = new GestureManager();
		
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onNewFrame(Collection<HandData> hands) {
		ArrayList<HandData> gestureHands = new ArrayList<HandData>();
		if(gestureInProgress != null) {
			//we have a gesture in progress, only update this one
			GestureState oldState = gestureInProgress.getCurrentState();
			GestureState newState = gestureInProgress.updateState(hands, gestureHands);
			if(oldState != newState)
				System.out.println(gestureInProgress.getName() + ": " + oldState.toString() + " => " + newState.toString());
			notifyListeners(Collections.unmodifiableCollection(gestureHands), gestureInProgress, oldState);
			
			if(newState != GestureState.IN_PROGRESS && newState != GestureState.COMPLETED) {
				//remove it if it's no longer in progress or complete
				gestureInProgress = null;
			}
		}
		else {
			//there's no gesture in progress, update them all
			boolean reset = false;
			for(Gesture g : gestures) {
				GestureState oldState = g.getCurrentState();
				GestureState newState = g.updateState(hands, gestureHands);
				if(oldState != newState)
					System.out.println(g.getName() + ": " + oldState.toString() + " -> " + newState.toString());
				notifyListeners(Collections.unmodifiableCollection(gestureHands), g, oldState);
				
				if(newState == GestureState.IN_PROGRESS) {
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
		listeners.add(listener);
	}
	
	/**
	 * Removes the {@link GestureListener} from all registered gestures.
	 * @param listener The listener to remove
	 */
	public void removeGestureListener(GestureListener listener) {
		listeners.remove(listener);
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
	 * @param gestures The {@link Gesture}s to register
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
		listeners.clear();
	}
	
	/**
	 * Notifies listeners if necessary, based on the {@link GestureState} transition that occurred.
	 * Live {@link Gesture}s are notified of all events. Non-live gestures are only notified of gesture completion.
	 * @param hands The hands the gesture is tracking.
	 * @param gesture The gesture that may be firing the event.
	 * @param oldState The old {@link GestureState} the gesture was in.
	 */
	private void notifyListeners(Collection<HandData> hands, Gesture gesture, GestureState oldState) {
		GestureState currentState = gesture.getCurrentState();
		
		if(currentState == GestureState.IN_PROGRESS) {
			if(oldState == GestureState.IN_PROGRESS && gesture.isLive())
				notifyGestureInProgress(hands, gesture);
			else if(oldState == GestureState.POSSIBLE_DETECTION && gesture.isLive())
				notifyGestureStarted(hands, gesture);
			else if(gesture.isLive())
				throw new IllegalStateException("Illegal state transition from: " + oldState + " to: " + currentState);
		}
		else if(currentState == GestureState.COMPLETED) {
			if(oldState == GestureState.IN_PROGRESS)
				notifyGestureCompleted(hands, gesture);
			else if(oldState != GestureState.COMPLETED)
				throw new IllegalStateException("Illegal state transition from: " + oldState + " to: " + currentState);
		}
		//else we don't care (no notification when entering other states)
	}
	
	/**
	 * Notifies listeners that the gesture is started (i.e. invokes {@link GestureListener#onGestureStarted(GestureData)})
	 * @param hands The {@link HandData} of the current frame
	 */
	private void notifyGestureStarted(Collection<HandData> hands, Gesture gesture) {
		GestureData gd = new GestureData(gesture.getName(), gesture.getCurrentState(), hands, gesture.getData(), gesture.isLive());
		for(GestureListener l : listeners) {
			l.onGestureStarted(gd);
		}
	}
	
	/**
	 * Notifies listeners that the gesture is in progress (i.e. invokes {@link GestureListener#onGestureInProgress(GestureData)})
	 * @param hands The {@link HandData} of the current frame
	 */
	private void notifyGestureInProgress(Collection<HandData> hands, Gesture gesture) {
		GestureData gd = new GestureData(gesture.getName(), gesture.getCurrentState(), hands, gesture.getData(), gesture.isLive());
		for(GestureListener l : listeners) {
			l.onGestureInProgress(gd);
		}
	}
	
	/**
	 * Notifies listeners that the gesture has completed (i.e. invokes {@link GestureListener#onGestureCompleted(GestureData)})
	 * @param hands The {@link HandData} of the current frame
	 */
	private void notifyGestureCompleted(Collection<HandData> hands, Gesture gesture) {
		GestureData gd = new GestureData(gesture.getName(), gesture.getCurrentState(), hands, gesture.getData(), gesture.isLive());
		for(GestureListener l : listeners) {
			l.onGestureCompleted(gd);
		}
	}

}
