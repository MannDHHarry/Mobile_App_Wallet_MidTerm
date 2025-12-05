package y3.mobiledev.mywallet.models;

public class ExchangeRate {
    private String exchange;
    private String currencyPair;
    private double spotPrice;
    private double p2pBuyPrice;
    private double p2pSellPrice;
    private long lastUpdated;
    private String source; // "binance" or "bybit"
    private String sourceType; // "P2P", "API", "FALLBACK"

    public ExchangeRate() {
    }

    public ExchangeRate(String exchange, String currencyPair, double spotPrice, 
                        double p2pBuyPrice, double p2pSellPrice, long lastUpdated, String source) {
        this.exchange = exchange;
        this.currencyPair = currencyPair;
        this.spotPrice = spotPrice;
        this.p2pBuyPrice = p2pBuyPrice;
        this.p2pSellPrice = p2pSellPrice;
        this.lastUpdated = lastUpdated;
        this.source = source;
        this.sourceType = "P2P"; // Default to P2P
    }

    public ExchangeRate(String exchange, String currencyPair, double spotPrice, 
                        double p2pBuyPrice, double p2pSellPrice, long lastUpdated, String source, String sourceType) {
        this.exchange = exchange;
        this.currencyPair = currencyPair;
        this.spotPrice = spotPrice;
        this.p2pBuyPrice = p2pBuyPrice;
        this.p2pSellPrice = p2pSellPrice;
        this.lastUpdated = lastUpdated;
        this.source = source;
        this.sourceType = sourceType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public double getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }

    public double getP2pBuyPrice() {
        return p2pBuyPrice;
    }

    public void setP2pBuyPrice(double p2pBuyPrice) {
        this.p2pBuyPrice = p2pBuyPrice;
    }

    public double getP2pSellPrice() {
        return p2pSellPrice;
    }

    public void setP2pSellPrice(double p2pSellPrice) {
        this.p2pSellPrice = p2pSellPrice;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}

