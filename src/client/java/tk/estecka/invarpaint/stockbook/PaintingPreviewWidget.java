package tk.estecka.invarpaint.stockbook;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Identifier;


public class PaintingPreviewWidget
implements Drawable
{
	static private final Identifier CHECKER_TEX = new Identifier("invarpaint", "textures/gui/stockbook/checker.png");

	private int menuX, menuY, menuSize;
	private int pixelX, pixelY, pixelSize;

	private int tilesHorizontal, tilesVertical;
	private int checkerX, checkerY, checkerW, checkerH;

	private @Nullable PaintingVariant variant;
	private @Nullable Sprite sprite;
	private int paintX, paintY, paintW, paintH;

	public PaintingPreviewWidget(int size){
		this.menuSize = size;
	}


	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta){
		if (this.sprite == null)
			return;

		// Uses real pixel coordinates to increase precision.
		final float guiScale = (float)(1f/MinecraftClient.getInstance().getWindow().getScaleFactor());
		final MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(guiScale, guiScale, 1);

		context.drawTexture(CHECKER_TEX, checkerX,checkerY, checkerW,checkerH, +0.5f,+0.5f, tilesHorizontal,tilesVertical, 2,2);
		context.drawSprite(paintX,paintY, 0, paintW,paintH, sprite);

		matrices.pop();
	}

	public void Bake(){
		if (this.variant == null){
			this.sprite = null;
			return;
		}

		this.sprite = MinecraftClient.getInstance().getPaintingManager().getPaintingSprite(this.variant);

		int varW = variant.getWidth()  / 16;
		int varH = variant.getHeight() / 16;

		this.tilesHorizontal = varW + 1;
		this.tilesVertical   = varH + 1;
		int maxTiles = Math.max(tilesHorizontal, tilesVertical);

		// Uses real pixel coordinates to increase precision.
		final int guiScale = (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
		this.pixelSize = guiScale * this.menuSize;
		this.pixelX = guiScale * this.menuX;
		this.pixelY = guiScale * this.menuY;

		this.checkerW = pixelSize * (tilesHorizontal) / maxTiles;
		this.checkerH = pixelSize * (tilesVertical  ) / maxTiles;
		this.checkerX = (pixelSize - checkerW) / 2 + pixelX;
		this.checkerY = (pixelSize - checkerH) / 2 + pixelY;
		
		int halfTile = Math.round(pixelSize / (2*maxTiles));
		this.paintX = checkerX + halfTile;
		this.paintY = checkerY + halfTile;
		this.paintW = checkerW - 2*halfTile;
		this.paintH = checkerH - 2*halfTile;
	}


	public void SetPos(int x, int y){
		this.menuX = x;
		this.menuY = y;
		this.Bake();
	}

	public void SetVariant(PaintingVariant variant){
		if (this.variant != variant){
			this.variant = variant;
			this.sprite = (variant==null) ? null : MinecraftClient.getInstance().getPaintingManager().getPaintingSprite(variant);

			this.Bake();
		}
	}
}
