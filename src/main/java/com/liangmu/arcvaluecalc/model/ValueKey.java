package com.liangmu.arcvaluecalc.model;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class ValueKey {
    public static final int MAX_NBT_CHARS = 8192;
    private final ResourceLocation item;
    private final String nbt;

    public ValueKey(ResourceLocation item, CompoundTag nbt) {
        this(item, nbt == null || nbt.isEmpty() ? null : nbt.toString());
    }

    public ValueKey(ResourceLocation item, String nbt) {
        this.item = Objects.requireNonNull(item, "item");
        String normalized = nbt == null || nbt.isBlank() ? null : nbt;
        if (normalized != null && normalized.length() > MAX_NBT_CHARS) {
            throw new IllegalArgumentException("nbt is too large");
        }
        this.nbt = normalized;
    }

    public static ValueKey itemOnly(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered item: " + stack);
        }
        return new ValueKey(id, (String) null);
    }

    public static ValueKey exact(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered item: " + stack);
        }
        return new ValueKey(id, stack.getTag());
    }

    public ResourceLocation item() {
        return item;
    }

    public String nbt() {
        return nbt;
    }

    public boolean hasNbt() {
        return nbt != null;
    }

    public boolean matches(ItemStack stack) {
        ResourceLocation stackId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (!item.equals(stackId)) {
            return false;
        }
        if (nbt == null) {
            return true;
        }
        return nbt.equals(stack.getTag() == null ? null : stack.getTag().toString());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(item);
        buffer.writeBoolean(nbt != null);
        if (nbt != null) {
            buffer.writeUtf(nbt, MAX_NBT_CHARS);
        }
    }

    public static ValueKey read(FriendlyByteBuf buffer) {
        ResourceLocation item = buffer.readResourceLocation();
        String nbt = buffer.readBoolean() ? buffer.readUtf(MAX_NBT_CHARS) : null;
        return new ValueKey(item, nbt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValueKey valueKey)) {
            return false;
        }
        return item.equals(valueKey.item) && Objects.equals(nbt, valueKey.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, nbt);
    }

    @Override
    public String toString() {
        return nbt == null ? item.toString() : item + " " + nbt;
    }
}
