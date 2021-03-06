/**
 * 
 */
package it.polito.computervision.actions;

import java.util.HashMap;

import java.util.Map;

import it.polito.computervision.gestures.Gesture;
import it.polito.computervision.gestures.GestureData;
import it.polito.computervision.gestures.GestureListener;
import it.polito.computervision.gestures.GestureManager;

/**
 * Singleton that acts as a dispatcher for {@link Gesture} events.
 * Only one {@link GestureListener} can be bound to a specific {@link Gesture} at any time.
 * 
 * @author Giovanni Piumatti
 *
 */
public class ActionManager implements GestureListener {

	private static ActionManager instance = null;
	
	private Map<String, GestureListener> bindings;
	
	private ActionManager() {
		bindings = new HashMap<String, GestureListener>();
	}
	
	public static ActionManager getInstance() {
		if(instance == null)
			instance = new ActionManager();
		
		return instance;
	}
	
	/**
	 * Starts listening to gesture events and dispatching them to the bound {@link GestureListener}s
	 */
	public void start() {
		GestureManager.getInstance().addGestureListener(this);
	}
	
	/**
	 * Stops dispatching events to the {@link GestureListener}s
	 */
	public void stop() {
		GestureManager.getInstance().removeGestureListener(this);
	}
	
	/**
	 * Binds a {@link GestureListener} to a specific {@link Gesture} referenced by its unique name (i.e. {@link Gesture#getName()}).
	 * If a listener is already bound to this {@link Gesture} it will be overridden.
	 * @param gestureName The name of the {@link Gesture} to bind to.
	 * @param listener The {@link GestureListener} to bind.
	 */
	public synchronized void bind(String gestureName, GestureListener listener) {
		bindings.put(gestureName, listener);
	}
	
	/**
	 * Unbinds the {@link GestureListener} bound to this {@link Gesture}.
	 * @param gestureName The name of the {@link Gesture} to unbind from.
	 */
	public synchronized void unbind(String gestureName) {
		bindings.remove(gestureName);
	}
	
	/**
	 * Checks if the {@link Gesture} with the given name is bound to a {@link GestureListener}
	 * @param gestureName The name of the {@link Gesture}
	 * @return true if a gesture with such name is bound to a listener, false otherwise
	 */
	public synchronized boolean isBound(String gestureName) {
		return bindings.containsKey(gestureName);
	}
	
	@Override
	protected void finalize() {
		stop();
		bindings.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onGestureStarted(GestureData gesture) {
		GestureListener l = bindings.get(gesture.getName());
		if(l != null)
			l.onGestureStarted(gesture);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onGestureInProgress(GestureData gesture) {
		GestureListener l = bindings.get(gesture.getName());
		if(l != null)
			l.onGestureInProgress(gesture);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onGestureCompleted(GestureData gesture) {
		GestureListener l = bindings.get(gesture.getName());
		if(l != null)
			l.onGestureCompleted(gesture);
	}

}
