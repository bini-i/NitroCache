package com.nitro.cache;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class NitroCache<K, V> {
	
//	fixed capacity, to ensure contiguous memory layout
	private final int capacity;
	
//	TODO: Look into the use of plain array with manual implementation of volatile reads, and all the perks that come with AtomicReferenceArray
//	Atomic visibility to guarantee updates to an index is seen by other cores
	private final AtomicReferenceArray<StampedValue<V>> table;
	
	private final Metrics metrics = new Metrics();
	
	public NitroCache(int capacity) {
		this.capacity = capacity;
		this.table = new AtomicReferenceArray<>(capacity);
	}
	
	public void put(K key, V value) {
		
//		TODO: look into power of two trick to remove the use of modulo which consumes extra CPU cycles
		int index = (key.hashCode() & 0x7FFFFFFF) % capacity;                      
		
		StampedValue<V> oldEntry = table.get(index);
		
		while(true) {
			int nextStamp = oldEntry == null ? 0 : oldEntry.stamp() + 1;
			
			StampedValue<V> newTableEntry = new StampedValue<>(value, nextStamp);
			
			if(table.compareAndSet(index, oldEntry, newTableEntry)) {
				return;
			}
			
			Thread.onSpinWait();
		}
	}
	
	public V get(K key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % capacity;
		
		StampedValue<V> entry = table.get(index);
		
		if(entry != null) {
			metrics.hits.increment();     // stripe-bases counter
			return entry.value();
		}else {
			metrics.misses.increment();
			return null;
		}
	}
	
//	Specialized Atomic increment for our stress tests
	@SuppressWarnings("unchecked")
	public void incrementInteger(K key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % capacity;
		
		while(true) {
			StampedValue<V> oldEntry = table.get(index);
			
			Integer oldValue = (oldEntry == null) ? 0 : (Integer) oldEntry.value();
			
			Integer newValue = oldValue + 1;
			int nextStamp = (oldEntry == null) ? 0 : oldEntry.stamp() + 1;
			
			StampedValue<V> newEntry  = (StampedValue<V>) new StampedValue<>(newValue, nextStamp);
			
//			commit the whole calculation
			if(table.compareAndSet(index, oldEntry, newEntry)) {
				return;
			}
			
			Thread.onSpinWait();
		}
	}
}








