package y3.mobiledev.mywallet.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BINANCE_BASE_URL = "https://api.binance.com/";
    private static final String BINANCE_P2P_BASE_URL = "https://p2p.binance.com/";
    private static final String BYBIT_BASE_URL = "https://api.bybit.com/";
    private static final String CURRENCY_API_BASE_URL = "https://api.exchangerate.host/";
    private static final String CURRENCY_API_ALT_BASE_URL = "https://v6.exchangerate-api.com/";

    private static Retrofit binanceRetrofit;
    private static Retrofit binanceP2PRetrofit;
    private static Retrofit bybitRetrofit;
    private static Retrofit currencyApiRetrofit;

    private static OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static Retrofit getBinanceRetrofit() {
        if (binanceRetrofit == null) {
            binanceRetrofit = new Retrofit.Builder()
                    .baseUrl(BINANCE_BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return binanceRetrofit;
    }

    public static Retrofit getBinanceP2PRetrofit() {
        if (binanceP2PRetrofit == null) {
            binanceP2PRetrofit = new Retrofit.Builder()
                    .baseUrl(BINANCE_P2P_BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return binanceP2PRetrofit;
    }

    public static Retrofit getBybitRetrofit() {
        if (bybitRetrofit == null) {
            bybitRetrofit = new Retrofit.Builder()
                    .baseUrl(BYBIT_BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return bybitRetrofit;
    }

    public static Retrofit getCurrencyApiRetrofit() {
        if (currencyApiRetrofit == null) {
            currencyApiRetrofit = new Retrofit.Builder()
                    .baseUrl(CURRENCY_API_BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return currencyApiRetrofit;
    }
}

