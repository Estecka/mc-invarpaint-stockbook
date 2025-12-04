package tk.estecka.invarpaint.stockbook;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ToggleButtonWidget;

public class SimpleToggleButton
extends ToggleButtonWidget
{
	private final Consumer<Boolean> onToggled;
	private @Nullable Tooltip enabledTtooltip, disabledTooltip;

	public SimpleToggleButton(int x, int y, int width, int height, boolean toggled, Consumer<Boolean> onToggled) {
		super(x, y, width, height, toggled);
		this.onToggled = onToggled;
	}

	public void SetToolTips(Tooltip enabled, Tooltip disabled){
		this.enabledTtooltip = enabled;
		this.disabledTooltip = disabled;
		this.setTooltip(this.isToggled() ? enabled : disabled);
	}

	@Override
	public boolean isSelected(){
		return this.isHovered();
	}

	@Override
	public void setToggled(boolean enabled){
		super.setToggled(enabled);
		this.setTooltip(enabled ? enabledTtooltip : disabledTooltip);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (super.mouseClicked(click, doubled)){
			this.setToggled(!this.isToggled());
			this.onToggled.accept(this.isToggled());
			return true;
		}
		else
			return false;
	}
}
