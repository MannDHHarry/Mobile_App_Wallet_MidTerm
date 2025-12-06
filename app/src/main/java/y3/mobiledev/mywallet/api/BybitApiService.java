package y3.mobiledev.mywallet.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import y3.mobiledev.mywallet.models.BybitP2PResponse;
import y3.mobiledev.mywallet.models.BybitSpotResponse;

public interface BybitApiService {
    // Get spot ticker for a symbol
    @GET("v5/market/tickers")
    Call<BybitSpotResponse> getSpotTicker(@Query("category") String category, 
                                             @Query("symbol") String symbol);

    // Get P2P orders
    @GET("v1/private/otc/order/list")
    Call<BybitP2PResponse> getP2POrders(@Query("coin") String coin,
                                        @Query("currencyId") String currencyId,
                                        @Query("side") String side);
}

