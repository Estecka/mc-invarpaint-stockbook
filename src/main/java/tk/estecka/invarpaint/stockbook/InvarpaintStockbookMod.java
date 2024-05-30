package tk.estecka.invarpaint.stockbook;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvarpaintStockbookMod
implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("invarpaint-stockbook");

	@Override
	public void onInitialize() {
		VariantCollectionComponent.Register();
		StockbookItem.Register();
		AStockbookHandler.Register();
	}
}
