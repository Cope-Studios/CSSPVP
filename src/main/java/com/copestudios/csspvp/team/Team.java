package com.copestudios.csspvp.team;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("CSSPVP_Team")
public class Team implements ConfigurationSerializable {
    private String name;
    private List<UUID> members;

    public Team(String name) {
        this(name, new ArrayList<>());
    }

    public Team(String name, List<UUID> members) {
        this.name = name;
        this.members = members;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);

        List<String> memberStrings = members.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        map.put("members", memberStrings);

        return map;
    }

    public static Team deserialize(Map<String, Object> map) {
        String name = (String) map.get("name");

        List<UUID> members = new ArrayList<>();
        if (map.containsKey("members")) {
            @SuppressWarnings("unchecked")
            List<String> memberStrings = (List<String>) map.get("members");
            for (String uuidStr : memberStrings) {
                try {
                    members.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUID
                }
            }
        }

        return new Team(name, members);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public boolean addMember(UUID uuid) {
        if (!members.contains(uuid)) {
            members.add(uuid);
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public int getSize() {
        return members.size();
    }

    public List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }
}