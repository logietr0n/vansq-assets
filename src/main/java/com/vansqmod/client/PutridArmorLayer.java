package com.vansqmod.client;

import com.vansqmod.entity.Putrid;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Draws vanilla armor on Putrid's geo bones. See {@link GeoZombieArmorLayer}.
 * The helmet follows the hunched head bone pivot in {@code putrid.geo.json}
 * ({@code [0, 23.5, -1]} instead of vanilla {@code [0, 24, 0]}).
 */
public class PutridArmorLayer extends GeoZombieArmorLayer<Putrid> {

    public PutridArmorLayer(GeoRenderer<Putrid> renderer) {
        super(renderer);
    }
}
