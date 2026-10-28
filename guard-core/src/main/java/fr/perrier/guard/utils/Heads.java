package fr.perrier.guard.utils;

import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.gameprofile.MojangGameProfile;
import com.cryptomorin.xseries.profiles.gameprofile.XGameProfile;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Utilitaires de têtes de joueur (pseudo -> ItemStack), SANS aucune requête Mojang et sans
 * texture custom : compatible serveur online comme crack.
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

    public static ItemStack textured(String key) {
        String[] texture = GuardHeads.texture(key);

        if (texture == null) {
            return XMaterial.STRUCTURE_VOID.parseItem();
        }

        try {
            String value = texture[0];

            return XSkull.createItem().profile(Profileable.detect(value)).apply();
        } catch (Exception e) {
            e.printStackTrace();
            return XMaterial.PLAYER_HEAD.parseItem();
        }
    }
}