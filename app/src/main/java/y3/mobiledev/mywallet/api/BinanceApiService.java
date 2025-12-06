package y3.mobiledev.mywallet.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Query;
import y3.mobiledev.mywallet.models.BinanceP2PResponse;
import y3.mobiledev.mywallet.models.SpotPriceResponse;

public interface BinanceApiService {
    // Get spot price for a symbol (e.g., USDTUSD)
    @GET("api/v3/ticker/price")
    Call<SpotPriceResponse> getSpotPrice(@Query("symbol") String symbol);

    // P2P search endpoint - requires POST with body
    @POST("bapi/c2c/v2/friendly/c2c/adv/search")
    Call<BinanceP2PResponse> searchP2P(@Body BinanceP2PRequest request);
}

