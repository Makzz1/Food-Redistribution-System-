package com.foodredistribution.foodredistribution.enums;

public enum CancellationReason {
    RECEIVER_CANCELLED,    // receiver voluntarily backed out (ACTIVE)
    RECEIVER_NO_SHOW,      // donor waited 30+ min, receiver never arrived
    FOOD_UNAVAILABLE,      // food expired or no longer available (pre-claim delete only)
    FOOD_EXPIRED,          // scheduler auto-cancelled expired post
    ADMIN_RESOLVED         // admin cancelled a disputed claim via resolveDispute()
}
