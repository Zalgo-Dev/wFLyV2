package com.wayvi.wfly.wflyV2.placeholders;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;

    public WFlyPlaceholder(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wfly";
    }

    @Override
    public @NotNull String getAuthor() {
        return "wPlugin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();


            if (params.equals("fly_remaining")) {
                int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(player);
                if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                    return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomConfig().getString("format-placeholder.unlimited"));
                }
                return formatTime(timeRemaining);
            }

            if (params.equals("fly_activate")) {
                try {
                    UUID player1 = offlinePlayer.getUniqueId();
                    AccessPlayerDTO isFlying = plugin.getFlyManager().getPlayerFlyData(player1);
                    return String.valueOf(isFlying.isinFly());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private String formatTime(int seconds) {
        Map<String, Boolean> enabledFormats = plugin.getTimeFormatTranslatorUtil().getTimeUnitsEnabled();
        String format = plugin.getTimeFormatTranslatorUtil().getPlaceholderFormat();
        boolean autoFormat = configUtil.getCustomConfig().getBoolean("format-placeholder.auto-format");
        boolean removeNullValues = configUtil.getCustomConfig().getBoolean("format-placeholder.remove-null-values");

        String secondsSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.seconds_suffixe", "s");
        String minutesSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.minutes_suffixe", "m");
        String hoursSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.hours_suffixe", "h");
        String daysSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.days_suffixe", "j");

        int days = seconds / 86400;
        int hours = (seconds % 86400) / 3600;
        int minutes = (seconds % 3600) / 60;
        int sec = seconds % 60;

        // Regroupement des unités selon les formats activés
        if (!enabledFormats.get("days")) {
            hours += days * 24;
            days = 0;
        }
        if (!enabledFormats.get("hours")) {
            minutes += hours * 60;
            hours = 0;
        }
        if (!enabledFormats.get("minutes")) {
            sec += minutes * 60;
            minutes = 0;
        }
        if (enabledFormats.get("minutes")) {
            minutes += sec / 60;
            sec %= 60;
        }
        if (enabledFormats.get("hours")) {
            hours += minutes / 60;
            minutes %= 60;
        }
        if (enabledFormats.get("days")) {
            days += hours / 24;
            hours %= 24;
        }

        if (autoFormat) {
            if (!enabledFormats.getOrDefault("days", false)) {
                format = format.replace("%days%", "").replace("%days_suffixe%", "");
            }
            if (!enabledFormats.getOrDefault("hours", false)) {
                format = format.replace("%hours%", "").replace("%hours_suffixe%", "");
            }
            if (!enabledFormats.getOrDefault("minutes", false)) {
                format = format.replace("%minutes%", "").replace("%minutes_suffixe%", "");
            }
            if (!enabledFormats.getOrDefault("seconds", false)) {
                format = format.replace("%seconds%", "").replace("%seconds_suffixe%", "");
            }
        }

        format = format.replace("%seconds%", (removeNullValues && sec == 0) ? "" : String.valueOf(sec))
                .replace("%minutes%", (removeNullValues && minutes == 0) ? "" : String.valueOf(minutes))
                .replace("%hours%", (removeNullValues && hours == 0) ? "" : String.valueOf(hours))
                .replace("%days%", (removeNullValues && days == 0) ? "" : String.valueOf(days))
                .replace("%seconds_suffixe%", (removeNullValues && sec == 0) ? "" : secondsSuffix)
                .replace("%minutes_suffixe%", (removeNullValues && minutes == 0) ? "" : minutesSuffix)
                .replace("%hours_suffixe%", (removeNullValues && hours == 0) ? "" : hoursSuffix)
                .replace("%days_suffixe%", (removeNullValues && days == 0) ? "" : daysSuffix);

        format = format.replaceAll("\\s+", " ").trim();

        if (format.isEmpty()) {
            format = "0" + secondsSuffix;
        }

        return (String) ColorSupportUtil.convertColorFormat(format);
    }

}
