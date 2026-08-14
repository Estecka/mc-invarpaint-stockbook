package tk.estecka.invarpaint.stockbook.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tk.estecka.invarpaint.stockbook.IDrawContextDuck;

@Mixin(GuiGraphicsExtractor.class)
public class DrawContextMixin
implements IDrawContextDuck
{
	@Unique
	private @NotNull ClientTooltipPositioner tooltipPositioner = DefaultTooltipPositioner.INSTANCE;

	@Override
	public @NotNull ClientTooltipPositioner invarpaint$GetTooltipPositioner(){
		return this.tooltipPositioner;
	}

	@Override
	public void invarpaint$SetTooltipPositioner(@NotNull ClientTooltipPositioner value){
		this.tooltipPositioner = value;
	}

	/**
	 * Tries to redirect all instances of `HoveredTooltipPositioner.INSTANCE`
	 */
	@ModifyArg(
		require=3,
		method={
			"setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
			"setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V",
			"setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V",
		},
		index=4,
		at=@At(value="INVOKE", target="net/minecraft/client/gui/GuiGraphicsExtractor.setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V")
	)
	private ClientTooltipPositioner UseCustomPositioner(ClientTooltipPositioner original){
		return this.tooltipPositioner;
	}


}
