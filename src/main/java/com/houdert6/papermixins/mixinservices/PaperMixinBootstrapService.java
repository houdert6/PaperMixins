package com.houdert6.papermixins.mixinservices;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class PaperMixinBootstrapService implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "PaperMixins";
    }

    @Override
    public String getServiceClassName() {
        return PaperMixinService.class.getName();
    }

    @Override
    public void bootstrap() {

    }
}
