package y3.mobiledev.mywallet.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BinanceP2PResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<AdvData> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AdvData> getData() {
        return data;
    }

    public void setData(List<AdvData> data) {
        this.data = data;
    }

    public static class AdvData {
        @SerializedName("adv")
        private Adv adv;

        @SerializedName("advertiser")
        private Advertiser advertiser;

        public Adv getAdv() {
            return adv;
        }

        public void setAdv(Adv adv) {
            this.adv = adv;
        }

        public Advertiser getAdvertiser() {
            return advertiser;
        }

        public void setAdvertiser(Advertiser advertiser) {
            this.advertiser = advertiser;
        }
    }

    public static class Adv {
        @SerializedName("price")
        private String price;

        @SerializedName("tradeType")
        private String tradeType; // "BUY" or "SELL"

        @SerializedName("asset")
        private String asset; // "USDT"

        @SerializedName("fiatUnit")
        private String fiatUnit; // "MMK"

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }

        public String getFiatUnit() {
            return fiatUnit;
        }

        public void setFiatUnit(String fiatUnit) {
            this.fiatUnit = fiatUnit;
        }
    }

    public static class Advertiser {
        @SerializedName("nickName")
        private String nickName;

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
    }
}

