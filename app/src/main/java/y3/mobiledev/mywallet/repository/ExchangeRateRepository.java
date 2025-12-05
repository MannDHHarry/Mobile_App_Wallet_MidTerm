package y3.mobiledev.mywallet.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;
import y3.mobiledev.mywallet.api.ApiClient;
import y3.mobiledev.mywallet.api.BinanceApiService;
import y3.mobiledev.mywallet.api.BinanceP2PRequest;
import y3.mobiledev.mywallet.api.CurrencyApiService;
import y3.mobiledev.mywallet.models.BinanceP2PResponse;
import y3.mobiledev.mywallet.models.CurrencyApiResponse;
import y3.mobiledev.mywallet.models.ExchangeRate;

public class ExchangeRateRepository {
    private static final String TAG = "ExchangeRateRepository";
    private static final String PREFS_NAME = "exchange_rate_cache";
    private static final String KEY_CACHED_RATES = "cached_rates";
    private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp";
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    private final BinanceApiService binanceP2PApiService;
    private final CurrencyApiService currencyApiService;
    private final ExecutorService executorService;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    // Famous currencies to show VND rates for
    private static final String[] FAMOUS_CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CNY", "SGD", "THB", "KRW", "AUD", "CAD"};

    private final MutableLiveData<List<ExchangeRate>> exchangeRates = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ExchangeRateRepository(Context context) {
        binanceP2PApiService = ApiClient.getBinanceP2PRetrofit().create(BinanceApiService.class);
        currencyApiService = ApiClient.getCurrencyApiRetrofit().create(CurrencyApiService.class);
        executorService = Executors.newFixedThreadPool(4);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        // Load cached data initially
        loadCachedData();
    }

    public MutableLiveData<List<ExchangeRate>> getExchangeRates() {
        return exchangeRates;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchExchangeRates() {
        isLoading.postValue(true);
        errorMessage.postValue(null);

        executorService.execute(() -> {
            List<ExchangeRate> rates = new ArrayList<>();
            long currentTime = System.currentTimeMillis();

            // Fetch VND rates for famous currencies
            // Try P2P first for USD, then use currency API for all currencies
            ExchangeRate vndUsdP2P = fetchVndUsdData();
            if (vndUsdP2P != null) {
                rates.add(vndUsdP2P);
            }

            // Fetch VND rates for all famous currencies from currency API
            List<ExchangeRate> vndRates = fetchVndRatesForCurrencies();
            if (vndRates != null && !vndRates.isEmpty()) {
                rates.addAll(vndRates);
            }

            if (rates.isEmpty()) {
                errorMessage.postValue("Failed to fetch exchange rates. Please try again.");
                // Try to load cached data as fallback
                loadCachedData();
            } else {
                exchangeRates.postValue(rates);
                saveCachedData(rates);
            }

            isLoading.postValue(false);
        });
    }

    private List<ExchangeRate> fetchVndRatesForCurrencies() {
        List<ExchangeRate> rates = new ArrayList<>();
        try {
            // Fetch all rates from currency API (base USD)
            Call<CurrencyApiResponse> apiCall = currencyApiService.getLatestRates();
            Response<CurrencyApiResponse> response = apiCall.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                CurrencyApiResponse apiResponse = response.body();
                Map<String, Double> allRates = apiResponse.getRates();
                if (allRates == null) {
                    allRates = apiResponse.getConversionRates();
                }
                
                if (allRates != null && !allRates.isEmpty()) {
                    Log.d(TAG, "Currency API returned " + allRates.size() + " rates");
                    // Get VND rate (1 USD = X VND)
                    Double vndRate = allRates.get("VND");
                    Log.d(TAG, "VND rate from API: " + vndRate);
                    
                    if (vndRate != null && vndRate > 0) {
                        // For each famous currency (excluding USD since we get it from P2P), calculate VND rate
                        for (String currency : FAMOUS_CURRENCIES) {
                            // Skip USD as we already have it from P2P
                            if ("USD".equals(currency)) {
                                continue;
                            }
                            
                            Double currencyRate = allRates.get(currency);
                            if (currencyRate != null && currencyRate > 0) {
                                // Calculate: 1 [currency] = ? VND
                                // If 1 USD = X VND and 1 USD = Y [currency], then 1 [currency] = X/Y VND
                                double vndPerCurrency = vndRate / currencyRate;
                                
                                ExchangeRate rate = new ExchangeRate("Currency API", "VND/" + currency, 
                                        vndPerCurrency, vndPerCurrency, vndPerCurrency, 
                                        System.currentTimeMillis(), "currency_api", "API");
                                rates.add(rate);
                                Log.d(TAG, "Added VND/" + currency + " rate: " + vndPerCurrency);
                            } else {
                                Log.w(TAG, "Currency rate not found for: " + currency);
                            }
                        }
                    } else {
                        Log.w(TAG, "VND rate is null or zero");
                    }
                } else {
                    Log.w(TAG, "Currency API rates map is null or empty");
                    if (apiResponse.getResult() != null) {
                        Log.w(TAG, "API result: " + apiResponse.getResult());
                    }
                }
            } else {
                Log.w(TAG, "Currency API response not successful. Code: " + 
                        (response != null ? response.code() : "null") + 
                        ", Message: " + (response != null && response.errorBody() != null ? 
                        response.errorBody().toString() : "null"));
                // Try alternative endpoint
                try {
                    Call<CurrencyApiResponse> altCall = currencyApiService.getLatestRatesAlt();
                    Response<CurrencyApiResponse> altResponse = altCall.execute();
                    if (altResponse.isSuccessful() && altResponse.body() != null) {
                        Log.d(TAG, "Alternative API endpoint succeeded");
                        CurrencyApiResponse altApiResponse = altResponse.body();
                        Map<String, Double> altRates = altApiResponse.getRates();
                        if (altRates == null) {
                            altRates = altApiResponse.getConversionRates();
                        }
                        
                        if (altRates != null && !altRates.isEmpty()) {
                            Double vndRate = altRates.get("VND");
                            if (vndRate != null && vndRate > 0) {
                                for (String currency : FAMOUS_CURRENCIES) {
                                    if ("USD".equals(currency)) {
                                        continue;
                                    }
                                    Double currencyRate = altRates.get(currency);
                                    if (currencyRate != null && currencyRate > 0) {
                                        double vndPerCurrency = vndRate / currencyRate;
                                        ExchangeRate rate = new ExchangeRate("Currency API", "VND/" + currency, 
                                                vndPerCurrency, vndPerCurrency, vndPerCurrency, 
                                                System.currentTimeMillis(), "currency_api", "API");
                                        rates.add(rate);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception altE) {
                    Log.e(TAG, "Alternative API endpoint also failed", altE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching VND rates for currencies", e);
            e.printStackTrace();
        }
        
        Log.d(TAG, "Returning " + rates.size() + " VND currency rates");
        return rates;
    }

    private void saveCachedData(List<ExchangeRate> rates) {
        try {
            String json = gson.toJson(rates);
            sharedPreferences.edit()
                    .putString(KEY_CACHED_RATES, json)
                    .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving cached data", e);
        }
    }

    private ExchangeRate fetchVndUsdData() {
        try {
            double p2pBuyPrice = 0;
            double p2pSellPrice = 0;

            // Fetch VND/USD P2P buy price from Binance
            try {
                BinanceP2PRequest buyRequest = new BinanceP2PRequest("BUY");
                buyRequest.setFiat("VND");
                Call<BinanceP2PResponse> buyCall = binanceP2PApiService.searchP2P(buyRequest);
                Response<BinanceP2PResponse> buyResponse = buyCall.execute();
                if (buyResponse.isSuccessful() && buyResponse.body() != null) {
                    BinanceP2PResponse response = buyResponse.body();
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        String priceStr = response.getData().get(0).getAdv().getPrice();
                        p2pBuyPrice = Double.parseDouble(priceStr);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching Binance VND/USD P2P buy price", e);
            }

            // Fetch VND/USD P2P sell price from Binance
            try {
                BinanceP2PRequest sellRequest = new BinanceP2PRequest("SELL");
                sellRequest.setFiat("VND");
                Call<BinanceP2PResponse> sellCall = binanceP2PApiService.searchP2P(sellRequest);
                Response<BinanceP2PResponse> sellResponse = sellCall.execute();
                if (sellResponse.isSuccessful() && sellResponse.body() != null) {
                    BinanceP2PResponse response = sellResponse.body();
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        String priceStr = response.getData().get(0).getAdv().getPrice();
                        p2pSellPrice = Double.parseDouble(priceStr);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching Binance VND/USD P2P sell price", e);
            }

            if (p2pBuyPrice > 0 || p2pSellPrice > 0) {
                return new ExchangeRate("Binance", "VND/USD", 0, 
                        p2pBuyPrice, p2pSellPrice, System.currentTimeMillis(), "binance", "P2P");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching VND/USD data", e);
        }
        return null;
    }


    private void loadCachedData() {
        try {
            long cacheTime = sharedPreferences.getLong(KEY_CACHE_TIMESTAMP, 0);
            long currentTime = System.currentTimeMillis();

            if (currentTime - cacheTime < CACHE_DURATION) {
                String json = sharedPreferences.getString(KEY_CACHED_RATES, null);
                if (json != null) {
                    Type type = new TypeToken<List<ExchangeRate>>(){}.getType();
                    List<ExchangeRate> cachedRates = gson.fromJson(json, type);
                    if (cachedRates != null && !cachedRates.isEmpty()) {
                        exchangeRates.postValue(cachedRates);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cached data", e);
        }
    }
}

