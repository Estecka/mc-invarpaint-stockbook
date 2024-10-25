package tk.estecka.invarpaint.stockbook.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import tk.estecka.invarpaint.stockbook.IDrawContextDuck;

@Mixin(DrawContext.class)
public class DrawContextMixin
implements IDrawContextDuck
{
	@Unique
	private @NotNull TooltipPositioner tooltipPositioner = HoveredTooltipPositioner.INSTANCE;

	@Override
	public @NotNull TooltipPositioner invarpaint$GetTooltipPositioner(){
		return this.tooltipPositioner;
	}

	@Override
	public void invarpaint$SetTooltipPositioner(@NotNull TooltipPositioner value){
		this.tooltipPositioner = value;
	}

	@ModifyArg(
		require=3,
		method={
			"drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V",
			"drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/util/Identifier;)V",
			"drawOrderedTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/util/Identifier;)V",
		},
		index=4,
		at=@At(value="INVOKE", target="net/minecraft/client/gui/DrawContext.drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V")
	)
	private TooltipPositioner UseCustomPositioner(TooltipPositioner original){
		return this.tooltipPositioner;
	}


}
