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
 * Untinted falling-leaf particle matching VanillaBackport pale oak motion,
 * so hand-painted leaf textures show their true colors.
 */
@OnlyIn(Dist.CLIENT)
public class FallingLeafParticle extends TextureSheetParticle {

    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;

    private float rotSpeed;
    private final float spinAcceleration;
    private final float windBig;
    private final boolean swirl;
    private final double xaFlowScale;
    private final double zaFlowScale;
    private final double swirlPeriod;

    protected FallingLeafParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            SpriteSet sprites,
            float gravityScale,
            float windBig,
            boolean swirl,
            float sizeScale,
            float fallSpeed
    ) {
        super(level, x, y, z);
        this.rotSpeed = (float) Math.toRadians(this.random.nextBoolean() ? -30.0D : 30.0D);
        this.spinAcceleration = (float) Math.toRadians(this.random.nextBoolean() ? -5.0D : 5.0D);
        this.setSprite(sprites.get(this.random.nextInt(4), 4));
        this.windBig = windBig;
        this.swirl = swirl;
        this.lifetime = INITIAL_LIFETIME;
        this.gravity = gravityScale * 1.2F * ACCELERATION_SCALE;
        float size = sizeScale * (this.random.nextBoolean() ? 0.05F : 0.075F);
        this.quadSize = size;
        this.setSize(size, size);
        this.friction = 1.0F;
        this.yd = -fallSpeed;

        float angleSeed = this.random.nextFloat();
        this.xaFlowScale = Math.cos(Math.toRadians(angleSeed * 60.0F)) * windBig;
        this.zaFlowScale = Math.sin(Math.toRadians(angleSeed * 60.0F)) * windBig;
        this.swirlPeriod = Math.toRadians(1000.0F + angleSeed * 3000.0F);
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

        float age = INITIAL_LIFETIME - this.lifetime;
        float progress = Math.min(age / (float) INITIAL_LIFETIME, 1.0F);
        double flowX = 0.0D;
        double flowZ = 0.0D;
        if (this.swirl) {
            flowX += progress * Math.cos(progress * this.swirlPeriod) * this.windBig;
            flowZ += progress * Math.sin(progress * this.swirlPeriod) * this.windBig;
        }

        this.xd += flowX * ACCELERATION_SCALE;
        this.zd += flowZ * ACCELERATION_SCALE;
        this.yd -= this.gravity;
        this.rotSpeed += this.spinAcceleration / 20.0F;
        this.oRoll = this.roll;
        this.roll += this.rotSpeed / 20.0F;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround || (this.lifetime < INITIAL_LIFETIME - 1 && (this.xd == 0.0D || this.zd == 0.0D))) {
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
            return new FallingLeafParticle(level, x, y, z, this.sprites, 0.07F, 10.0F, true, 2.0F, 0.021F);
        }
    }
}
