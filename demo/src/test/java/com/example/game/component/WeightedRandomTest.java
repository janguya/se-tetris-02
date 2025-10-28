package com.example.game.component;

import com.example.game.blocks.Block;
import com.example.settings.GameSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeightedRandomTest {

    // Number of samples (>= 1000 as required). Use 5000 for better statistical stability.
    private static final int SAMPLES = 5000;
    private static final double TOLERANCE = 0.05; // +/- 5%

    boolean log = false;

    @Test
    @DisplayName("Weighted distribution matches expected probabilities for EASY/ MEDIUM/ HARD")
    void testWeightedDistributionByDifficulty() {
        // Test each difficulty (use global GameSettings)
        for (GameSettings.Difficulty diff : GameSettings.Difficulty.values()) {
            // set global difficulty
            GameSettings.getInstance().setDifficulty(diff);
            GameLogic gl = new GameLogic();

            // Count occurrences by block simple class name
            Map<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < SAMPLES; i++) {
                Block b = gl.getRandomBlock();
                String name = b.getClass().getSimpleName();
                counts.put(name, counts.getOrDefault(name, 0) + 1);
            }

            // Compute expected weights as in GameLogic.getRandomBlock
            double baseWeight = 1.0;
            double iWeight = baseWeight;
            if (diff == GameSettings.Difficulty.EASY) iWeight *= 1.2;
            else if (diff == GameSettings.Difficulty.HARD) iWeight *= 0.8;
            double[] weights = new double[] { iWeight, baseWeight, baseWeight, baseWeight, baseWeight, baseWeight, baseWeight };
            double sum = 0.0;
            for (double w : weights) sum += w;

            String[] blockOrder = new String[] { "IBlock", "JBlock", "LBlock", "ZBlock", "SBlock", "TBlock", "OBlock" };

            // Print summary to help debug/test results
            if (log) {
                System.out.println("\n=== WeightedRandomTest: Difficulty=" + diff + " (samples=" + SAMPLES + ") ===");
                System.out.println("Block    Count    Observed%    Expected%    Diff%");
            }

            for (int i = 0; i < blockOrder.length; i++) {
                String name = blockOrder[i];
                int cnt = counts.getOrDefault(name, 0);
                double observed = (double) cnt / SAMPLES;
                double expected = weights[i] / sum;
                double diffAbs = Math.abs(observed - expected);

                if (log) {
                    System.out.printf("%-7s %7d    %7.3f%%    %7.3f%%    %7.3f%%%n",
                    name, cnt, observed * 100.0, expected * 100.0, diffAbs * 100.0);
                }

                String msg = String.format("Difficulty=%s block=%s observed=%.4f expected=%.4f diff=%.4f (count=%d)", diff, name, observed, expected, diffAbs, cnt);
                assertTrue(diffAbs <= TOLERANCE, msg);
            }
        }
    }
}
