package y3.mobiledev.mywallet.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BybitP2PResponse {
    @SerializedName("retCode")
    private int retCode;

    @SerializedName("retMsg")
    private String retMsg;

    @SerializedName("result")
    private Result result;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Result {
        @SerializedName("list")
        private List<Item> list;

        public List<Item> getList() {
            return list;
        }

        public void setList(List<Item> list) {
            this.list = list;
        }
    }

    public static class Item {
        @SerializedName("price")
        private String price;

        @SerializedName("side")
        private String side; // "1" for sell, "0" for buy

        @SerializedName("tokenId")
        private String tokenId; // "USDT"

        @SerializedName("currencyId")
        private String currencyId; // "MMK"

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getCurrencyId() {
            return currencyId;
        }

        public void setCurrencyId(String currencyId) {
            this.currencyId = currencyId;
        }
    }
}

