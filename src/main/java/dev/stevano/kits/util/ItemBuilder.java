package dev.stevano.kits.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this(material, amount, (short) 0);
    }

    public ItemBuilder(Material material, int amount, short durability) {
        this.item = new ItemStack(material != null ? material : Material.AIR, amount, durability);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack != null ? itemStack.clone() : new ItemStack(Material.AIR);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public static ItemBuilder from(Object obj) {
        ItemStack item = deserialize(obj);
        return new ItemBuilder(item != null ? item : new ItemStack(Material.AIR));
    }

    public ItemBuilder material(Material material) {
        if (material != null) {
            this.item.setType(material);
        }
        return this;
    }

    public ItemBuilder type(Material material) {
        return material(material);
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.item.setDurability(durability);
        return this;
    }

    public ItemBuilder data(short data) {
        return durability(data);
    }

    public ItemBuilder name(String name) {
        if (name != null) {
            ItemMeta meta = this.item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate(name));
                this.item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder displayName(String name) {
        return name(name);
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        if (lore != null) {
            ItemMeta meta = this.item.getItemMeta();
            if (meta != null) {
                meta.setLore(CC.translate(lore));
                this.item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        return addLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(List<String> lines) {
        if (lines != null && !lines.isEmpty()) {
            ItemMeta meta = this.item.getItemMeta();
            if (meta != null) {
                List<String> current = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                current.addAll(CC.translate(lines));
                meta.setLore(current);
                this.item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.setLore(new ArrayList<>());
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (enchantment != null) {
            this.item.addUnsafeEnchantment(enchantment, level);
        }
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        return enchant(enchantment, level);
    }

    public ItemBuilder clearEnchantments() {
        for (Enchantment enchantment : new HashSet<>(this.item.getEnchantments().keySet())) {
            this.item.removeEnchantment(enchantment);
        }
        return this;
    }

    public ItemBuilder color(Color color) {
        if (color != null) {
            ItemMeta meta = this.item.getItemMeta();
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(color);
                this.item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder color(String hexOrRgb) {
        return color(parseColor(hexOrRgb));
    }

    public ItemBuilder owner(String playerName) {
        if (playerName != null && !playerName.isEmpty()) {
            ItemMeta meta = this.item.getItemMeta();
            if (meta instanceof SkullMeta) {
                ((SkullMeta) meta).setOwner(playerName);
                this.item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            try {
                meta.spigot().setUnbreakable(unbreakable);
                this.item.setItemMeta(meta);
            } catch (Throwable ignored) {}
        }
        return this;
    }

    public ItemStack build() {
        return this.item.clone();
    }

    public static Map<String, Object> serialize(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("material", item.getType().name());

        if (item.getAmount() > 1) {
            map.put("amount", item.getAmount());
        }

        if (item.getDurability() > 0) {
            map.put("data", (int) item.getDurability());
        }

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                map.put("name", CC.deTranslate(meta.getDisplayName()));
            }

            if (meta.hasLore() && meta.getLore() != null && !meta.getLore().isEmpty()) {
                map.put("lore", CC.deTranslate(meta.getLore()));
            }

            if (meta.hasEnchants()) {
                Map<String, Integer> enchants = new LinkedHashMap<>();
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    enchants.put(entry.getKey().getName(), entry.getValue());
                }
                map.put("enchants", enchants);
            }

            if (meta instanceof LeatherArmorMeta) {
                Color color = ((LeatherArmorMeta) meta).getColor();
                map.put("color", String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
            }

            if (meta instanceof SkullMeta && ((SkullMeta) meta).hasOwner()) {
                map.put("owner", ((SkullMeta) meta).getOwner());
            }

            try {
                if (meta.spigot().isUnbreakable()) {
                    map.put("unbreakable", true);
                }
            } catch (Throwable ignored) {}
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public static ItemStack deserialize(Object obj) {
        if (obj == null) return null;

        if (obj instanceof ItemStack) {
            return ((ItemStack) obj).clone();
        }

        if (obj instanceof ConfigurationSection) {
            ConfigurationSection sec = (ConfigurationSection) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            for (String key : sec.getKeys(false)) {
                map.put(key, sec.get(key));
            }
            return deserializeMap(map);
        }

        if (obj instanceof Map<?, ?>) {
            return deserializeMap((Map<String, Object>) obj);
        }

        if (obj instanceof List<?>) {
            return fromListFormat((List<?>) obj);
        }

        if (obj instanceof String) {
            return fromStringRepresentation((String) obj);
        }

        return null;
    }

    private static ItemStack deserializeMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;

        Object matObj = map.get("material");
        if (matObj == null) matObj = map.get("type");
        if (matObj == null) return null;

        Material material = parseMaterial(String.valueOf(matObj));
        if (material == null || material == Material.AIR) return null;

        int amount = 1;
        if (map.containsKey("amount")) {
            amount = parseInt(map.get("amount"), 1);
        }

        short durability = 0;
        if (map.containsKey("data")) {
            durability = (short) parseInt(map.get("data"), 0);
        } else if (map.containsKey("durability")) {
            durability = (short) parseInt(map.get("durability"), 0);
        }

        ItemBuilder builder = new ItemBuilder(material, amount, durability);

        if (map.containsKey("name")) {
            builder.name(String.valueOf(map.get("name")));
        } else if (map.containsKey("displayName")) {
            builder.name(String.valueOf(map.get("displayName")));
        }

        if (map.containsKey("lore")) {
            Object loreObj = map.get("lore");
            if (loreObj instanceof List<?>) {
                List<String> loreLines = new ArrayList<>();
                for (Object line : (List<?>) loreObj) {
                    loreLines.add(String.valueOf(line));
                }
                builder.lore(loreLines);
            } else if (loreObj instanceof String) {
                builder.lore(((String) loreObj).split("\\|"));
            }
        }

        if (map.containsKey("enchants")) {
            Object enchantsObj = map.get("enchants");
            if (enchantsObj instanceof Map<?, ?>) {
                Map<?, ?> enchantsMap = (Map<?, ?>) enchantsObj;
                for (Map.Entry<?, ?> entry : enchantsMap.entrySet()) {
                    Enchantment ench = parseEnchantment(String.valueOf(entry.getKey()));
                    if (ench != null) {
                        int level = parseInt(entry.getValue(), 1);
                        builder.enchant(ench, level);
                    }
                }
            } else if (enchantsObj instanceof ConfigurationSection) {
                ConfigurationSection sec = (ConfigurationSection) enchantsObj;
                for (String key : sec.getKeys(false)) {
                    Enchantment ench = parseEnchantment(key);
                    if (ench != null) {
                        builder.enchant(ench, sec.getInt(key, 1));
                    }
                }
            } else if (enchantsObj instanceof String) {
                parseEnchantmentsString(builder, (String) enchantsObj);
            }
        }

        if (map.containsKey("color")) {
            builder.color(String.valueOf(map.get("color")));
        }

        if (map.containsKey("owner")) {
            builder.owner(String.valueOf(map.get("owner")));
        }

        if (map.containsKey("unbreakable")) {
            builder.unbreakable(Boolean.parseBoolean(String.valueOf(map.get("unbreakable"))));
        }

        return builder.build();
    }

    private static ItemStack fromListFormat(List<?> list) {
        if (list == null || list.isEmpty()) return null;

        int startIndex = 0;
        if (list.size() > 1 && isInteger(list.get(0)) && !isMaterial(list.get(0))) {
            startIndex = 1;
        }

        if (startIndex >= list.size()) return null;

        Material material = parseMaterial(String.valueOf(list.get(startIndex)));
        if (material == null || material == Material.AIR) return null;

        ItemBuilder builder = new ItemBuilder(material);

        if (list.size() > startIndex + 1 && list.get(startIndex + 1) != null) {
            String name = String.valueOf(list.get(startIndex + 1)).trim();
            if (!name.isEmpty() && !name.equalsIgnoreCase("none")) {
                builder.name(name);
            }
        }

        if (list.size() > startIndex + 2 && list.get(startIndex + 2) != null) {
            builder.amount(parseInt(list.get(startIndex + 2), 1));
        }

        if (list.size() > startIndex + 3 && list.get(startIndex + 3) != null) {
            builder.durability((short) parseInt(list.get(startIndex + 3), 0));
        }

        if (list.size() > startIndex + 4 && list.get(startIndex + 4) != null) {
            String enchStr = String.valueOf(list.get(startIndex + 4)).trim();
            if (!enchStr.isEmpty() && !enchStr.equalsIgnoreCase("none")) {
                parseEnchantmentsString(builder, enchStr);
            }
        }

        return builder.build();
    }

    private static ItemStack fromStringRepresentation(String in) {
        if (in == null || in.trim().isEmpty() || in.equalsIgnoreCase("AIR") || in.equalsIgnoreCase("null")) {
            return null;
        }

        if (in.startsWith("rO0AB")) {
            try {
                return itemFromBase64(in);
            } catch (Exception ignored) {}
        }

        if (!in.contains(";") && in.contains(":")) {
            String[] split = in.split(":");
            Material mat = parseMaterial(split[0]);
            if (mat != null) {
                ItemBuilder b = new ItemBuilder(mat);
                if (split.length > 1) b.amount(parseInt(split[1], 1));
                if (split.length > 2) b.durability((short) parseInt(split[2], 0));
                return b.build();
            }
        }

        Material mat = parseMaterial(in);
        if (mat != null) {
            return new ItemStack(mat, 1);
        }

        return fromItemString(in);
    }

    public static PotionEffect parsePotionEffect(Object obj) {
        if (obj == null) return null;
        if (obj instanceof PotionEffect) return (PotionEffect) obj;

        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Object typeObj = map.get("type");
            if (typeObj == null) typeObj = map.get("effect");
            if (typeObj == null) return null;

            PotionEffectType type = parsePotionEffectType(String.valueOf(typeObj));
            if (type == null) return null;

            int duration = parseInt(map.get("duration"), 1200);
            int amplifier = parseInt(map.get("amplifier"), 0);

            return new PotionEffect(type, duration, amplifier);
        }

        if (obj instanceof ConfigurationSection) {
            ConfigurationSection sec = (ConfigurationSection) obj;
            String typeName = sec.getString("type", sec.getString("effect", null));
            PotionEffectType type = parsePotionEffectType(typeName);
            if (type == null) return null;

            int duration = sec.getInt("duration", 1200);
            int amplifier = sec.getInt("amplifier", 0);
            return new PotionEffect(type, duration, amplifier);
        }

        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            if (list.isEmpty()) return null;
            PotionEffectType type = parsePotionEffectType(String.valueOf(list.get(0)));
            if (type == null) return null;
            int duration = list.size() > 1 ? parseInt(list.get(1), 1200) : 1200;
            int amplifier = list.size() > 2 ? parseInt(list.get(2), 0) : 0;
            return new PotionEffect(type, duration, amplifier);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            String[] split = str.contains(";") ? str.split(";") : str.split(":");
            if (split.length >= 1) {
                PotionEffectType type = parsePotionEffectType(split[0]);
                if (type == null) return null;
                int duration = split.length >= 2 ? parseInt(split[1], 1200) : 1200;
                int amplifier = split.length >= 3 ? parseInt(split[2], 0) : 0;
                return new PotionEffect(type, duration, amplifier);
            }
        }

        return null;
    }

    public static PotionEffectType parsePotionEffectType(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String clean = name.trim().toUpperCase().replace(" ", "_");
        PotionEffectType type = PotionEffectType.getByName(clean);
        if (type != null) return type;

        switch (clean) {
            case "SPEED": return PotionEffectType.SPEED;
            case "SLOWNESS": case "SLOW": return PotionEffectType.SLOW;
            case "HASTE": return PotionEffectType.FAST_DIGGING;
            case "MINING_FATIGUE": return PotionEffectType.SLOW_DIGGING;
            case "STRENGTH": return PotionEffectType.INCREASE_DAMAGE;
            case "INSTANT_HEAL": case "HEAL": return PotionEffectType.HEAL;
            case "INSTANT_DAMAGE": case "HARM": return PotionEffectType.HARM;
            case "JUMP_BOOST": case "JUMP": return PotionEffectType.JUMP;
            case "NAUSEA": return PotionEffectType.CONFUSION;
            case "REGENERATION": case "REGEN": return PotionEffectType.REGENERATION;
            case "RESISTANCE": return PotionEffectType.DAMAGE_RESISTANCE;
            case "FIRE_RESISTANCE": return PotionEffectType.FIRE_RESISTANCE;
            case "WATER_BREATHING": return PotionEffectType.WATER_BREATHING;
            case "INVISIBILITY": return PotionEffectType.INVISIBILITY;
            case "BLINDNESS": return PotionEffectType.BLINDNESS;
            case "NIGHT_VISION": return PotionEffectType.NIGHT_VISION;
            case "HUNGER": return PotionEffectType.HUNGER;
            case "WEAKNESS": return PotionEffectType.WEAKNESS;
            case "POISON": return PotionEffectType.POISON;
            case "WITHER": return PotionEffectType.WITHER;
            case "HEALTH_BOOST": return PotionEffectType.HEALTH_BOOST;
            case "ABSORPTION": return PotionEffectType.ABSORPTION;
            case "SATURATION": return PotionEffectType.SATURATION;
            default: return null;
        }
    }

    public static String toBase64(ItemStack[] items) {
        if (items == null || items.length == 0) return "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.flush();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ItemStack array to Base64.", e);
        }
    }

    public static ItemStack[] fromBase64(String data) {
        if (data == null || data.trim().isEmpty()) return new ItemStack[0];
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize ItemStack array from Base64.", e);
        }
    }

    public static String itemToBase64(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeObject(item);
            dataOutput.flush();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ItemStack to Base64.", e);
        }
    }

    public static ItemStack itemFromBase64(String data) {
        if (data == null || data.trim().isEmpty()) return null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize ItemStack from Base64.", e);
        }
    }

    public static String toItemString(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "AIR";

        StringBuilder sb = new StringBuilder();
        sb.append(item.getType().name());

        if (item.getAmount() > 1) sb.append(";amount=").append(item.getAmount());
        if (item.getDurability() > 0) sb.append(";data=").append(item.getDurability());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) sb.append(";name=").append(CC.deTranslate(meta.getDisplayName()).replace(';', ','));
            if (meta.hasLore() && meta.getLore() != null) sb.append(";lore=").append(String.join("|", CC.deTranslate(meta.getLore())).replace(';', ','));
            if (meta.hasEnchants()) {
                StringBuilder enchSb = new StringBuilder();
                boolean first = true;
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    if (!first) enchSb.append(",");
                    enchSb.append(entry.getKey().getName()).append(":").append(entry.getValue());
                    first = false;
                }
                sb.append(";enchants=").append(enchSb.toString());
            }
        }

        return sb.toString();
    }

    public static ItemStack fromItemString(String in) {
        if (in == null || in.trim().isEmpty() || in.equalsIgnoreCase("AIR") || in.equalsIgnoreCase("null")) return null;

        String[] parts = in.split(";");
        if (parts.length == 0) return null;

        Material material = parseMaterial(parts[0].trim());
        if (material == null) material = Material.WOOD_SWORD;

        ItemBuilder builder = new ItemBuilder(material);

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) continue;

            String lower = part.toLowerCase();
            if (lower.startsWith("amount=")) {
                builder.amount(parseInt(part.substring(part.indexOf('=') + 1), 1));
            } else if (lower.startsWith("data=") || lower.startsWith("durability=")) {
                builder.durability((short) parseInt(part.substring(part.indexOf('=') + 1), 0));
            } else if (lower.startsWith("name=")) {
                builder.name(part.substring(part.indexOf('=') + 1));
            } else if (lower.startsWith("lore=")) {
                builder.lore(part.substring(part.indexOf('=') + 1).split("\\|"));
            } else if (lower.startsWith("enchants=")) {
                parseEnchantmentsString(builder, part.substring(part.indexOf('=') + 1));
            }
        }

        return builder.build();
    }

    public static Material parseMaterial(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        String clean = str.trim().toUpperCase().replace(" ", "_");
        Material mat = Material.matchMaterial(clean);
        if (mat == null) {
            try {
                mat = Material.valueOf(clean);
            } catch (IllegalArgumentException e) {
                mat = null;
            }
        }
        return mat;
    }

    public static Enchantment parseEnchantment(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String clean = name.trim().toUpperCase().replace(" ", "_");
        Enchantment enchantment = Enchantment.getByName(clean);
        if (enchantment != null) return enchantment;

        switch (clean) {
            case "SHARPNESS": return Enchantment.DAMAGE_ALL;
            case "PROTECTION": return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "FIRE_PROTECTION": return Enchantment.PROTECTION_FIRE;
            case "FEATHER_FALLING": return Enchantment.PROTECTION_FALL;
            case "BLAST_PROTECTION": return Enchantment.PROTECTION_EXPLOSIONS;
            case "PROJECTILE_PROTECTION": return Enchantment.PROTECTION_PROJECTILE;
            case "RESPIRATION": return Enchantment.OXYGEN;
            case "AQUA_AFFINITY": return Enchantment.WATER_WORKER;
            case "UNBREAKING": return Enchantment.DURABILITY;
            case "POWER": return Enchantment.ARROW_DAMAGE;
            case "PUNCH": return Enchantment.ARROW_KNOCKBACK;
            case "FLAME": return Enchantment.ARROW_FIRE;
            case "INFINITY": return Enchantment.ARROW_INFINITE;
            case "EFFICIENCY": return Enchantment.DIG_SPEED;
            case "SILK_TOUCH": return Enchantment.SILK_TOUCH;
            case "FORTUNE": return Enchantment.LOOT_BONUS_BLOCKS;
            case "LOOTING": return Enchantment.LOOT_BONUS_MOBS;
            case "KNOCKBACK": return Enchantment.KNOCKBACK;
            case "FIRE_ASPECT": return Enchantment.FIRE_ASPECT;
            case "THORNS": return Enchantment.THORNS;
            case "DEPTH_STRIDER": return Enchantment.DEPTH_STRIDER;
            default: return null;
        }
    }

    public static Color parseColor(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        String clean = str.trim();
        if (clean.startsWith("#")) {
            try {
                return Color.fromRGB(Integer.parseInt(clean.substring(1), 16));
            } catch (NumberFormatException ignored) {}
        }
        if (clean.contains(",")) {
            String[] rgb = clean.split(",");
            if (rgb.length >= 3) {
                try {
                    return Color.fromRGB(
                            Integer.parseInt(rgb[0].trim()),
                            Integer.parseInt(rgb[1].trim()),
                            Integer.parseInt(rgb[2].trim())
                    );
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private static void parseEnchantmentsString(ItemBuilder builder, String enchantsStr) {
        String[] entries = enchantsStr.split(",");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) continue;
            String[] pair = trimmed.split(":");
            if (pair.length == 2) {
                Enchantment ench = parseEnchantment(pair[0].trim());
                if (ench != null) {
                    builder.enchant(ench, parseInt(pair[1].trim(), 1));
                }
            }
        }
    }

    private static boolean isInteger(Object obj) {
        if (obj instanceof Number) return true;
        if (obj instanceof String) {
            try {
                Integer.parseInt(((String) obj).trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean isMaterial(Object obj) {
        if (obj == null) return false;
        return parseMaterial(String.valueOf(obj)) != null;
    }

    private static int parseInt(Object obj, int fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(String.valueOf(obj).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
