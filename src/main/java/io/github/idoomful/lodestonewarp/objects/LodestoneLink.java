package io.github.idoomful.lodestonewarp.objects;

import io.github.idoomful.lodestonewarp.data.WarpingLodestone;

public class LodestoneLink {
    private WarpingLodestone origin;
    private WarpingLodestone destionation;
    private boolean isGlobal = false;

    public LodestoneLink(WarpingLodestone origin) {
        this.origin = origin;
    }

    public WarpingLodestone getOrigin() {
        return origin;
    }
    public void setOrigin(WarpingLodestone origin) {
        this.origin = origin;
    }
    public WarpingLodestone getDestionation() {
        return destionation;
    }
    public void setDestionation(WarpingLodestone destionation) {
        this.destionation = destionation;
    }
    public boolean isGlobal() {
        return isGlobal;
    }
    public void toggleGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
}
