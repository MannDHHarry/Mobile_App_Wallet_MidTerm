package y3.mobiledev.mywallet.models;

import com.google.gson.annotations.SerializedName;

public class SpotPriceResponse {
    @SerializedName("symbol")
    private String symbol;

    @SerializedName("price")
    private String price;

    // For Binance API
    public SpotPriceResponse() {
    }

    // For Bybit API
    @SerializedName("lastPrice")
    private String lastPrice;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }

    // Helper method to get the actual price (works for both APIs)
    public String getActualPrice() {
        if (price != null && !price.isEmpty()) {
            return price;
        }
        return lastPrice != null ? lastPrice : "0";
    }
}

