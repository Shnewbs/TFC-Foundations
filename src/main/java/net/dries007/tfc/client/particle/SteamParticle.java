/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;

import net.dries007.tfc.client.ClimateRenderCache;

public class SteamParticle extends SingleQuadParticle
{
    private SteamParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite)
    {
        super(level, x, y, z, sprite);
        setAlpha(0.2F);
        setLifetime((int) (12.0F / (random.nextFloat() * 0.9F + 0.1F)));

        final Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
        final float windStrength = wind.length();
        xd = xSpeed + (random.nextFloat() - 0.5f) * 0.02f + (wind.x * windStrength * 0.05f);
        yd = ySpeed + 0.1;
        zd = zSpeed + (random.nextFloat() - 0.5f) * 0.02f + (wind.y * windStrength + 0.05f * 0.05f);
    }

    @Override
    protected Layer getLayer()
    {
        return Layer.TRANSLUCENT;
    }

    public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random)
        {
            SteamParticle particle = new SteamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite.get(random));
            return particle;
        }
    }
}
