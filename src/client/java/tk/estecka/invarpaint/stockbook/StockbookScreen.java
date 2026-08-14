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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import fr.estecka.invarpaint.api.PaintStackUtil;


@Environment(EnvType.CLIENT)
public class StockbookScreen
extends AbstractContainerScreen<AStockbookHandler>
implements ClientTooltipPositioner
{
	static private final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("invarpaint", "stockbook/background");
	static private final Identifier FULL_SLOT  = Identifier.fromNamespaceAndPath("invarpaint", "stockbook/full_slot" );
	static private final Identifier STOCK_SLOT = Identifier.fromNamespaceAndPath("invarpaint", "stockbook/stock"     );
	static private final Identifier SCROLLBAR  = Identifier.fromNamespaceAndPath("invarpaint", "stockbook/scrollbar" );

	static private final WidgetSprites FILTER_TEXTURES = new WidgetSprites(
		Identifier.fromNamespaceAndPath("invarpaint", "stockbook/filter_enabled"),
		Identifier.fromNamespaceAndPath("invarpaint", "stockbook/filter_disabled"),
		Identifier.fromNamespaceAndPath("invarpaint", "stockbook/filter_enabled_highlighted"),
		Identifier.fromNamespaceAndPath("invarpaint", "stockbook/filter_disabled_highlighted")
	);
	static private final Tooltip FILTER_TOOLTIP_ON  = Tooltip.create(Component.translatable("gui.invapraint.stockbook.filter.stored"));
	static private final Tooltip FILTER_TOOLTIP_OFF = Tooltip.create(Component.translatable("gui.invapraint.stockbook.filter.discovered"));

	// Slot count
	static public final int GRID_W=5, GRID_H=4;
	static public final int GRID_SLOT_COUNT = GRID_W * GRID_H;

	// Pixel measurements
	static private final int BG_WIDTH=320, BG_HEIGHT=230;
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
	private final EditBox searchBox = new EditBox(Minecraft.getInstance().font, 0, 0, SEARCH_W, SEARCH_H, Component.literal("Search"));
	private final List<StockbookSlot> searchResults = new ArrayList<>();
	private final PaintingPreviewWidget preview = new PaintingPreviewWidget(PREVIEW_SIZE);
	private final CycleButton<Boolean> filterButton = CycleButton.onOffBuilder(false)
		.withSprite( (button,value)->FILTER_TEXTURES.get(value, button.isHoveredOrFocused()) )
		.displayState(CycleButton.DisplayState.HIDE)
		.withTooltip(enabled -> enabled ? FILTER_TOOLTIP_ON : FILTER_TOOLTIP_OFF)
		.create(leftPos, topPos, FILTER_W, FILTER_H, CommonComponents.EMPTY, (button,value)->this.UpdateSearchResults())
		;
	{
		searchBox.setHint(Component.translatable("gui.invarpaint.stockbook.search").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
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
		MenuScreens.<AStockbookHandler,StockbookScreen>register(AStockbookHandler.TYPE, StockbookScreen::new);
		AStockbookHandler.clientFactory = StockbookClientHandler::new;
	}

	public StockbookScreen(StockbookClientHandler handler, Inventory player, Component title){
		this((AStockbookHandler)handler, player, title);
	}
	private StockbookScreen(AStockbookHandler handler, Inventory player, Component title){
		super(handler, player, title, BG_WIDTH, BG_HEIGHT);
		this.paintingRegistry = player.player.level().registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT);
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
		super.init();

		searchBox.setX(this.leftPos + SEARCH_X);
		searchBox.setY(this.topPos + SEARCH_Y);
		this.addRenderableWidget(searchBox);

		preview.SetPos(this.leftPos+PREVIEW_X, this.topPos+PREVIEW_Y);
		this.addRenderableOnly(this.preview);

		filterButton.setPosition(this.leftPos+FILTER_X, this.topPos+FILTER_Y);
		this.addRenderableWidget(filterButton);

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
		if (filterButton.getValue() && slot.getItem().isEmpty())
			return false;

		if (searchBox.getValue().isBlank())
			return true;

		final Language lang = Language.getInstance();
		final PaintingEntry entry = slot.GetVariant();
		final PaintingVariant variant = (entry!=null) ? entry.value() : null;

		String name=null, author=null;
		if (entry != null){
			name   = lang.getOrDefault(entry.id.toLanguageKey("painting", "title" ), null);
			author = lang.getOrDefault(entry.id.toLanguageKey("painting", "author"), null);
		}

		String size="0x0";
		if (variant != null)
			size = String.format("%dx%d", variant.width(), variant.height());

		String query = searchBox.getValue().toLowerCase().trim();
		return entry.toString().contains(query)
		    || size.contains(query)
		    || (name   != null && name  .toLowerCase().contains(query))
		    || (author != null && author.toLowerCase().contains(query))
		    ;
	}

	private void SortSearchResult(){
		this.searchResults.sort((a,b) -> {
			PaintingEntry iA=a.GetVariant(), iB=b.GetVariant();
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
		this.linesScrolled = Mth.clamp(linesScrolled, 0, linesScrolledMax);

		int height = RAIL_H * GRID_H / (linesScrolledMax + GRID_H);
		height =  Math.max(height, SCROLLBAR_MIN_H);
		int offsetY = ((RAIL_H - height) * linesScrolled);
		if (linesScrolledMax != 0)
			offsetY /= linesScrolledMax;

		this.scrollbar.setX(this.leftPos+RAIL_X);
		this.scrollbar.setY(this.topPos+RAIL_Y+offsetY);
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

	public boolean ScrollTo(@NotNull PaintingEntry variant){
		StockbookSlot slot = null;
		int index = -1;

		for (int i=0; i<handler.bookSlots.size(); ++i)
		if  (variant.equals(handler.bookSlots.get(i).GetVariant())) {
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
		this.linesScrolled = Mth.clamp(linesScrolled, line+1-GRID_H, line);
		this.UpdateScrollability();

		this.preview.SetVariant(variant.value);
		return true;
	}


/******************************************************************************/
/* ## Render                                                                  */
/******************************************************************************/

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta){
		if (this.knownSlots != handler.bookSlots.size()){
			this.knownSlots = handler.bookSlots.size();
			this.UpdateSearchResults();
			this.UpdateScrollability();
		}
		if (handler.requestedFocus != null && this.ScrollTo(handler.requestedFocus))
			handler.requestedFocus = null;

		this.renderBackground(context, mouseX, mouseY, delta);
		super.extractRenderState(context, mouseX, mouseY, delta);
		this.extractTooltip(context, mouseX, mouseY);
	}

	public void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta){
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
		this.RenderScrollbar(context);

		for (StockbookSlot slot : searchResults)
		if  (slot.isActive() && slot != highlighted)
			this.DrawSlotBackground(context, slot, delta);

		if (this.highlighted != null)
			this.DrawSlotBackground(context, highlighted, delta);
	}

	// Intentionally skips title draw from super.
	@Override
	protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int moueY){
		int lockId = handler.containerSlot.get();
		if (0 <= lockId && lockId < handler.slots.size()){
			Slot slot = handler.getSlot(lockId);
			context.blitSprite(RenderPipelines.GUI_TEXTURED, STOCK_SLOT, slot.x-2, slot.y-2, 20, 20);
		}
	}

	private void DrawSlotBackground(GuiGraphicsExtractor context, Slot slot, float delta){
		if (!slot.hasItem())
			return;

		int drawX = this.leftPos+slot.x-5;
		int drawY = this.topPos+slot.y-5;
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

		context.blitSprite(RenderPipelines.GUI_TEXTURED, FULL_SLOT, drawX, drawY, drawSize, drawSize);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY){
		var contextpp = IDrawContextDuck.Of(context);

		if (mouseY < (this.topPos + PREVIEW_Y + PREVIEW_SIZE))
			contextpp.invarpaint$SetTooltipPositioner(this);

		super.extractTooltip(context, mouseX, mouseY);
		contextpp.invarpaint$SetTooltipPositioner(DefaultTooltipPositioner.INSTANCE);
	}

	// Tooltip Positioner
	@Override
	public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight){
		Vector2i pos = new Vector2i(
			this.leftPos + TOOLTIP_X_MIN + TOOLTIP_PADDING,
			mouseY + 16
		);
		int overflow;

		// x
		overflow = mouseX - (pos.x + tooltipWidth);
		if (overflow > 0)
			pos.x += overflow;

		overflow = (pos.x + tooltipWidth) - (this.leftPos + TOOLTIP_X_MAX - TOOLTIP_PADDING);
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
	protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
		Holder<PaintingVariant> variantEntry = PaintStackUtil.GetVariantEntry(stack);
		if (variantEntry != null)
			this.preview.SetVariant(variantEntry.value());

		return super.getTooltipFromContainerItem(stack);
	}

	private void RenderScrollbar(GuiGraphicsExtractor context){
		if (linesScrolledMax == 0)
			return;

		context.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLBAR, scrollbar.getX(), scrollbar.getY(), scrollbar.getWidth(), scrollbar.getHeight());
	}


/******************************************************************************/
/* ## Interactions                                                            */
/******************************************************************************/

	@Override
	public boolean charTyped(CharacterEvent charInput){
		boolean r = super.charTyped(charInput);

		if (r && searchBox.isFocused())
			this.UpdateSearchResults();

		return r;
	}

	@Override
	public boolean keyPressed(KeyEvent keyInput){
		int keyCode = keyInput.input();
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
			this.setFocused(searchBox);
		else if (this.searchBox.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE )
				this.setFocused(null);
			else if (searchBox.keyPressed(keyInput))
				this.UpdateSearchResults();
			return true;
		}

		return super.keyPressed(keyInput);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		this.linesScrolled -= (int)verticalAmount;
		this.UpdateScrollbar();
		return true;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled){
		double mouseX = click.x();
		double mouseY = click.y();

		this.setFocused(null);
		if (click.button() == 0
		 && mouseX >= (this.leftPos+RAIL_X)
		 && mouseX <  (this.leftPos+RAIL_X+RAIL_W)
		 && mouseY >= (this.topPos+RAIL_Y)
		 && mouseY <  (this.topPos+RAIL_Y+RAIL_H)
		) {
			this.isScrolling = true;
			this.linesScrolled = (int)Math.round( linesScrolledMax * (mouseY - this.topPos - RAIL_Y) / RAIL_H );
			this.UpdateScrollbar();
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click){
		if (click.button() == 0)
			this.isScrolling = false;

		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY){
		if (!this.isScrolling)
			return super.mouseDragged(click, deltaX, deltaY);

		this.linesScrolled = (int)Math.round( linesScrolledMax * (click.y() - this.topPos - RAIL_Y) / RAIL_H );
		this.UpdateScrollbar();
		return true;
	}

}
