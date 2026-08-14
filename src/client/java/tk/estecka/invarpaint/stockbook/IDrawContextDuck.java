package tk.estecka.invarpaint.stockbook;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

public interface IDrawContextDuck
{
	static public IDrawContextDuck Of(GuiGraphicsExtractor context){
		return (IDrawContextDuck)(Object)context;
	}

	public ClientTooltipPositioner invarpaint$GetTooltipPositioner();
	public void invarpaint$SetTooltipPositioner(ClientTooltipPositioner positioner);
}
