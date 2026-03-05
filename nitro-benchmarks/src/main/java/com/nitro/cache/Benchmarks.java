/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nitro.cache;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Nitrocache
 * This will measure "Throughput" (How many operations per millisecond)
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)         // The cache is shared across all threads
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmarks {

    private NitroCache<String, Integer> cache;
    private static final String SHARED_KEY = "global_counter";

    @Setup
    public void setup(){
        cache = new NitroCache<>(1024);
        cache.put(SHARED_KEY, 0);         // pre-fill the key so we aren't measuring first null check overhead
    }

    /**
     * Scenario 1: High Contention
     */
    @org.openjdk.jmh.annotations.Benchmark
    @Threads(8)
    public void testHighContentionIncrement() {
        cache.incrementInteger(SHARED_KEY);
    }

    /**
     * Scenario 2 : Zero Contention
     */
    @org.openjdk.jmh.annotations.Benchmark
    @Threads(8)
    public void testZeroContentionIncrement(ThreadState state){
        cache.incrementInteger("Key_" + state.threadId);
    }

    @State(Scope.Thread)
    public static class ThreadState{
        long threadId = Thread.currentThread().getId();
    }

}
