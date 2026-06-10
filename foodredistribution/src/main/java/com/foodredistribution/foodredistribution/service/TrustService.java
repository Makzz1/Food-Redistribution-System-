package com.foodredistribution.foodredistribution.service;

import org.springframework.stereotype.Service;

import com.foodredistribution.foodredistribution.entity.User;

/**
 * Calculates a trust score (0–100) on-the-fly from raw metrics on User.
 * Trust score is NOT persisted — it is always computed fresh.
 *
 * ══════════════════════════════════════════════════════════════════
 *  FORMULA
 * ══════════════════════════════════════════════════════════════════
 *
 *  Start:     50 points  (neutral baseline for new users)
 *
 *  POSITIVE (earned by completing transactions):
 *    + successfulDonations  × 2   (capped contribution: max +40)
 *    + successfulPickups    × 2   (capped contribution: max +40)
 *    → Combined positive cap: +40 (so best possible = 90)
 *
 *  NEGATIVE (trust violations):
 *    − reportCount          × 15  (most severe — admin-reviewed reports)
 *    − noShowCount          × 8   (medium — receiver didn't show up)
 *    − disputeCount         × 4   (lightest — single accused dispute)
 *
 *  Final score is clamped to [0, 100].
 *
 * ══════════════════════════════════════════════════════════════════
 *  HOW EACH ACTOR AFFECTS SCORES
 * ══════════════════════════════════════════════════════════════════
 *
 *  DONOR:
 *    ✅  +2 per completed donation (successfulDonations++)
 *    ⚠️  -4 when receiver disputes after donor-confirm (accusedDisputeCount++)
 *    ⚠️  -15 when admin-reviewed report is filed against them (reportCount++)
 *
 *  RECEIVER:
 *    ✅  +2 per completed pickup (successfulPickups++)
 *    ⚠️  -8 for every no-show (noShowCount++)
 *    ⚠️  -4 when donor disputes receiver behaviour (accusedDisputeCount++)
 *    ⚠️  -15 when admin-reviewed report is filed against them (reportCount++)
 *
 *  NOTE: disputeCount is only incremented for the ACCUSED party,
 *        not for the person who raised the dispute. Raising a dispute
 *        in good faith does not hurt your score.
 *
 * ══════════════════════════════════════════════════════════════════
 *  TRUST TIERS
 * ══════════════════════════════════════════════════════════════════
 *
 *   90–100 : ⭐ Trusted      (high-volume, no violations)
 *   70–89  : ✅ Good         (some activity, clean history)
 *   50–69  : 🆕 New / Neutral (new account or some disputes)
 *   30–49  : ⚠️ Caution      (multiple disputes or no-shows)
 *    0–29  : 🔴 Risky        (reports filed, consider flagging)
 *
 * ══════════════════════════════════════════════════════════════════
 */
@Service
public class TrustService {

    private static final double BASE_SCORE            = 50.0;
    private static final double POSITIVE_WEIGHT       =  2.0;  // per donation/pickup
    private static final double POSITIVE_CAP          = 40.0;  // max bonus from activity
    private static final double REPORT_PENALTY        = 15.0;  // admin-reviewed report
    private static final double NO_SHOW_PENALTY       =  8.0;  // receiver no-show
    private static final double DISPUTE_PENALTY       =  4.0;  // accused in a dispute

    public Double calculateTrustScore(User user) {

        long reports   = user.getReportCount()         != null ? user.getReportCount()         : 0L;
        long noShows   = user.getNoShowCount()          != null ? user.getNoShowCount()         : 0L;
        long disputes  = user.getDisputeCount()         != null ? user.getDisputeCount()        : 0L;
        long donations = user.getSuccessfulDonations()  != null ? user.getSuccessfulDonations() : 0L;
        long pickups   = user.getSuccessfulPickups()    != null ? user.getSuccessfulPickups()   : 0L;

        // Positive contribution — capped so it can never exceed +40
        double positiveBonus = Math.min(
                (donations + pickups) * POSITIVE_WEIGHT,
                POSITIVE_CAP
        );

        // Negative deductions — uncapped (bad actors can reach 0)
        double penalty = (reports  * REPORT_PENALTY)
                       + (noShows  * NO_SHOW_PENALTY)
                       + (disputes * DISPUTE_PENALTY);

        double score = BASE_SCORE + positiveBonus - penalty;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Returns a human-readable trust tier label for this score.
     */
    public String getTrustTier(double score) {
        if (score >= 90) return "TRUSTED";
        if (score >= 70) return "GOOD";
        if (score >= 50) return "NEUTRAL";
        if (score >= 30) return "CAUTION";
        return "RISKY";
    }
}
