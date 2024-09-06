package com.cleanroommc.neverenoughanimations.config;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.Tags;
import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A cursed config helper to parse annotations of fields and turns it into a forge config.
 */
public class ModConfigMagic {

    private static final List<Value> configs = new ArrayList<>();
    private static final List<String> translations = new ArrayList<>();

    private static final ObjectArrayList<String> sectionStack = new ObjectArrayList<>();

    static {
        String title = Tags.NAME + " Config";
        appendTranslation("title", title, false);
        appendTranslation("section." + NEA.MODID + ".common.toml.title", title, false);
        appendTranslation("section." + NEA.MODID + ".common.toml", title, false);
    }

    public static ModConfigSpec create(Class<?> clazz) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        collectValuesFrom(builder, clazz);
        return builder.build();
    }

    private static void collectValuesFrom(ModConfigSpec.Builder builder, Object o) {
        boolean isStatic = o instanceof Class<?>;
        Class<?> clazz = isStatic ? (Class<?>) o : o.getClass();
        Object instance = isStatic ? null : o;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Config.Ignore.class) ||
                    !field.canAccess(null) ||
                    Modifier.isStatic(field.getModifiers()) != isStatic) {
                continue;
            }
            Class<?> type = field.getType();
            String path = field.getName();
            Object def;
            try {
                def = field.get(instance);
                if (def == null) {
                    NEA.LOGGER.warn("Default config value for field {} is null. This is not good!", path);
                    continue;
                }
            } catch (IllegalAccessException e) {
                continue;
            }
            if (field.isAnnotationPresent(Config.Category.class)) {
                builder.push(path);
                sectionStack.push(path);
                collectValuesFrom(builder, def);
                sectionStack.pop();
                builder.pop();
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) continue;
            Value value = parseToConfigValue(builder, field, path, type, def);
            value.instance = instance;
            configs.add(value);
        }
    }

    private static void appendTranslation(String path, String translation, boolean tooltip) {
        if (FMLLoader.isProduction()) return;
        StringBuilder b = new StringBuilder().append('\t').append('"').append(NEA.MODID).append('.').append("configuration.");
        sectionStack.forEach(s -> b.append(s).append('.'));
        b.append(path);
        if (tooltip) b.append(".tooltip");
        b.append('"').append(": ").append('"').append(translation).append('"').append(", \n");
        translations.add(b.toString());
    }

    private static Value parseToConfigValue(ModConfigSpec.Builder builder, Field field, String path, Class<?> type, Object def) {
        Function<Object, Object> converter = o -> o;
        ModConfigSpec.ConfigValue<?> value;
        boolean hasTooltip = false;
        if (field.isAnnotationPresent(Config.Name.class)) {
            appendTranslation(path, field.getAnnotation(Config.Name.class).value(), false);
        }
        if (field.isAnnotationPresent(Config.Comment.class)) {
            Config.Comment comment = field.getAnnotation(Config.Comment.class);
            builder.comment(comment.value());
            appendTranslation(path, Joiner.on("\\n").join(comment.value()), true);
            hasTooltip = true;
        }
        if (field.isAnnotationPresent(Config.RequiresWorldRestart.class)) {
            builder.worldRestart();
        }
        if (field.isAnnotationPresent(Config.RequiresMcRestart.class)) {
            builder.gameRestart();
        }
        if (field.isAnnotationPresent(Config.RangeInt.class) && (type == Integer.class || type == int.class)) {
            Config.RangeInt range = field.getAnnotation(Config.RangeInt.class);
            value = builder.defineInRange(path, (int) def, range.min(), range.max(), Integer.class);
        } else if (field.isAnnotationPresent(Config.RangeDouble.class) && (type == Double.class || type == double.class)) {
            Config.RangeDouble range = field.getAnnotation(Config.RangeDouble.class);
            value = builder.defineInRange(path, (double) def, range.min(), range.max(), Double.class);
        } else if (Enum.class.isAssignableFrom(type)) {
            if (!hasTooltip) {
                StringBuilder b = new StringBuilder().append("Allowed values: ");
                Class<Enum<?>> enumClass = (Class<Enum<?>>) type;
                for (Enum<?> e : enumClass.getEnumConstants()) {
                    b.append(e.name()).append(", ");
                }
                b.deleteCharAt(b.length() - 1);
                b.deleteCharAt(b.length() - 1);
                appendTranslation(path, b.toString(), true);
                hasTooltip = true;
            }
            value = builder.defineEnum(path, (Enum) def);
        } else if (type.isArray()) {
            List<Object> list = new ArrayList<>();
            Collections.addAll(list, (Object[]) def);
            value = builder.defineList(path, list, o -> true);
            converter = o -> ((List<?>) o).toArray((Object[]) Array.newInstance(type.getComponentType(), 0));
            appendTranslation(path + ".button", "Edit list", false);
        } else if (def instanceof Boolean b) {
            value = builder.define(path, b.booleanValue());
        } else {
            value = builder.define(path, def);
        }
        if (!hasTooltip) appendTranslation(path, "", true);
        return new Value(field, value, converter);
    }

    public static void load() {
        for (Value value : configs) {
            value.apply();
        }
        if (!translations.isEmpty()) {
            // TODO use data gen
            StringBuilder b = new StringBuilder("Config translations:\n");
            translations.forEach(b::append);
            NEA.LOGGER.info(b);
        }
    }

    private static class Value {

        private Object instance;
        private final Field field;
        private final ModConfigSpec.ConfigValue<?> configValue;
        private final Function<Object, Object> converter;

        private Value(Field field, ModConfigSpec.ConfigValue<?> configValue, Function<Object, Object> converter) {
            this.field = field;
            this.configValue = configValue;
            this.converter = converter;
        }

        private void apply() {
            try {
                field.set(instance, converter.apply(configValue.get()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
