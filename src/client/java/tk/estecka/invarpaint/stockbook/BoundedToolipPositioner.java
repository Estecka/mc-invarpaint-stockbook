package tk.estecka.invarpaint.stockbook;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.client.gui.tooltip.TooltipPositioner;

public class BoundedToolipPositioner
implements TooltipPositioner
{
	public int rightBound;

	public BoundedToolipPositioner(int rightBound){
		this.rightBound = rightBound;
	}

	public Vector2ic getPosition(int screenWidth, int screenHeight, int mouseX, int mouseY, int tipWidth, int tipHeight){
		Vector2i pos = new Vector2i(mouseX+16, mouseY+16);
		int overflow;

		overflow = (pos.x + tipWidth) - rightBound;
		if (overflow > 0){
			pos.x -= overflow;
			if (pos.x < 0)
				pos.x = 0;
		}

		overflow = (pos.y + tipHeight) - screenHeight;
		if (overflow > 0){
			pos.y -= overflow;
			if (pos.y < 0)
				pos.y = 0;
		}

		return pos;
	}
	
}
