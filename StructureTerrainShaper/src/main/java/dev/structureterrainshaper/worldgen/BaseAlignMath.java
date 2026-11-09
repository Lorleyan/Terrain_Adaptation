package dev.structureterrainshaper.worldgen;

public final class BaseAlignMath {
    private BaseAlignMath() {}
    private static double smooth(double t) { t=Math.max(0,Math.min(1,t)); return t*t*(3-2*t); }
    public static double blendHeight(double edgeY,double terrainY,double distance,int reach,long seed,int x,int z) {
        double w=1.0-smooth(distance/Math.max(1,reach));
        return terrainY+(edgeY+relief(seed,x,z)*Math.min(1.25,w*1.25)-terrainY)*w;
    }
    private static double relief(long seed,int x,int z) {
        int cell=5,x0=Math.floorDiv(x,cell),z0=Math.floorDiv(z,cell);
        double fx=smooth(Math.floorMod(x,cell)/(double)cell),fz=smooth(Math.floorMod(z,cell)/(double)cell);
        return lerp(lerp(hash(seed,x0,z0),hash(seed,x0+1,z0),fx),lerp(hash(seed,x0,z0+1),hash(seed,x0+1,z0+1),fx),fz);
    }
    private static double hash(long seed,int x,int z) {
        long n=seed^(long)x*0x9E3779B97F4A7C15L^(long)z*0xC2B2AE3D27D4EB4FL;
        n^=n>>>30;n*=0xBF58476D1CE4E5B9L;n^=n>>>27;n*=0x94D049BB133111EBL;n^=n>>>31;
        return ((n>>>11)*0x1.0p-53)*2-1;
    }
    private static double lerp(double a,double b,double t){return a+(b-a)*t;}
}
