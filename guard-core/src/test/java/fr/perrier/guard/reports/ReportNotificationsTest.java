package fr.perrier.guard.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Toggle de notifications des staff : état persisté dans un fichier local (fonctionne sans DB),
 * et court-circuit pour les staff qui n'ont pas la permission de se désabonner.
 */
public class ReportNotificationsTest {

    @TempDir
    Path temp;

    private UUID player;

    @BeforeEach
    public void reset() {
        player = UUID.randomUUID();
        ReportNotifications.resetForTests(temp.resolve("notifications.yml").toFile());
    }

    @Test
    public void defaultIsEnabled() {
        assertTrue(ReportNotifications.wantsNotifications(player, false));
    }

    @Test
    public void disablingSticksAndSurvivesReload() {
        ReportNotifications.setEnabled(player, false);
        assertFalse(ReportNotifications.wantsNotifications(player, false));

        // nouvel objet de persistance sur le même fichier = redémarrage du serveur
        ReportNotifications.resetForTests(new File(temp.toFile(), "notifications.yml"));
        assertFalse(ReportNotifications.wantsNotifications(player, false));
    }

    @Test
    public void reenablingRestores() {
        ReportNotifications.setEnabled(player, false);
        ReportNotifications.setEnabled(player, true);
        assertTrue(ReportNotifications.wantsNotifications(player, false));
    }

    @Test
    public void playersWithoutNotifyPermAlwaysReceive() {
        // un staff qui n'a PAS guard.reports.notify ne peut pas se couper du flux
        ReportNotifications.setEnabled(player, false);
        assertTrue(ReportNotifications.wantsNotifications(player, true));
    }

    @Test
    public void otherPlayersUnaffected() {
        UUID other = UUID.randomUUID();
        ReportNotifications.setEnabled(player, false);
        assertTrue(ReportNotifications.wantsNotifications(other, false));
    }
}
