package com.vansqmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RhodoheartRainParticle extends SimpleAnimatedParticle {

    public RhodoheartRainParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0125F);
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        this.quadSize *= 2.0F;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new RhodoheartRainParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
        }
    }
}
