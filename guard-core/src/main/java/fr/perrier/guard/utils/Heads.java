package fr.perrier.guard.utils;

import com.cryptomorin.xseries.*;

import java.lang.Class;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

/**
 * Utilitaires de têtes de joueur (pseudo -> ItemStack), SANS aucune requête Mojang et sans
 * texture custom : compatible serveur online comme crack.
 *
 * NOTE : les anciennes textures custom (flèches de pagination...) ont été retirées — elles
 * dépendaient d'authlib/PropertySet, dont les signatures changent à chaque version de Paper
 * (NoSuchMethodError/NoSuchMethodException). Les items vanilla (ARROW...) les remplacent.
 */
public final class Heads {

    private Heads() {
    }

    /**
     * Pur (testé) : UUID offline déterministe d'un pseudo — algorithme Mojang
     * {@code UUID.nameUUIDFromBytes("OfflinePlayer:" + name)} : même UUID en ligne et hors
     * ligne, sur serveur online comme crack (aucune requête réseau).
     */
    public static java.util.UUID offlineUuid(String name) {
        return java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Profil d'un pseudo SANS requête Mojang (compatible serveur online ET crack) :
     * joueur connecté → son profil déjà résolu ; hors ligne → profil local à UUID offline
     * déterministe. Null si l'API profile n'existe pas (1.8-1.12) — l'appelant retombe alors
     * sur {@code SkullMeta.setOwner(String)}.
     */
    public static Object ownerProfile(String name) {
        if (name == null || name.isEmpty()) {
            name = "Guard";
        }
        org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getPlayerProfile(); // déjà complet, zéro réseau
        }
        try {
            return org.bukkit.Bukkit.getServer().getClass()
                    .getMethod("createProfile", java.util.UUID.class, String.class)
                    .invoke(org.bukkit.Bukkit.getServer(), offlineUuid(name), name);
        } catch (Exception e) {
            return null; // API profile indisponible : l'appelant prend le chemin legacy
        }
    }

    /**
     * Tête d'un joueur SANS requête Mojang (compatible serveur online ET crack).
     * En cas d'échec : tête par défaut (le menu s'ouvre quand même).
     */
    public static ItemStack skullOf(String name) {
        Object profile = ownerProfile(name);
        if (profile == null) {
            return XMaterial.PLAYER_HEAD.parseItem();
        }
        try {
            org.bukkit.inventory.ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
            org.bukkit.inventory.meta.ItemMeta meta = head.getItemMeta();
            meta.getClass().getMethod("setOwnerProfile", Class.forName("org.bukkit.profile.PlayerProfile"))
                    .invoke(meta, profile);
            head.setItemMeta(meta);
            return head;
        } catch (Exception e) {
            e.printStackTrace();
            return XMaterial.PLAYER_HEAD.parseItem();
        }
    }
}
