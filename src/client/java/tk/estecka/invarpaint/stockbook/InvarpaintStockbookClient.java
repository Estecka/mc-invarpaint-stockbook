package tk.estecka.invarpaint.stockbook;

import net.fabricmc.api.ClientModInitializer;

public class InvarpaintStockbookClient
implements ClientModInitializer
{
	@Override
	public void onInitializeClient() {
		StockbookScreen.Register();
	}
}
