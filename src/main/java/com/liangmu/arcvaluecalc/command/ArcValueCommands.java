package com.liangmu.arcvaluecalc.command;

import com.liangmu.arcvaluecalc.config.ArcValueConfig;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.service.PriceParser;
import com.liangmu.arcvaluecalc.service.ValueFormatter;
import com.liangmu.arcvaluecalc.service.ValueService;
import com.liangmu.arcvaluecalc.service.ValueServices;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ArcValueCommands {
    private ArcValueCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("arcvalue")
                .then(Commands.literal("get")
                        .executes(context -> get(context.getSource())))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(ArcValueConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .then(Commands.argument("price", StringArgumentType.word())
                                .executes(context -> set(context.getSource(), StringArgumentType.getString(context, "price")))))
                .then(Commands.literal("settag")
                        .requires(source -> source.hasPermission(ArcValueConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .then(Commands.argument("price", StringArgumentType.word())
                                        .executes(context -> setTag(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "tag"),
                                                StringArgumentType.getString(context, "price")
                                        )))))
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(ArcValueConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .executes(context -> remove(context.getSource())))
                .then(Commands.literal("removetag")
                        .requires(source -> source.hasPermission(ArcValueConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .then(Commands.argument("tag", StringArgumentType.word())
                                .executes(context -> removeTag(context.getSource(), StringArgumentType.getString(context, "tag")))))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(ArcValueConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .executes(context -> reload(context.getSource())))
                .then(Commands.literal("export")
                        .requires(source -> source.hasPermission(ArcValueConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("rules");
                                    builder.suggest("values");
                                    return builder.buildFuture();
                                })
                                .executes(context -> export(context.getSource(), StringArgumentType.getString(context, "type"))))));
    }

    private static int get(CommandSourceStack source) {
        ItemStack stack = heldStack(source);
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("commands.arcvalue.no_item"));
            return 0;
        }
        ValueService service = ValueServices.server();
        return service.getValue(stack).map(value -> {
            source.sendSuccess(() -> Component.translatable(
                    "commands.arcvalue.value",
                    stack.getHoverName(),
                    ValueFormatter.display(value),
                    ArcValueConfig.VALUE_UNIT.get()
            ), false);
            source.sendSuccess(() -> Component.translatable("commands.arcvalue.source", service.getSource(stack).name()), false);
            return 1;
        }).orElseGet(() -> {
            source.sendFailure(Component.translatable("commands.arcvalue.no_value"));
            return 0;
        });
    }

    private static int set(CommandSourceStack source, String price) {
        ItemStack stack = heldStack(source);
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("commands.arcvalue.no_item"));
            return 0;
        }
        try {
            ValueKey key = ValueKey.itemOnly(stack);
            var value = PriceParser.parsePrice(price);
            ValueServices.server().setManualValue(key, value, source.getServer().getRecipeManager());
            source.sendSuccess(() -> Component.translatable(
                    "commands.arcvalue.set",
                    key.item().toString(),
                    ValueFormatter.display(value),
                    ArcValueConfig.VALUE_UNIT.get()
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int remove(CommandSourceStack source) {
        ItemStack stack = heldStack(source);
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("commands.arcvalue.no_item"));
            return 0;
        }
        try {
            ValueKey key = ValueKey.itemOnly(stack);
            boolean removed = ValueServices.server().removeManualValue(key, source.getServer().getRecipeManager());
            if (removed) {
                source.sendSuccess(() -> Component.translatable("commands.arcvalue.remove", key.item().toString()), true);
                return 1;
            }
            source.sendFailure(Component.translatable("commands.arcvalue.remove_missing", key.item().toString()));
            return 0;
        } catch (Exception e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int setTag(CommandSourceStack source, String tagText, String price) {
        try {
            ResourceLocation tag = parseTag(tagText);
            var value = PriceParser.parsePrice(price);
            ValueServices.server().setTagValue(tag, value, source.getServer().getRecipeManager());
            source.sendSuccess(() -> Component.translatable(
                    "commands.arcvalue.settag",
                    tag.toString(),
                    ValueFormatter.display(value),
                    ArcValueConfig.VALUE_UNIT.get()
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int removeTag(CommandSourceStack source, String tagText) {
        try {
            ResourceLocation tag = parseTag(tagText);
            boolean removed = ValueServices.server().removeTagValue(tag, source.getServer().getRecipeManager());
            if (removed) {
                source.sendSuccess(() -> Component.translatable("commands.arcvalue.removetag", tag.toString()), true);
                return 1;
            }
            source.sendFailure(Component.translatable("commands.arcvalue.removetag_missing", tag.toString()));
            return 0;
        } catch (Exception e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int reload(CommandSourceStack source) {
        ValueService service = ValueServices.server();
        service.reload(source.getServer().getRecipeManager(), source.getServer().registryAccess(), true);
        source.sendSuccess(() -> Component.translatable("commands.arcvalue.reload", service.size()), true);
        return 1;
    }

    private static int export(CommandSourceStack source, String type) {
        try {
            Path path;
            if ("rules".equals(type)) {
                path = ValueServices.server().exportRules();
            } else if ("values".equals(type)) {
                path = ValueServices.server().exportValues();
            } else {
                source.sendFailure(Component.literal("type must be rules or values"));
                return 0;
            }
            source.sendSuccess(() -> Component.translatable("commands.arcvalue.export", path.toString()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static ItemStack heldStack(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            return player.getMainHandItem();
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static ResourceLocation parseTag(String tagText) {
        String normalized = tagText.startsWith("#") ? tagText.substring(1) : tagText;
        return new ResourceLocation(normalized);
    }
}
