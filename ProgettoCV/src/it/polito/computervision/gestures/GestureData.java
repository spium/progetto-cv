package it.polito.computervision.gestures;

import it.polito.computervision.virtualscreen.HandData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents data about a 2D gesture
 * @author Giovanni Piumatti
 *
 */
public class GestureData {
	
	private String name;
	private GestureState state;
	private List<HandData> hands;
	private boolean live;
	private Map<String, Object> data;
	
	/**
	 * @param name The unique name of this gesture
	 * @param state The state this gesture is in during the current frame
	 * @param hands The hands this gesture is tracking
	 * @param data A map of arbitrary gesture-specific data
	 * @param live Whether the gesture is live or not
	 */
	public GestureData(String name, GestureState state, List<HandData> hands, Map<String, Object> data, boolean live) {
		this.name = name;
		this.state = state;
		this.hands = hands;
		this.data = data;
		this.live = live;
	}

	/**
	 * @return the unique name of this gesture
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the state this gesture is in during the current frame
	 */
	public GestureState getState() {
		return state;
	}

	/**
	 * @return the hands this gesture is tracking
	 */
	public List<HandData> getHands() {
		return hands;
	}
	
	/**
	 * Returns (already cast to the type T) the data (if any) with the given key.
	 * @param key The key of the data to retrieve
	 * @return The data cast to the type T
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		return (T) data.get(key);
	}
	
	/**
	 * Checks if there is data with the given key.
	 * @param key The key to check
	 * @return true if there is data associated to the key, false otherwise
	 */
	public boolean hasData(String key) {
		return data.containsKey(key);
	}
	
	/**
	 * @return A set of all keys that have data bound to them.
	 */
	public Set<String> getDataKeys() {
		return data.keySet();
	}

	/**
	 * @return true if the gesture is a live gesture, false otherwise
	 */
	public boolean isLive() {
		return live;
	}

	/**
	 * 
	 * @return true if the gesture is in progress, false otherwise
	 */
	public boolean isInProgress() {
		return state == GestureState.IN_PROGRESS;
	}
	
	/**
	 * 
	 * @return true if the gesture has completed, false otherwise
	 */
	public boolean isComplete() {
		return state == GestureState.COMPLETED;
	}
	

}
