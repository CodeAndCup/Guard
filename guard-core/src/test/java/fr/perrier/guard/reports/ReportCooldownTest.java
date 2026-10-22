package fr.perrier.guard.reports;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cooldown /report : pur, horloge passée en paramètre. */
public class ReportCooldownTest {

    private static final UUID U = UUID.fromString("069a79c4-ed6e-4a33-8c24-d7283a2bcb21");

    @Test
    public void neverReportedMeansNoCooldown() {
        UUID fresh = UUID.randomUUID();
        assertEquals(0, ReportCooldown.cooldownLeft(fresh, 10_000, 60_000));
    }

    @Test
    public void insideCooldownShowsRemaining() {
        ReportCooldown.remember(U, 1_000);
        long left = ReportCooldown.cooldownLeft(U, 2_000, 60_000);
        assertEquals(59_000, left);
    }

    @Test
    public void cooldownExpiredAndPerReporter() {
        UUID other = UUID.fromString("4419f5a1-6e46-45a2-9e05-3e3733bb0b3f");
        ReportCooldown.remember(U, 1_000);
        ReportCooldown.remember(other, 2_000);
        assertEquals(0, ReportCooldown.cooldownLeft(U, 61_001, 60_000));
        assertTrue(ReportCooldown.cooldownLeft(other, 61_001, 60_000) > 0);
    }
}
