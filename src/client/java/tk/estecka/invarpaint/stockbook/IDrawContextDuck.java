package tk.estecka.invarpaint.stockbook;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipPositioner;

public interface IDrawContextDuck
{
	static public IDrawContextDuck Of(DrawContext context){
		return (IDrawContextDuck)(Object)context;
	}

	public TooltipPositioner invarpaint$GetTooltipPositioner();
	public void invarpaint$SetTooltipPositioner(TooltipPositioner positioner);
}
