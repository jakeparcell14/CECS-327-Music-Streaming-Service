package dfs;

import java.util.Set;

/**
 * Class to keep track of progress in map and reduce phases
 */
public class Counter {
	private long count = 0;
	
	//set containing chunks
	private Set<Integer> keys;
	
	public Counter() {
		
	}
	
	public void add(int guid) {
		keys.add(guid);
	}
	
	/**
	 * Checks when the phase is complete.
	 * Phase is complete when there are no chunks that need to be read
	 * and when the count is 0
	 * @return
	 */
	public boolean hasCompleted() {
		if (count == 0 && keys.isEmpty()) { return true; }
		
		return false;
	}
	
	/**
	 * Removes the guid of chunk being used from the set
	 * and increments count by number of objects (lines) in the chunk
	 * @param guid
	 * @param lines
	 */
	public void increment(int guid, int lines) {
		keys.remove(guid);
		count += lines;
	}
	
	/**
	 * Decrements count by 1
	 */
	public void decrement() {
		count--;
	}
}