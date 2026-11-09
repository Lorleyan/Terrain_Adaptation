package dev.structureterrainshaper.client;

import dev.structureterrainshaper.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.Structure;
import java.util.*;

public final class StructureListScreen extends Screen {
    private final Screen parent;
    private final List<Row> rows=new ArrayList<>();
    private final Set<String> selected=new HashSet<>();
    private final List<Button> rowButtons=new ArrayList<>();
    private EditBox search;
    private Button bulkButton;
    private String query="";
    private int page;
    private static final int PER_PAGE=8;
    public StructureListScreen(Screen parent){super(Component.literal("Structure Terrain Shaper"));this.parent=parent;}
    @Override protected void init(){
        rows.clear();
        var level=Minecraft.getInstance().level;
        if(level!=null) level.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().forEach(h->rows.add(new Row(h.key().location().toString(),h.value().terrainAdaptation().toString())));
        rows.sort(Comparator.comparing(Row::id));
        search=addRenderableWidget(new EditBox(font,16,34,width-32,20,Component.literal("Search structure or namespace")));
        search.setValue(query);
        search.setResponder(v->{query=v;page=0;updateRows();});
        bulkButton=addRenderableWidget(Button.builder(Component.literal("Apply Base align to selected"),b->bulk("base_align")).bounds(16,59,Math.min(300,width-32),20).build());
        addRenderableWidget(Button.builder(Component.literal("Clear overrides"),b->bulk("inherit")).bounds(Math.min(324,width-110),59,94,20).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);updateRows();}).bounds(width-76,height-28,26,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;updateRows();}).bounds(width-44,height-28,26,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(16,height-28,70,20).build());
        addRenderableWidget(Button.builder(Component.literal("Blend reach: "+ConfigStore.get().blendDistance()+" blocks"),b->{int n=ConfigStore.get().blendDistance()+2;if(n>24)n=2;ConfigStore.update(ConfigStore.get().withBlendDistance(n));b.setMessage(Component.literal("Blend reach: "+n+" blocks"));}).bounds(94,height-28,180,20).build());
        updateRows();
    }
    private void updateRows(){
        for(Button b:rowButtons)removeWidget(b); rowButtons.clear();
        if(bulkButton!=null)bulkButton.setMessage(Component.literal("Apply Base align to selected ("+selected.size()+")"));
        int start=page*PER_PAGE, shown=0;
        for(Row r:rows){if(!r.id.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;if(shown++<start)continue;if(shown>start+PER_PAGE)break;
            int y=88+(shown-start-1)*25;
            rowButtons.add(addRenderableWidget(Button.builder(Component.literal((selected.contains(r.id)?"☑ ":"☐ ")+r.id.replace(":"," / ")),b->{if(!selected.add(r.id))selected.remove(r.id);updateRows();}).bounds(16,y,Math.max(180,width-255),21).build()));
            rowButtons.add(addRenderableWidget(Button.builder(Component.literal((r.adaptation.equalsIgnoreCase("NONE")?"  ":"✓ ")+r.adaptation),b->{}).bounds(Math.max(200,width-230),y,105,21).build()));
            rowButtons.add(addRenderableWidget(Button.builder(Component.literal(ConfigStore.get().overrideFor(r.id)),b->{String n=ConfigStore.get().overrideFor(r.id).equals("base_align")?"inherit":"base_align";ConfigStore.update(ConfigStore.get().withOverride(r.id,n));b.setMessage(Component.literal(n));}).bounds(Math.max(310,width-120),y,104,21).build()));
        }
    }
    private void bulk(String value){ConfigStore.Config c=ConfigStore.get();for(String id:selected)c=c.withOverride(id,value);ConfigStore.update(c);updateRows();}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){renderBackground(g,mx,my,delta);g.drawCenteredString(font,title,width/2,12,0xFFFFFFFF);g.drawString(font,"Warning: Base align can spoil structures intended to float.",16,25,0xFFFFAA55);g.drawString(font,"Structure / namespace | Current adaptation | Override",16,80,0xFFCCCCCC);if(Minecraft.getInstance().level==null)g.drawString(font,"Open this screen from an active world to load its structure list.",16,115,0xFFFFCC55);super.render(g,mx,my,delta);}
    @Override public void onClose(){Minecraft.getInstance().setScreen(parent);}
    private record Row(String id,String adaptation){}
}
