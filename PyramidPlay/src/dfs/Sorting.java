package dfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import application.Song;

public class Sorting {
	Map<Integer, List<String>> peer1Map = new TreeMap<Integer, List<String>>();
	Map<Integer, List<String>> peer2Map = new TreeMap<Integer, List<String>>();
	Map<Integer, List<String>> peer3Map = new TreeMap<Integer, List<String>>();
	
	private final int PEER_1_GUID = 10000;
	private final int PEER_2_GUID = 20000;
	private final int PEER_3_GUID = 30000;
	
	public File mapReduce (File file) {
		return null;
	}
	
	private File map(File file) {
		return null;
	}
	
	private File reduce(File file) {
		return null;
	}
	
	private void emitMap(Integer key, String value, Counter counter) {
		if (value.compareTo("A") >= 0 && value.compareTo("L") <= 0) {
			addToMap(peer1Map, key, value, counter);
		} else if (value.compareTo("M") >= 0 && value.compareTo("X") <=0) {
			addToMap(peer2Map, key, value, counter);
		} else {
			addToMap(peer3Map, key, value, counter);
		}
	}
	
	private void addToMap(Map<Integer, List<String>> map, Integer key, String value, Counter counter) {
		if (map.containsKey(key)) {
			map.get(key).add(value);
		} else {
			map.put(key, new ArrayList<String>(Arrays.asList(value)));
		}
		
		counter.decrement();
	}
	
	
}
