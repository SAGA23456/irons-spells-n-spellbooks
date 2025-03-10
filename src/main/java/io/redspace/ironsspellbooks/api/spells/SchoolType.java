package io.redspace.ironsspellbooks.api.spells;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

public class SchoolType {
//    FIRE(0),
//    ICE(1),
//    LIGHTNING(2),
//    HOLY(3),
//    ENDER(4),
//    BLOOD(5),
//    EVOCATION(6),
//    //VOID(7),
//    NATURE(8);

    final ResourceLocation id;
    final TagKey<Item> focus;
    final Component displayName;
    final Style displayStyle;
//    final PlaceholderDamageType damageType;
    final LazyOptional<Attribute> powerAttribute;
    final LazyOptional<Attribute> resistanceAttribute;
    final LazyOptional<SoundEvent> defaultCastSound;

    public SchoolType(ResourceLocation id, TagKey<Item> focus, Component displayName, LazyOptional<Attribute> powerAttribute, LazyOptional<Attribute> resistanceAttribute, LazyOptional<SoundEvent> defaultCastSound) {
        this.id = id;
        this.focus = focus;
        this.displayName = displayName;
        this.displayStyle = displayName.getStyle();
        this.powerAttribute = powerAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.defaultCastSound = defaultCastSound;
    }

    public double getResistanceFor(LivingEntity livingEntity) {
        var resistanceAttribute = this.resistanceAttribute.orElse(null);
        if (resistanceAttribute != null) {
            return livingEntity.getAttributeValue(resistanceAttribute);
        } else {
            return 1;
        }
    }

    public double getPowerFor(LivingEntity livingEntity) {
        var powerAttribute = this.powerAttribute.orElse(null);
        if (powerAttribute != null) {
            return livingEntity.getAttributeValue(powerAttribute);
        } else {
            return 1;
        }
    }

    public SoundEvent getCastSound() {
        return defaultCastSound.resolve().get();
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public boolean isFocus(ItemStack itemStack) {
        return itemStack.is(focus);
    }

    public Vector3f getTargetingColor() {
        return Utils.deconstructRGB(this.displayStyle.getColor().getValue());
    }
}
