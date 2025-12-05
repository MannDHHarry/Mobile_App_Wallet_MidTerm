package y3.mobiledev.mywallet;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import y3.mobiledev.mywallet.models.ExchangeRate;
import y3.mobiledev.mywallet.repository.ExchangeRateRepository;

public class ExchangeRateViewModel extends AndroidViewModel {
    private ExchangeRateRepository repository;
    private LiveData<List<ExchangeRate>> exchangeRates;
    private LiveData<Boolean> isLoading;
    private LiveData<String> errorMessage;

    public ExchangeRateViewModel(@NonNull Application application) {
        super(application);
        repository = new ExchangeRateRepository(application);
        exchangeRates = repository.getExchangeRates();
        isLoading = repository.getIsLoading();
        errorMessage = repository.getErrorMessage();
    }

    public LiveData<List<ExchangeRate>> getExchangeRates() {
        return exchangeRates;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshExchangeRates() {
        repository.fetchExchangeRates();
    }
}

