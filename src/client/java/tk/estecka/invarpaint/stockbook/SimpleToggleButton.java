package tk.estecka.invarpaint.stockbook;

import java.util.function.Consumer;

import net.minecraft.client.gui.widget.ToggleButtonWidget;

public class SimpleToggleButton
extends ToggleButtonWidget
{
	private final Consumer<Boolean> onToggled;

	public SimpleToggleButton(int x, int y, int width, int height, boolean toggled, Consumer<Boolean> onToggled) {
		super(x, y, width, height, toggled);
		this.onToggled = onToggled;
	}

	@Override
	public boolean isSelected(){
		return this.isHovered();
	}

	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)){
			this.setToggled(!this.isToggled());
			this.onToggled.accept(this.isToggled());
			return true;
		}
		else
			return false;
	}
}
