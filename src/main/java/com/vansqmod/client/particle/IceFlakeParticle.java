package com.vansqmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Non-black copy of JNE {@code BlackFlakeParticle} motion, using {@code vansqmod:ice_flake} sprites.
 */
@OnlyIn(Dist.CLIENT)
public class IceFlakeParticle extends TextureSheetParticle {

    private float rotSpeed;
    private final float particleRandom;
    private final float spinAcceleration;

    protected IceFlakeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSprite(sprites.get(this.random.nextInt(12), 12));
        this.rotSpeed = (float) Math.toRadians(this.random.nextBoolean() ? -50.0D : 50.0D);
        this.particleRandom = this.random.nextFloat();
        this.spinAcceleration = (float) Math.toRadians(this.random.nextBoolean() ? -5.0D : 5.0D);
        this.lifetime = 300;
        this.gravity = 7.5E-4F;
        this.quadSize = 0.1F;
        this.setSize(0.2F, 0.2F);
        this.friction = 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }
        if (this.removed) {
            return;
        }
        float age = 300 - this.lifetime;
        float progress = Math.min(age / 300.0F, 1.0F);
        double angle = Math.toRadians(this.particleRandom * 60.0F);
        double wind = Math.pow(progress, 1.25D) * 6.0D;
        this.xd += Math.cos(angle) * wind * 0.0325D;
        this.zd += Math.sin(angle) * wind * 0.0325D;
        this.yd = -0.2D;
        this.rotSpeed += this.spinAcceleration / 20.0F;
        this.oRoll = this.roll;
        this.roll += this.rotSpeed / 20.0F;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround || (this.lifetime < 299 && (this.xd == 0.0D || this.zd == 0.0D))) {
            this.remove();
        }
        if (!this.removed) {
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new IceFlakeParticle(level, x, y, z, this.sprites);
        }
    }
}
