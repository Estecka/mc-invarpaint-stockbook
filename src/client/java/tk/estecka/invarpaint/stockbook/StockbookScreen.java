package tk.estecka.invarpaint.stockbook;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import fr.estecka.invarpaint.api.PaintStackUtil;


@Environment(EnvType.CLIENT)
public class StockbookScreen
extends HandledScreen<AStockbookHandler>
implements TooltipPositioner
{
	static private final Identifier BACKGROUND = Identifier.of("invarpaint", "stockbook/background");
	static private final Identifier FULL_SLOT  = Identifier.of("invarpaint", "stockbook/full_slot" );
	static private final Identifier STOCK_SLOT = Identifier.of("invarpaint", "stockbook/stock"     );
	static private final Identifier SCROLLBAR  = Identifier.of("invarpaint", "stockbook/scrollbar" );

	static private final ButtonTextures FILTER_TEXTURES = new ButtonTextures(
		Identifier.of("invarpaint", "stockbook/filter_enabled"),
		Identifier.of("invarpaint", "stockbook/filter_disabled"),
		Identifier.of("invarpaint", "stockbook/filter_enabled_highlighted"),
		Identifier.of("invarpaint", "stockbook/filter_disabled_highlighted")
	);

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
	static private final int SEARCH_X=31, SEARCH_Y=15, SEARCH_W=107, SEARCH_H=14;
	static private final int FILTER_X=139, FILTER_Y=14, FILTER_W=26, FILTER_H=16;
	static private final int TOOLTIP_X_MIN=10, TOOLTIP_X_MAX=169, TOOLTIP_PADDING=4;

	protected final StockbookClientHandler handler;
	protected final Registry<PaintingVariant> paintingRegistry;

	// Widgets
	private final TextFieldWidget searchBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, SEARCH_W, SEARCH_H, Text.literal("Search"));
	private final List<StockbookSlot> searchResults = new ArrayList<>();
	private final PaintingPreviewWidget preview = new PaintingPreviewWidget(PREVIEW_SIZE);
	private final SimpleToggleButton filterButton = new SimpleToggleButton(0, 0, FILTER_W, FILTER_H, false, b ->this.UpdateSearchResults());
	{
		searchBox.setPlaceholder(Text.translatable("gui.invarpaint.stockbook.search").formatted(Formatting.ITALIC, Formatting.GRAY));
		filterButton.setTextures(FILTER_TEXTURES);
		filterButton.SetToolTips(
			Tooltip.of(Text.translatable("gui.invapraint.stockbook.filter.stored")),
			Tooltip.of(Text.translatable("gui.invapraint.stockbook.filter.discovered"))
		);
	}

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
		this.paintingRegistry = player.player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.PAINTING_VARIANT);
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

		searchBox.setX(this.x + SEARCH_X);
		searchBox.setY(this.y + SEARCH_Y);
		this.addDrawableChild(searchBox);

		preview.SetPos(this.x+PREVIEW_X, this.y+PREVIEW_Y);
		this.addDrawable(this.preview);

		filterButton.setPosition(this.x+FILTER_X, this.y+FILTER_Y);
		this.addDrawableChild(filterButton);

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

		for (StockbookSlot slot : handler.bookSlots)
		{
			slot.SetVisible(false);
			if (MatchesSearch(slot))
				searchResults.add(slot);
		}

		this.SortSearchResult();
		this.UpdateScrollability();
	}

	private boolean MatchesSearch(StockbookSlot slot){
		if (filterButton.isToggled() && slot.getStack().isEmpty())
			return false;

		if (searchBox.getText().isBlank())
			return true;

		final Language lang = Language.getInstance();
		final Identifier id = slot.GetVariant();
		final PaintingVariant variant = paintingRegistry.getOptionalValue(id).orElse(null);

		String name=null, author=null;
		if (id != null){
			name   = lang.get(id.toTranslationKey("painting", "title" ), null);
			author = lang.get(id.toTranslationKey("painting", "author"), null);
		}

		String size="0x0";
		if (variant != null)
			size = String.format("%dx%d", variant.width(), variant.height());

		String query = searchBox.getText().toLowerCase().trim();
		return id.toString().contains(query)
		    || size.contains(query)
		    || (name   != null && name  .toLowerCase().contains(query))
		    || (author != null && author.toLowerCase().contains(query))
		    ;
	}

	private void SortSearchResult(){
		this.searchResults.sort((a,b) -> {
			Identifier iA=a.GetVariant(), iB=b.GetVariant();
			return (iA == iB)   ?  0
			     : (iA == null) ? -1
			     : (iB == null) ? +1
			     : iA.toString().compareTo(iB.toString())
			     ;
		});
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
		StockbookSlot slot = null;
		int index = -1;

		for (int i=0; i<handler.bookSlots.size(); ++i)
		if  (variantId.equals(handler.bookSlots.get(i).GetVariant())) {
			slot = handler.bookSlots.get(i);
			break;
		}

		if (slot == null)
			return false;

		if (!searchResults.contains(slot)){
			searchResults.add(slot);
			this.SortSearchResult();
		}

		this.highlighted = slot;
		this.animRemainingTime = ANIM_DURATION_MAX;
		index = searchResults.indexOf(slot);

		int line = index / GRID_W;
		this.linesScrolled = MathHelper.clamp(linesScrolled, line+1-GRID_H, line);
		this.UpdateScrollability();

		this.preview.SetVariant(paintingRegistry.getOptionalValue(variantId).orElse(null));
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

		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY){
		context.drawGuiTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x, this.y, this.backgroundWidth, this.backgroundHeight);
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
			context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, STOCK_SLOT, slot.x-2, slot.y-2, 233, 20, 20);
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

		context.drawGuiTexture(RenderLayer::getGuiTextured, FULL_SLOT, drawX, drawY, drawSize, drawSize);
	}

	@Override
	protected void	drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY){
		var contextpp = IDrawContextDuck.Of(context);

		if (mouseY < (this.y + PREVIEW_Y + PREVIEW_SIZE))
			contextpp.invarpaint$SetTooltipPositioner(this);

		super.drawMouseoverTooltip(context, mouseX, mouseY);
		contextpp.invarpaint$SetTooltipPositioner(HoveredTooltipPositioner.INSTANCE);
	}

	// Tooltip Positioner
	@Override
	public Vector2ic getPosition(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight){
		Vector2i pos = new Vector2i(
			this.x + TOOLTIP_X_MIN + TOOLTIP_PADDING,
			mouseY + 16
		);
		int overflow;

		// x
		overflow = mouseX - (pos.x + tooltipWidth);
		if (overflow > 0)
			pos.x += overflow;

		overflow = (pos.x + tooltipWidth) - (this.x + TOOLTIP_X_MAX - TOOLTIP_PADDING);
		if (overflow > 0)
			pos.x -= overflow;

		if (pos.x < TOOLTIP_PADDING)
			pos.x = TOOLTIP_PADDING;

		// y
		overflow = (pos.y + tooltipHeight) - screenHeight;
		if (overflow > 0){
			pos.y -= overflow;
			if (pos.y < 0)
				pos.y = 0;
		}

		return pos;
	}

	@Override
	protected List<Text> getTooltipFromItem(ItemStack stack) {
		String variantName = PaintStackUtil.GetVariantName(stack);
		if (variantName != null)
			this.preview.SetVariant(paintingRegistry.getOptionalValue(Identifier.tryParse(variantName)).orElse(null));

		return super.getTooltipFromItem(stack);
	}

	private void	RenderScrollbar(DrawContext context){
		if (linesScrolledMax == 0)
			return;

		context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLBAR, scrollbar.getX(), scrollbar.getY(), scrollbar.getWidth(), scrollbar.getHeight());
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
		this.setFocused(null);
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
