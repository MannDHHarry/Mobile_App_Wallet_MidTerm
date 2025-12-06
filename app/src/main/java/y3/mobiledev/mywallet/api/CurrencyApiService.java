package y3.mobiledev.mywallet.api;

import retrofit2.Call;
import retrofit2.http.GET;
import y3.mobiledev.mywallet.models.CurrencyApiResponse;

public interface CurrencyApiService {
    // exchangerate.host free endpoint (no API key required)
    @GET("latest?base=USD")
    Call<CurrencyApiResponse> getLatestRates();

    // Alternative: exchangerate-api.com endpoint (may require API key)
    @GET("v6/latest/USD")
    Call<CurrencyApiResponse> getLatestRatesAlt();
}

