package it.polito.computervision.gestures;

/**
 * A convenience class providing an empty implementation of the {@link GestureListener} interface.
 * @author Giovanni Piumatti
 *
 */
public class GestureListenerAdapter implements GestureListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onGestureStarted(GestureData gesture) { }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onGestureInProgress(GestureData gesture) { }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onGestureCompleted(GestureData gesture) { }

}
