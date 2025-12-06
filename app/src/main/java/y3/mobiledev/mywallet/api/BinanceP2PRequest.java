package y3.mobiledev.mywallet.api;

import com.google.gson.annotations.SerializedName;

public class BinanceP2PRequest {
    @SerializedName("asset")
    private String asset = "USDT";

    @SerializedName("fiat")
    private String fiat = "MMK";

    @SerializedName("page")
    private int page = 1;

    @SerializedName("rows")
    private int rows = 20;

    @SerializedName("tradeType")
    private String tradeType; // "BUY" or "SELL"

    @SerializedName("payTypes")
    private String[] payTypes = new String[0];

    public BinanceP2PRequest() {
    }

    public BinanceP2PRequest(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getFiat() {
        return fiat;
    }

    public void setFiat(String fiat) {
        this.fiat = fiat;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String[] getPayTypes() {
        return payTypes;
    }

    public void setPayTypes(String[] payTypes) {
        this.payTypes = payTypes;
    }
}

