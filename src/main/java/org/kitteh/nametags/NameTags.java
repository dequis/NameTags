/* Copyright 2012 Matt Baxter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kitteh.nametags;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class NameTags extends JavaPlugin implements Listener {

    private enum Color {
        aqua,
        black,
        blue,
        dark_aqua,
        dark_blue,
        dark_gray,
        dark_green,
        dark_purple,
        dark_red,
        gold,
        gray,
        green,
        light_purple,
        red,
        yellow,
        white;

        private final ChatColor color;
        private final String node;

        Color() {
            this.color = ChatColor.valueOf(this.name().toUpperCase());
            this.node = "nametags.color." + this.name();
        }

        public ChatColor getColor() {
            return this.color;
        }

        public String getNode() {
            return this.node;
        }

    }

    private enum Format {
        bold,
        italic,
        magic,
        strikethrough,
        underline;

        private final ChatColor color;
        private final String node;

        Format() {
            this.color = ChatColor.valueOf(this.name().toUpperCase());
            this.node = "nametags.format." + this.name();
        }

        public ChatColor getColor() {
            return this.color;
        }

        public String getNode() {
            return this.node;
        }
    }

    private static final String CONFIG_BASECOLOR = "baseColor";
    private static final String CONFIG_BASECOLOR_DEFAULT = "white";
    private static final String CONFIG_NOLONGNAMES = "noChangeForLongNames";
    private static final String CONFIG_ONLYSAME = "onlySeeSame";
    private static final String CONFIG_REFRESH = "refreshAutomatically";
    private static final String CONFIG_SET_DISPLAYNAME = "setDisplayName";
    private static final String CONFIG_SET_TABNAME = "setTabName";
    private static final String METADATA_NAME = "nametags.displayname";

    private static final Object ADORABLE_OBJECT = new Object();

    private File configFile;
    private int refreshTaskID;
    private boolean setDisplayName;
    private boolean setTabName;
    private boolean noLongNames;
    private boolean onlySeeSelf;
    private ChatColor baseColor;
    private final Map<String, String> nameTagMap = new ConcurrentHashMap<String, String>();
    private final Map<String, Object> seenAlways = new ConcurrentHashMap<String, Object>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((args.length > 0) && args[0].equalsIgnoreCase("reload")) {
            this.load();
            sender.sendMessage("Reloaded!");
        }
        return true;
    }

    @Override
    public void onDisable() {
        for (final Player player : this.getServer().getOnlinePlayers()) {
            if ((player != null) && player.isOnline()) {
                player.removeMetadata(NameTags.METADATA_NAME, this);
            }
        }
    }

    @Override
    public void onEnable() {
        if (!this.getServer().getPluginManager().isPluginEnabled("TagAPI")) {
            this.getLogger().severe("TagAPI required. Get it at http://dev.bukkit.org/server-mods/tag/");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            Class.forName("org.kitteh.tag.AsyncPlayerReceiveNameTagEvent");
        } catch (final ClassNotFoundException e) {
            this.getLogger().severe("You need a newer version of TagAPI! Get it at http://dev.bukkit.org/server-mods/tag/");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.configFile = new File(this.getDataFolder(), "config.yml");
        this.load();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.calculate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNameTag(AsyncPlayerReceiveNameTagEvent event) {
        final String tag = this.getDisplay(event.getNamedPlayer());
        if (tag != null) {
            if (this.onlySeeSelf && !this.seenAlways.containsKey(event.getNamedPlayer().getName())) {
                final String otherTag = this.getDisplay(event.getPlayer());
                if (otherTag == null) {
                    event.setTag((this.baseColor != null ? this.baseColor : "") + event.getNamedPlayer().getName());
                    return;
                }
                final int ionamed = tag.indexOf(event.getNamedPlayer().getName());
                final int iosee = otherTag.indexOf(event.getPlayer().getName());
                if ((ionamed <= 0) || (ionamed != iosee) || !tag.substring(0, ionamed).equals(otherTag.substring(0, iosee))) {
                    event.setTag((this.baseColor != null ? this.baseColor : "") + event.getNamedPlayer().getName());
                    return;
                }
            }
            event.setTag(tag);
        }
    }

    private void calculate(Player player) {
        final StringBuilder name = new StringBuilder();
        final List<Color> colors = Arrays.asList(Color.values());
        Collections.shuffle(colors);
        for (final Color color : colors) {
        }
        if ((name.length() == 0) && (this.baseColor != null)) {
            name.append(this.baseColor);
        }
        if ((name.length() > 1) && (name.charAt(1) == 'f')) {
            name.setLength(0);
        }
        final List<Format> formats = Arrays.asList(Format.values());
        Collections.shuffle(formats);
        for (final Format format : formats) {
        }
        final String cleanName = player.getName();
        name.append(cleanName);
        if (name.length() > 16) {
            if (this.noLongNames) {
                name.delete(0, name.length()).append(cleanName);
            } else {
                name.setLength(16);
            }
        }
        final String newName = name.toString();
        player.setMetadata(NameTags.METADATA_NAME, new FixedMetadataValue(this, newName));

        this.nameTagMap.put(cleanName, newName);

        if (this.setDisplayName) {
            player.setDisplayName(newName + ChatColor.RESET);
        }
        if (this.setTabName) {
            player.setPlayerListName(newName);
        }
    }

    private String getDisplay(Player player) {
        return this.nameTagMap.get(player.getName());
    }

    private void load() {
        if (this.refreshTaskID != -1) {
            this.getServer().getScheduler().cancelTask(this.refreshTaskID);
            this.refreshTaskID = -1;
        }
        if (!this.configFile.exists()) {
            this.saveDefaultConfig();
        }
        this.reloadConfig();
        if (!this.getConfig().contains(NameTags.CONFIG_BASECOLOR)) {
            this.getConfig().set(NameTags.CONFIG_BASECOLOR, NameTags.CONFIG_BASECOLOR_DEFAULT);
        }
        if (!this.getConfig().contains(NameTags.CONFIG_NOLONGNAMES)) {
            this.getConfig().set(NameTags.CONFIG_NOLONGNAMES, false);
        }
        if (!this.getConfig().contains(NameTags.CONFIG_ONLYSAME)) {
            this.getConfig().set(NameTags.CONFIG_ONLYSAME, false);
        }
        if (!this.getConfig().contains(NameTags.CONFIG_REFRESH)) {
            this.getConfig().set(NameTags.CONFIG_REFRESH, false);
        }
        if (!this.getConfig().contains(NameTags.CONFIG_SET_DISPLAYNAME)) {
            this.getConfig().set(NameTags.CONFIG_SET_DISPLAYNAME, false);
        }
        if (!this.getConfig().contains(NameTags.CONFIG_SET_TABNAME)) {
            this.getConfig().set(NameTags.CONFIG_SET_TABNAME, false);
        }
        this.saveConfig();
        if (this.getConfig().getBoolean(NameTags.CONFIG_REFRESH, false)) {
            this.refreshTaskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    NameTags.this.playerRefresh();
                }
            }, 1200, 1200);
        }
        final boolean newSetDisplayName = this.getConfig().getBoolean(NameTags.CONFIG_SET_DISPLAYNAME, false);
        final boolean forceDisplayName = this.setDisplayName && !newSetDisplayName;
        final boolean newSetTabName = this.getConfig().getBoolean(NameTags.CONFIG_SET_TABNAME, false);
        final boolean forceTabName = this.setTabName && !newSetTabName;
        if (forceDisplayName || forceTabName) {
            for (final Player player : this.getServer().getOnlinePlayers()) {
                if (forceDisplayName) {
                    player.setDisplayName(player.getName());
                }
                if (forceTabName) {
                    player.setPlayerListName(player.getName());
                }
            }
        }
        ChatColor newBaseColor;
        try {
            newBaseColor = ChatColor.valueOf(this.getConfig().getString(NameTags.CONFIG_BASECOLOR, "white").toUpperCase());
        } catch (final Exception e) {
            newBaseColor = null;
        }
        this.baseColor = newBaseColor == ChatColor.WHITE ? null : newBaseColor;
        this.setDisplayName = newSetDisplayName;
        this.setTabName = newSetTabName;
        this.noLongNames = this.getConfig().getBoolean(NameTags.CONFIG_NOLONGNAMES, false);
        this.onlySeeSelf = this.getConfig().getBoolean(NameTags.CONFIG_ONLYSAME, false);
        this.getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                NameTags.this.playerRefresh();
            }
        }, 2);
    }

    private void playerRefresh() {
        for (final Player player : this.getServer().getOnlinePlayers()) {
            if ((player != null) && player.isOnline()) {
                final String oldTag = this.getDisplay(player);
                this.calculate(player);
                final String newTag = this.getDisplay(player);
                final boolean one = (oldTag == null) && (newTag != null);
                final boolean two = (oldTag != null) && (newTag == null);
                final boolean three = ((oldTag != null) && (newTag != null)) && !oldTag.equals(newTag);
                if (one || two || three) {
                    TagAPI.refreshPlayer(player);
                }
            }
        }
    }

}

// vim: ts=4 sts=4 sw=4 et
