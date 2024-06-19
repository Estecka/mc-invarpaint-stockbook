package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import tk.estecka.invarpaint.core.PaintStackUtil;


@Environment(EnvType.CLIENT)
public class StockbookScreen
extends HandledScreen<AStockbookHandler>
{
	static private final Identifier BACKGROUND = Identifier.of("invarpaint", "textures/gui/stockbook/background.png");
	static private final Identifier FULL_SLOT  = Identifier.of("invarpaint", "textures/gui/stockbook/full_slot.png" );
	static private final Identifier STOCK_SLOT = Identifier.of("invarpaint", "textures/gui/stockbook/stock.png"     );
	static private final Identifier SCROLLBAR  = Identifier.of("invarpaint", "textures/gui/stockbook/scrollbar.png" );

	// Slot count
	static public final int GRID_W=5, GRID_H=4;
	static public final int GRID_SLOT_COUNT = GRID_W * GRID_H;

	// Pixel measurements
	static private final int GRID_X=15, GRID_Y=31;
	static private final int SLOT_W=26, SLOT_H=26;
	static private final int PLAYER_X=10, PLAYER_Y=148;
	static private final int HOTBAR_X=10, HOTBAR_Y=206;
	static private final int PREVIEW_X=188, PREVIEW_Y=13;
	static private final int PREVIEW_SIZE = 123;
	static private final int SCROLLBAR_MIN_H = 8;
	static private final int RAIL_X=153, RAIL_Y=32, RAIL_W=12, RAIL_H=101;
	static private final int SEARCH_X=31, SEARCH_Y=16, SEARCH_W=113, SEARCH_H=12;

	protected final StockbookClientHandler handler;
	protected final Registry<PaintingVariant> paintingRegistry;

	// Widgets
	private final TextFieldWidget searchBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, SEARCH_W, SEARCH_H, Text.literal("Search"));
	private final List<StockbookSlot> searchResults = new ArrayList<>();
	private final PaintingPreviewWidget preview = new PaintingPreviewWidget(PREVIEW_SIZE);

	// The amount of slots in the book, the last time the layout was updated.
	private int knownSlots = 0;

	// Quick-move higlight
	static private final float ANIM_DURATION_MAX = 6f;
	static private final float ANIM_SCALE_BONUS = 0.5f;
	private float animRemainingTime = 0;
	private @Nullable Slot highlighted = null;

	// scrollbar
	private int linesScrolled = 0;
	private int linesScrolledMax = 0;
	private Rect2i scrollbar = new Rect2i(0,0,0,0);
	private boolean isScrolling = false;

	static public void Register(){
		HandledScreens.<AStockbookHandler,StockbookScreen>register(AStockbookHandler.TYPE, StockbookScreen::new);
		AStockbookHandler.clientFactory = StockbookClientHandler::new;
	}

	public StockbookScreen(StockbookClientHandler handler, PlayerInventory player, Text title){
		this((AStockbookHandler)handler, player, title);
	}
	private StockbookScreen(AStockbookHandler handler, PlayerInventory player, Text title){
		super(handler, player, title);
		this.paintingRegistry = player.player.getWorld().getRegistryManager().get(RegistryKeys.PAINTING_VARIANT);
		if (handler instanceof StockbookClientHandler clientHandler)
			this.handler = clientHandler;
		else
			throw new AssertionError("Created a screen with a non-client handler.");
	}


/******************************************************************************/
/* ## Layout Updates                                                          */
/******************************************************************************/

	@Override
	protected void init(){
		this.backgroundWidth  = 320;
		this.backgroundHeight = 230;
		super.init();

		this.searchBox.setX(this.x + SEARCH_X);
		this.searchBox.setY(this.y + SEARCH_Y);
		super.addDrawableChild(searchBox);

		this.preview.SetPos(this.x+PREVIEW_X, this.y+PREVIEW_Y);
		super.addDrawable(this.preview);

		this.UpdatePlayerSlots();
		this.UpdateSearchResults();
	}

	private void UpdatePlayerSlots(){
		int i = 0;

		for (int x=0; x<9; ++x,++i) {
			MovableSlot slot = handler.getSlot(i);
			slot.SetX(HOTBAR_X + x*18);
			slot.SetY(HOTBAR_Y);
		}

		for (int y=0; y<3; ++y)
		for (int x=0; x<9; ++x,++i)
		{
			MovableSlot slot = handler.getSlot(i);
			slot.SetX(PLAYER_X + x*18);
			slot.SetY(PLAYER_Y + y*18);
		}
	}

	private void UpdateSearchResults(){
		this.searchResults.clear();

		if (this.searchBox.getText().isBlank())
			searchResults.addAll(handler.bookSlots);
		else for (StockbookSlot slot : handler.bookSlots)
		{
			slot.SetVisible(false);

			final Language lang = Language.getInstance();
			final Identifier id = slot.GetVariant();
			final PaintingVariant variant = paintingRegistry.getOrEmpty(id).orElse(null);

			String name=null, author=null;
			if (id != null){
				name   = lang.get(id.toTranslationKey("painting", "title" ), null);
				author = lang.get(id.toTranslationKey("painting", "author"), null);
			}

			String size="0x0";
			if (variant != null)
				size = String.format("%dx%d", variant.width(), variant.height());

			String query = searchBox.getText().toLowerCase().trim();
			if (id.toString().contains(query)
			 || size.contains(query)
			 || (name   != null && name  .toLowerCase().contains(query))
			 || (author != null && author.toLowerCase().contains(query))
			) {
				searchResults.add(slot);
			}
		}

		this.searchResults.sort((a,b) -> {
			Identifier iA=a.GetVariant(), iB=b.GetVariant();
			return (iA == iB) ? 0
			     : (iA == null) ? -1
			     : (iB == null) ? 1
			     : iA.toString().compareTo(iB.toString())
			     ;
		});
		this.UpdateScrollability();
	}

	private void UpdateScrollability(){
		this.linesScrolledMax = (searchResults.size() / GRID_W) + 1 - GRID_H;
		this.linesScrolledMax = Math.max(0, linesScrolledMax);

		this.UpdateScrollbar();
	}

	private void	UpdateScrollbar(){
		this.linesScrolled = MathHelper.clamp(linesScrolled, 0, linesScrolledMax);

		int height = RAIL_H * GRID_H / (linesScrolledMax + GRID_H);
		height =  Math.max(height, SCROLLBAR_MIN_H);
		int offsetY = ((RAIL_H - height) * linesScrolled);
		if (linesScrolledMax != 0)
			offsetY /= linesScrolledMax;

		this.scrollbar.setX(this.x+RAIL_X);
		this.scrollbar.setY(this.y+RAIL_Y+offsetY);
		this.scrollbar.setWidth(RAIL_W);
		this.scrollbar.setHeight(height);

		this.UpdateGridView();
	}

	private void UpdateGridView(){
		for (int i=0; i<searchResults.size(); ++i)
		{
			MovableSlot slot = searchResults.get(i);

			int slotX = i % GRID_W;
			int slotY = i / GRID_W;
			slotY -= linesScrolled;
			slot.SetVisible( 0<=slotY && slotY<GRID_H );
			slot.SetX(GRID_X+4 + slotX*SLOT_W);
			slot.SetY(GRID_Y+4 + slotY*SLOT_H);
		}

	}

	public boolean ScrollTo(@NotNull Identifier variantId){
		int index = -1;

		for (int i=0; i<searchResults.size(); ++i)
		if  (variantId.equals(searchResults.get(i).GetVariant())) {
			this.highlighted = searchResults.get(i);
			this.animRemainingTime = ANIM_DURATION_MAX;
			index = i;
			break;
		}

		if (index < 0)
			return false;

		int line = index / GRID_W;
		this.linesScrolled = MathHelper.clamp(linesScrolled, line+1-GRID_H, line);
		this.UpdateScrollbar();

		this.preview.SetVariant(paintingRegistry.getOrEmpty(variantId).orElse(null));
		return true;
	}


/******************************************************************************/
/* ## Render                                                                  */
/******************************************************************************/

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta){
		if (this.knownSlots != handler.bookSlots.size()){
			this.knownSlots = handler.bookSlots.size();
			this.UpdateSearchResults();
			this.UpdateScrollability();
		}
		if (handler.requestedFocus != null && this.ScrollTo(handler.requestedFocus))
			handler.requestedFocus = null;

		super.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		super.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY){
		context.drawTexture(BACKGROUND, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
		this.RenderScrollbar(context);

		for (StockbookSlot slot : searchResults)
		if  (slot.isEnabled() && slot != highlighted)
			this.DrawSlotBackground(context, slot, delta);

		if (this.highlighted != null)
			this.DrawSlotBackground(context, highlighted, delta);
	}

	// Intentionally skips title draw from super.
	@Override
	protected void drawForeground(DrawContext context, int mouseX, int moueY){
		int lockId = handler.containerSlot.get();
		if (0 <= lockId && lockId < handler.slots.size()){
			Slot slot = handler.getSlot(lockId);
			context.drawTexture(STOCK_SLOT, slot.x-2, slot.y-2, 233, 0,0, 20,20, 20,20);
		}
	}

	private void DrawSlotBackground(DrawContext context, Slot slot, float delta){
		if (!slot.hasStack())
			return;

		int drawX = this.x+slot.x-5;
		int drawY = this.y+slot.y-5;
		int drawSize = 26;

		if (this.highlighted == slot){
			drawSize += drawSize * ANIM_SCALE_BONUS * this.animRemainingTime / ANIM_DURATION_MAX;
			int padding = (drawSize - 26) / 2;
			drawX -= padding;
			drawY -= padding;

			this.animRemainingTime -= delta;
			if (animRemainingTime <= 0)
				this.highlighted = null;
		}

		context.drawTexture(FULL_SLOT, drawX, drawY, 0,0, drawSize, drawSize, drawSize, drawSize);
	}

	@Override
	protected void	drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY){
		if (mouseY < (this.y + PREVIEW_Y + PREVIEW_SIZE)) {
			mouseX = this.x;
			mouseY += 16+12;
		}
		super.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected List<Text> getTooltipFromItem(ItemStack stack) {
		String variantName = PaintStackUtil.GetVariantName(stack);
		if (variantName != null)
			this.preview.SetVariant(paintingRegistry.getOrEmpty(Identifier.tryParse(variantName)).orElse(null));

		return super.getTooltipFromItem(stack);
	}

	private void	RenderScrollbar(DrawContext context){
		if (linesScrolledMax == 0)
			return;

		context.drawTexture(SCROLLBAR, scrollbar.getX(), scrollbar.getY(), 0,0, scrollbar.getWidth(),scrollbar.getHeight(), scrollbar.getWidth(),scrollbar.getHeight());
	}


/******************************************************************************/
/* ## Interactions                                                            */
/******************************************************************************/

	@Override
	public boolean charTyped(char c, int modifiers){
		boolean r = super.charTyped(c, modifiers);

		if (r && searchBox.isFocused())
			this.UpdateSearchResults();

		return r;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
			this.setFocused(searchBox);
		else if (this.searchBox.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE )
				this.setFocused(null);
			else if (searchBox.keyPressed(keyCode, scanCode, modifiers))
				this.UpdateSearchResults();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		this.linesScrolled -= (int)verticalAmount;
		this.UpdateScrollbar();
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button){
		if (button==0
		 && mouseX >= (this.x+RAIL_X)
		 && mouseX <  (this.x+RAIL_X+RAIL_W)
		 && mouseY >= (this.y+RAIL_Y)
		 && mouseY <  (this.y+RAIL_Y+RAIL_H)
		) {
			this.isScrolling = true;
			this.linesScrolled = (int)Math.round( linesScrolledMax * (mouseY - this.y - RAIL_Y) / RAIL_H );
			this.UpdateScrollbar();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button){
		if (button == 0)
			this.isScrolling = false;

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
		if (!this.isScrolling)
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		this.linesScrolled = (int)Math.round( linesScrolledMax * (mouseY - this.y - RAIL_Y) / RAIL_H );
		this.UpdateScrollbar();
		return true;
	}

}
