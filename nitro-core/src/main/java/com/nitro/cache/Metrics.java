package com.nitro.cache;

import java.util.concurrent.atomic.LongAdder;

import jdk.internal.vm.annotation.Contended;

public class Metrics {
	
//	False sharing protected to avoid cache line read of both hits and misses together
	@Contended
	public final LongAdder hits = new LongAdder();
	
	@Contended
	public final LongAdder misses = new LongAdder();
}
