package fr.perrier.guard.utils;

import com.cryptomorin.xseries.*;
import org.bukkit.*;
import org.bukkit.inventory.*;

/**
 * @author Rhodless
 */
public enum Heads {

    NEXT_PAGE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ=="),
    PREVIOUS_PAGE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWQ3M2NmNjZkMzFiODNjZDhiODY0NGMxNTk1OGMxYjczYzhkOTczMjNiODAxMTcwYzFkODg2NGJiNmE4NDZkIn19fQ=="),
    BALLOON_BLUE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg2OGU2YTVjNGE0NDVkNjBhMzA1MGI1YmVjMWQzN2FmMWIyNTk0Mzc0NWQyZDQ3OTgwMGM4NDM2NDg4MDY1YSJ9fX0="),
    BALLOON_RED("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJkZDExZGEwNDI1MmY3NmI2OTM0YmMyNjYxMmY1NGYyNjRmMzBlZWQ3NGRmODk5NDEyMDllMTkxYmViYzBhMiJ9fX0="),
    BALLOON_ORANGE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTdmMzgxYTIwYTljNjQwNDI4MDc3MDcwY2M3YmQ5NWQ2ODg1OTJkMTEwNGNjYmNkNzEzNjQ5YTQ5ZTQxZWJmYiJ9fX0="),
    BALLOON_GREEN("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2EyZGYzMTViNDM1ODNiMTg5NjIzMWI3N2JhZTFhNTA3ZGJkN2U0M2FkODZjMWNmYmUzYjJiOGVmMzQzMGU5ZSJ9fX0="),
    COAL_CHEST("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzY1OGNjYzczNDU1NTllOTMyMWY0OWVlMWFmNjc1MjJlNzA4ZGNhODkzMmIwYTcyMWNjMzQxMzA3MzFlYjU5OCJ9fX0="),
    CHEST_MINECART("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlZDM0MjExZmVkNDAxMGE4Yzg1NzI0YTI3ZmE1ZmIyMDVkNjc2ODRiM2RhNTE3YjY4MjEyNzljNmI2NWQzZiJ9fX0="),
    COMMAND_BLOCK("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE5MDUzZDIxNjNkMGY1NjExNDVkMzNhNTEzMTQ1ZDRhYzFmOGE0NThiYWE3OTZiZTM4M2U3NTI1YTA1ZjQ1In19fQ=="),
    SPECIAL_CHEST("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI2MjlmMjY4MmRjZWUzMGY1ODU1YjFlNTQyN2NjNGJlZTczZDE4YTI3NmZhZmM1MjBkNjkzYjQwY2E4MWIyMiJ9fX0="),
    SOON("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ=="),
    YOUR_NAME("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk3YTQ2YjY1ZWIxMjAwY2I5MGIzNzQwNmU4ZjA1ZTJhN2NlNjY3M2MxZjJjZGY5YWUxM2I3YzBlMzZjNjBlNyJ9fX0="),
    DICE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYxYWVhMDQ3YmNmYzExNzkzOThhYTQ2MDIwNTM5N2RiMDI0NmEyZmM0N2QwMzVlMGU2ZTk3OTIzZmIzY2IxMiJ9fX0="),
    SETTINGS("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmZmMTczM2JmNGU4ZWU4MTA1ZjYwMmQ0NzY3MjU1ODBkOTM1MTI3ZDk4ZjMwYmQyNjQzYzY2ZWM4MjA0NjE5ZiJ9fX0="),
    LUCKY("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM0OTcwZWE5MWFiMDZlY2U1OWQ0NWZjZTc2MDRkMjU1NDMxZjJlMDNhNzM3YjIyNjA4MmM0Y2NlMWFjYTFjNCJ9fX0="),
    RED_F("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODhiYmMzNTIxM2M2NTEwOWZiZDhlOTA3NzFlNGM2ZGI3ODVhOWJlOGU0MDNkMjllZDhlZDJlM2RjZmIxMjgifX19"),
    YELLOW_I("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTdjODljZDY1MmIxMjFjNWJlZDQzMTVlO WVmMDRjNmExM2Y2ZGRmNzYyODgxYmZhYzhmNWU1YzZmZDkwIn19fQ=="),
    LIME_PLUS("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTdjODljZDY1MmIxMjFjNWJlZDQzMTVlOWVmMDRjNmExM2Y2ZGRmNzYyODgxYmZhYzhmNWU1YzZmZDkwIn19fQ=="),
    SHOP("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZkN2ZkYjUwZjE0YzczMWM3MjdiMGUwZDE4OWI2YTg3NDMxOWZjMGQ3OWM4YTA5OWFjZmM3N2M3YjJkOTE5NiJ9fX0="),
    BOUTIQUE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY0ZmNkOTU5OGExZmQzOWMyOGY1MDgwOWU0Y2U4YTEzMjJhZDQ4YTlhYmQ4YzIzZjIxMTg4Y2UwYTgyODhlNCJ9fX0="),
    ;

    private final String base;

    Heads(String base) {
        this.base = base;
    }

    public String getBase() {
        return base;
    }

    /**
     * Pur (testé) : UUID offline déterministe d'un pseudo — algorithme Mojang
     * {@code UUID.nameUUIDFromBytes("OfflinePlayer:" + name)} : même UUID en ligne et hors
     * ligne, sur serveur online comme crack (aucune requête réseau).
     */
    public static java.util.UUID offlineUuid(String name) {
        return java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public ItemStack toItemStack() {
        return new ItemBuilder(skullOf("Guard")).setTexture(getBase()).toItemStack();
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
