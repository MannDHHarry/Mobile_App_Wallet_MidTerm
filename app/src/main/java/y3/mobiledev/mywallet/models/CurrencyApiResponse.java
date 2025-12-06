package y3.mobiledev.mywallet.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class CurrencyApiResponse {
    @SerializedName("result")
    private String result;

    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("conversion_rates")
    private Map<String, Double> conversionRates;

    // Alternative structure for exchangerate-api.com
    @SerializedName("rates")
    private Map<String, Double> rates;

    @SerializedName("base")
    private String base;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }

    public void setConversionRates(Map<String, Double> conversionRates) {
        this.conversionRates = conversionRates;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    // Helper method to get MMK rate
    public Double getMmkRate() {
        if (conversionRates != null && conversionRates.containsKey("MMK")) {
            return conversionRates.get("MMK");
        }
        if (rates != null && rates.containsKey("MMK")) {
            return rates.get("MMK");
        }
        return null;
    }
}

