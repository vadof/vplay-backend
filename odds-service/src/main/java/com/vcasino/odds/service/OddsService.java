package com.vcasino.odds.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class OddsService {

    protected final double MARGIN = 0.05;
    protected final double MIN_PROBABILITY = MARGIN;
    protected final double MAX_PROBABILITY = 1 - MARGIN;
    protected final BigDecimal MIN_ODDS = new BigDecimal("1.05");

    public BigDecimal calculateOddsFromProbability(double prob) {
        prob = Math.min(Math.max(prob, MIN_PROBABILITY), MAX_PROBABILITY);
        return applyMarginToOdds(1 / prob);
    }

    public BigDecimal applyMarginToOdds(double odds) {
        double oddsWithMargin = odds * (1 - MARGIN);
        return new BigDecimal(oddsWithMargin).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isOddsPassesThreshold(BigDecimal odds) {
        return odds.compareTo(MIN_ODDS) >= 0;
    }

}
