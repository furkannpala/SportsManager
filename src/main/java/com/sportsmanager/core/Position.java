package com.sportsmanager.core;

import java.util.Map;

public interface Position {
    String getName();
    Map<String, Double> getWeightedAttributes();
}
