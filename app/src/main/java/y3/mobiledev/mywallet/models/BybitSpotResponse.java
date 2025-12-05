package y3.mobiledev.mywallet.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BybitSpotResponse {
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
        private List<Ticker> list;

        public List<Ticker> getList() {
            return list;
        }

        public void setList(List<Ticker> list) {
            this.list = list;
        }
    }

    public static class Ticker {
        @SerializedName("symbol")
        private String symbol;

        @SerializedName("lastPrice")
        private String lastPrice;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getLastPrice() {
            return lastPrice;
        }

        public void setLastPrice(String lastPrice) {
            this.lastPrice = lastPrice;
        }
    }
}

