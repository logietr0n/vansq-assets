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
 * Vanilla sweep slash animation, scaled up for scythe reach. Visual only.
 */
@OnlyIn(Dist.CLIENT)
public class ScytheSweepParticle extends TextureSheetParticle {

    /** Vanilla sweep averages ~1.0; scythes use longer reach so the slash reads larger. */
    private static final float BASE_SIZE = 1.75F;

    private final SpriteSet sprites;

    private ScytheSweepParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.lifetime = 4;
        float shade = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;
        this.quadSize = BASE_SIZE;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = ((float) this.age + partialTick) / (float) this.lifetime;
        return this.quadSize * (1.0F - progress * progress * 0.5F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
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
            return new ScytheSweepParticle(level, x, y, z, this.sprites);
        }
    }
}
