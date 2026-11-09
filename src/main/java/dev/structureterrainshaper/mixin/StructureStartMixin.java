package dev.structureterrainshaper.mixin;

import dev.structureterrainshaper.ConfigStore;
import dev.structureterrainshaper.StructureTerrainShaper;
import dev.structureterrainshaper.worldgen.BaseAlignMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
 @Shadow public abstract Structure getStructure();
 @Shadow public abstract List<StructurePiece> getPieces();
 @Inject(method="placeInChunk", at=@At("TAIL"))
 private void sts$terrain(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
  try {
   String id=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(getStructure()).toString();
   if (!"base_align".equals(ConfigStore.get().overrideFor(id))) return;
   for (StructurePiece piece:getPieces()) sts$piece(level,piece,chunkBox,ConfigStore.get().blendDistance(),id.hashCode());
  } catch(Exception e) { StructureTerrainShaper.LOGGER.error("Base align terrain shaping failed",e); }
 }
 private static void sts$piece(WorldGenLevel l, StructurePiece p, BoundingBox clip, int reach, int salt) {
  BoundingBox b=p.getBoundingBox(); int x0=Math.max(b.minX(),clip.minX()),x1=Math.min(b.maxX(),clip.maxX()),z0=Math.max(b.minZ(),clip.minZ()),z1=Math.min(b.maxZ(),clip.maxZ());
  if(x0>x1||z0>z1)return;
  Map<Long,Integer> base=new HashMap<>(); BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=b.minY();y<=b.maxY();y++){pos.set(x,y,z);BlockState s=l.getBlockState(pos);if(s.isSolid()&&!s.isAir()){base.put(key(x,z),y);break;}}
  if(base.isEmpty())return;
  List<Map.Entry<Long,Integer>> edge=new ArrayList<>();
  for(var e:base.entrySet()){int x=xx(e.getKey()),z=zz(e.getKey());if(!base.containsKey(key(x+1,z))||!base.containsKey(key(x-1,z))||!base.containsKey(key(x,z+1))||!base.containsKey(key(x,z-1)))edge.add(e);}
  long seed=l.getSeed()^salt; int minY=l.getMinY();
  int ax=Math.max(clip.minX(),b.minX()-reach),bx=Math.min(clip.maxX(),b.maxX()+reach),az=Math.max(clip.minZ(),b.minZ()-reach),bz=Math.min(clip.maxZ(),b.maxZ()+reach);
  for(int x=ax;x<=bx;x++)for(int z=az;z<=bz;z++){
   double dBest=Double.MAX_VALUE;int edgeY=0;for(var e:edge){double d=Math.hypot(x-xx(e.getKey()),z-zz(e.getKey()));if(d<dBest){dBest=d;edgeY=e.getValue();}}if(dBest>reach)continue;
   Integer inside=base.get(key(x,z));int ground;
   if(inside!=null){ground=minY;for(int y=inside-1;y>=minY;y--){pos.set(x,y,z);BlockState s=l.getBlockState(pos);if(s.isSolid()&&!s.isAir()){ground=y+1;break;}}}
   else ground=l.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z);
   int top=(int)Math.floor(BaseAlignMath.blendHeight(edgeY,ground,dBest,reach,seed,x,z));top=Math.min(top,edgeY-1);if(top<ground||ground<=minY)continue;
   pos.set(x,ground-1,z);BlockState soil=l.getBlockState(pos);if(!soil.isSolid()||soil.isAir())soil=Blocks.DIRT.defaultBlockState();pos.set(x,Math.max(minY,ground-2),z);BlockState subsoil=l.getBlockState(pos);if(!subsoil.isSolid()||subsoil.isAir())subsoil=Blocks.DIRT.defaultBlockState();
   for(int y=ground;y<=top;y++){pos.set(x,y,z);if(l.getBlockState(pos).isAir())l.setBlock(pos,y==ground?soil:subsoil,2);}
  }
 }
 private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);} private static int xx(long k){return(int)(k>>32);} private static int zz(long k){return(int)k;}
}


