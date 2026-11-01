package com.lapso.gdtracker.service.sync;

/**
 * Correspondencia entre el tier de GDDL (1-39) y la categoria de demon, segun
 * https://gdladder.com/references: Easy 1-5, Medium 6-10, Hard 11-15, Insane 16-20, Extreme 21+.
 */
final class GddlTiers {

    private GddlTiers() {
    }

    /** Devuelve algo como "Extreme Demon (Tier 24)", o null si el valor no es un tier valido (< 1). */
    static String describe(double rawTier) {
        int tier = (int) Math.round(rawTier);
        if (tier < 1) {
            return null;
        }
        return categoryFor(tier) + " (Tier " + tier + ")";
    }

    static String categoryFor(int tier) {
        if (tier <= 5) return "Easy Demon";
        if (tier <= 10) return "Medium Demon";
        if (tier <= 15) return "Hard Demon";
        if (tier <= 20) return "Insane Demon";
        return "Extreme Demon";
    }
}
