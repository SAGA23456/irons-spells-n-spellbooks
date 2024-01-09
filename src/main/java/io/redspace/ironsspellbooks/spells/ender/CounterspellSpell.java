package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

@AutoSpellConfig
public class CounterspellSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "counterspell");

    public CounterspellSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(10)
            .build();

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 start = entity.getEyePosition();
        Vec3 end = start.add(entity.getForward().normalize().scale(80));
        HitResult hitResult = Utils.raycastForEntity(entity.level, entity, start, end, true, 0.35f, Utils::validAntiMagicTarget);
        Vec3 forward = entity.getForward().normalize();
        if (hitResult instanceof EntityHitResult entityHitResult) {
            double distance = entity.distanceTo(entityHitResult.getEntity());
            for (float i = 1; i < distance; i += .5f) {
                Vec3 pos = entity.getEyePosition().add(forward.scale(i));
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
            }
            if (entityHitResult.getEntity() instanceof AntiMagicSusceptible antiMagicSusceptible && !(antiMagicSusceptible instanceof MagicSummon summon && summon.getSummoner() == entity)) {
                antiMagicSusceptible.onAntiMagic(playerMagicData);
            } else if (entityHitResult.getEntity() instanceof ServerPlayer serverPlayer) {
                Utils.serverSideCancelCast(serverPlayer, true);
                MagicData.getPlayerMagicData(serverPlayer).getPlayerRecasts().removeAll(RecastResult.COUNTERSPELL);
            } else if (entityHitResult.getEntity() instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
                abstractSpellCastingMob.cancelCast();
            }
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                for (MobEffectInstance mobEffect : livingEntity.getActiveEffects()) {
                    if (mobEffect.getEffect() instanceof MagicMobEffect magicMobEffect) {
                        livingEntity.removeEffect(magicMobEffect);
                    }
                }
            }
        } else {
            for (float i = 1; i < 40; i += .5f) {
                Vec3 pos = entity.getEyePosition().add(forward.scale(i));
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
                if (!world.getBlockState(new BlockPos(pos)).isAir()) {
                    break;
                }
            }
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}