package com.nitro.cache;

public record StampedValue<V>(V value, int stamp) {}
