package com.exiledpomegranate;

import com.exiledpomegranate.entities.BarrelBombEntity.SmokeParticles;
import net.minecraft.block.Block;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// I hate this class, it is probably the worst Java code I have ever written. It is so janky. I can't belive it works.
public class ConfigHandler {
    private static final String FILE_NAME = "barrelbombs.conf";
    private static final ConfigValues config = new ConfigValues();
    private static final ConfigValues defaultConfig = new ConfigValues();
    public static boolean dirty = false;

    private static String removeComments(String text) {
        StringBuilder output = new StringBuilder();
        for (char i : text.toCharArray()) {
            if (i == '#') break;
            output.append(i);
        }
        return output.toString();
    }

    private static ParticleEffect getSimpleParticleEffect(Identifier id) {
        ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);

        if (type == null) {
            throw new IllegalArgumentException("[BarrelBombs] Please fix your config, no such particle type: " + id);
        }

        if (!(type instanceof ParticleEffect)) {
            throw new IllegalArgumentException("[BarrelBombs] Please fix your config, " +
                    "particle '" + id + "' is parameterized and cannot be used as a simple ParticleEffect.");
        }

        return (ParticleEffect) type;
    }

    private static Block getSimpleBlock(Identifier id) {
        Block block = Registries.BLOCK.get(id);

        if (block == net.minecraft.block.Blocks.AIR && !id.equals(new Identifier("minecraft", "air"))) {
            throw new IllegalArgumentException("[BarrelBombs] Please fix your config, no such block: " + id);
        }

        return block;
    }

    // My list parsing functions are super janky. Don't read them.
    private static List<Block> parseBlacklist(String text) {
        List<String> idCollecter = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean collecting = false;
        for (char i : text.toCharArray()) {
            if (i == ' ') continue;
            if (i == '\"' && collecting) {
                collecting = false;
                idCollecter.add(current.toString());
                continue;
            }
            if (collecting) {
                current.append(i);
            }
            // collecting is always false here if i is "
            if (i == '\"') {
                collecting = true;
                current = new StringBuilder();
            }
        }

        List<Block> output = new ArrayList<>();
        idCollecter.forEach((id) -> output.add(getSimpleBlock(new Identifier(id))));
        return output;
    }

    private static List<SmokeParticles> parseSmoke(String text) {
        List<List<String>> data = new ArrayList<>();
        int index = -1;
        boolean collecting = false;
        for (char i : text.toCharArray()) {
            if (i == ' ') continue;
            if (i == '\"') continue;
            if (i == '(') {
                collecting = true;
                data.add(new ArrayList<>());
                index++;
                data.get(data.size() - 1).add("");
                continue;
            }
            if (i == ')') collecting = false;
            if (collecting) {
                if (i == ',') {
                    data.get(index).add("");
                    continue;
                }
                data.get(index).set(data.get(index).size() - 1, data.get(index).get(data.get(index).size() - 1) + i);
            }
        }

        List<SmokeParticles> output = new ArrayList<>();

        for (List<String> particleData : data) {
            output.add(new SmokeParticles(getSimpleParticleEffect(new Identifier(particleData.get(0))),
                    Integer.parseInt(particleData.get(1)), Float.parseFloat(particleData.get(2)), Float.parseFloat(particleData.get(3))));
        }

        return output;
    }
    // Okay, you can start reading functions again.

    public static void load(Path configDir) {
        Path path = configDir.resolve(FILE_NAME);
        if (!Files.exists(path)) {
            dirty = true;
            save(configDir);
            return;
        }

        try {
            List<String> rawlines = Files.readAllLines(path);
            List<String> lines = new ArrayList<>();
            lines.add("");
            // Allows tabs to be used to create multi-line lines
            for (String line : rawlines) {
                line = removeComments(line);
                if (line.isEmpty()) continue;
                if (line.startsWith("\t")) {
                    String updated = lines.get(lines.size()-1) + line.strip();
                    lines.set(lines.size()-1, updated);
                    continue;
                }
                lines.add(line.strip());
            }
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                // This takes the values from the config, and puts them into the config. Wow, that was super complex
                try {
                    switch (key) {
                        case "power" -> config.power = Float.parseFloat(value);
                        case "penetration" -> config.penetration = Float.parseFloat(value);
                        case "dropPercentage" -> config.dropPercentage = Float.parseFloat(value) / 100F;
                        case "directionalOffset" -> config.directionalOffset = Integer.parseInt(value);
                        case "additiveCap" -> config.additiveCap = Integer.parseInt(value);
                        case "smokeParticles" -> config.smokeParticles = parseSmoke(value);
                        case "dropBlacklist" -> config.dropBlacklist = parseBlacklist(value);
                        case "immuneList" -> config.immuneList = parseBlacklist(value);
                    }
                } catch (NullPointerException | NumberFormatException e) {
                    System.err.printf("[BarrelBombs] Couldn't read config value %s%n", key);
                }
            }
        } catch (IOException e) {
            System.err.println("[BarrelBombs] Failed to read config!");
        }
    }

    // I'm just gonna hope that these work the first try so that I don't have to figure out what I was thinking while I wrote these
    private static String formatSmoke(List<SmokeParticles> smokeParticles) {
        StringBuilder output = new StringBuilder();
        output.append("[(");
        for (SmokeParticles particles : smokeParticles) {
            StringBuilder data = new StringBuilder();
            List<String> properties = new ArrayList<>();
            properties.add(Registries.PARTICLE_TYPE.getId(particles.type().getType()).toString());
            properties.add(String.valueOf(particles.amount()));
            properties.add(String.valueOf(particles.range()));
            properties.add(String.valueOf(particles.velocity()));
            for (String property : properties) {
                data.append("\"");
                data.append(property);
                data.append("\", ");
            }
            data.setLength(data.length() - 2);
            output.append(data);
            output.append("), (");
        }
        output.setLength(output.length() - 3);
        output.append("]");
        return output.toString();
    }

    private static String formatBlacklist(List<Block> blacklist) {
        StringBuilder output = new StringBuilder();
        output.append("[");
        for (Block block : blacklist) {
            output.append("\"");
            output.append(Registries.BLOCK.getId(block));
            output.append("\", ");
        }
        output.setLength(output.length() - 2);
        output.append("]");
        return output.toString();
    }

    public static void save(Path configDir) {
        if (!dirty) return;
        Path path = configDir.resolve(FILE_NAME);
        List<String> lines = List.of(
                "# Explosive Barrel Config",
                "# Technically, nothing will stop you from entering something outside of the ranges, but it will probably cause weird behaviour and crashes.",
                "",
                "# Explosion power",
                "# Type: Float, Range: Probably between 0 and 50 before things start lagging a ton, Default: " + defaultConfig.power,
                "power = " + config.power,
                "",
                "# How much blast resistance should be ignored",
                "# Type: Float, Range: probably 500 to 2000 or smth, Default: " + defaultConfig.penetration,
                "penetration = " + config.penetration,
                "",
                "# What percent of kaboomed items should be dropped",
                "# Type: Percentage (technically float), Range: 0 to 100, Default: " + (defaultConfig.dropPercentage * 100),
                "dropPercentage = " + (config.dropPercentage * 100),
                "",
                "# How many blocks forward the explosion should go in the faced direction",
                "# Type: Integer, Range: >0, Default: " + defaultConfig.directionalOffset,
                "directionalOffset = " + config.directionalOffset,
                "",
                "# Max amount of extra additives that can be added to barrel bombs",
                "# Type: Integer, Range: 0 to 128, Default: " + defaultConfig.additiveCap,
                "additiveCap = " + config.additiveCap,
                "",
                "",
                "# Oh boy, it's time for lists. These things were a nightmare to code. If you want to make it multi-line, remember to tab every extra line",
                "",
                "# List of blocks that should not be dropped by barrel bombs",
                "# Default: " + formatBlacklist(defaultConfig.dropBlacklist),
                "dropBlacklist = " + formatBlacklist(config.dropBlacklist),
                "",
                "# List of blocks that are immune to barrel bombs (To avoid greifing)",
                "# Default: " + formatBlacklist(defaultConfig.immuneList),
                "immuneList = " + formatBlacklist(defaultConfig.immuneList),
                "",
                "# Particles to spawn when the barrel bomb explodes",
                "# Format: (particle type, amount, spawn range around explosion in blocks, velocity away from explosion in blocks)",
                "# Default: " + formatSmoke(defaultConfig.smokeParticles),
                "smokeParticles = " + formatSmoke(config.smokeParticles)
        );

        try {
            Files.createDirectories(configDir);
            Files.write(path, lines);
        } catch (IOException e) {
            System.err.println("[BarrelBombs] Failed to write config!");
        }
    }

    public static ConfigValues config() {
        return config;
    }

    public static ConfigValues defaultConfig() {
        return defaultConfig;
    }
}