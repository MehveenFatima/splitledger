package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Spending category used for reports. */
public enum Category {
    FOOD,
    TRAVEL,
    RENT,
    UTILITIES,
    SHOPPING,
    ENTERTAINMENT,
    OTHER;
}
