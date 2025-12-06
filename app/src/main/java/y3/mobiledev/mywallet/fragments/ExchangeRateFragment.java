package y3.mobiledev.mywallet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import y3.mobiledev.mywallet.ExchangeRateViewModel;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.models.ExchangeRate;

public class ExchangeRateFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ratesContainer;
    private LinearLayout loadingLayout;
    private LinearLayout emptyStateLayout;
    private CardView errorCard;
    private TextView tvErrorMessage;
    private ExchangeRateViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange_rate, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ExchangeRateViewModel.class);
        initViews(view);
        setupListeners();
        observeData();
        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        ratesContainer = view.findViewById(R.id.ratesContainer);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        errorCard = view.findViewById(R.id.errorCard);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshExchangeRates();
        });
    }

    private void observeData() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
            loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                errorCard.setVisibility(View.VISIBLE);
                tvErrorMessage.setText(errorMessage);
            } else {
                errorCard.setVisibility(View.GONE);
            }
        });

        viewModel.getExchangeRates().observe(getViewLifecycleOwner(), rates -> {
            if (rates != null && !rates.isEmpty()) {
                displayExchangeRates(rates);
                ratesContainer.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
            } else {
                ratesContainer.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
            }
        });

        // Initial load
        if (viewModel.getExchangeRates().getValue() == null || 
            viewModel.getExchangeRates().getValue().isEmpty()) {
            viewModel.refreshExchangeRates();
        }
    }

    private void displayExchangeRates(List<ExchangeRate> rates) {
        ratesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (ExchangeRate rate : rates) {
            View itemView = inflater.inflate(R.layout.item_exchange_rate, ratesContainer, false);
            bindExchangeRateItem(itemView, rate);
            ratesContainer.addView(itemView);
        }
    }

    private void bindExchangeRateItem(View itemView, ExchangeRate rate) {
        TextView tvExchangeName = itemView.findViewById(R.id.tvExchangeName);
        TextView tvSpotPrice = itemView.findViewById(R.id.tvSpotPrice);
        TextView tvBuyPrice = itemView.findViewById(R.id.tvBuyPrice);
        TextView tvSellPrice = itemView.findViewById(R.id.tvSellPrice);
        TextView tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);

        String currencyPair = rate.getCurrencyPair();
        String sourceType = rate.getSourceType();
        String sourceLabel = "";
        
        // Create source label based on source type
        if ("P2P".equals(sourceType)) {
            sourceLabel = " (P2P)";
        } else if ("API".equals(sourceType)) {
            sourceLabel = " (Currency API)";
        } else if ("FALLBACK".equals(sourceType)) {
            sourceLabel = " (Estimated)";
        } else if ("CALCULATED".equals(sourceType)) {
            sourceLabel = " (Calculated)";
        } else if ("SPOT".equals(sourceType)) {
            sourceLabel = " (Spot)";
        }
        
        tvExchangeName.setText(rate.getExchange() + " - " + currencyPair + sourceLabel);

        // Format spot price based on currency pair
        if (currencyPair.startsWith("VND/")) {
            // Extract the target currency (e.g., "USD" from "VND/USD")
            String targetCurrency = currencyPair.substring(4);
            
            if (rate.getSpotPrice() > 0) {
                // For VND rates, show as "1 [currency] = X VND"
                tvSpotPrice.setText(String.format(Locale.US, "1 %s = %.0f VND", targetCurrency, rate.getSpotPrice()));
            } else if (rate.getP2pBuyPrice() > 0 || rate.getP2pSellPrice() > 0) {
                double avgPrice = 0;
                int count = 0;
                if (rate.getP2pBuyPrice() > 0) {
                    avgPrice += rate.getP2pBuyPrice();
                    count++;
                }
                if (rate.getP2pSellPrice() > 0) {
                    avgPrice += rate.getP2pSellPrice();
                    count++;
                }
                if (count > 0) {
                    avgPrice = avgPrice / count;
                    tvSpotPrice.setText(String.format(Locale.US, "1 %s = %.0f VND", targetCurrency, avgPrice));
                } else {
                    tvSpotPrice.setText("Rate unavailable");
                }
            } else {
                tvSpotPrice.setText("Rate unavailable");
            }
        } else {
            tvSpotPrice.setText("Rate unavailable");
        }

        // Format P2P buy price based on currency pair
        if (rate.getP2pBuyPrice() > 0) {
            if (currencyPair.startsWith("VND/")) {
                // For VND rates, show as VND amount
                tvBuyPrice.setText(String.format(Locale.US, "%.0f VND", rate.getP2pBuyPrice()));
            } else {
                tvBuyPrice.setText(String.format(Locale.US, "%.2f", rate.getP2pBuyPrice()));
            }
        } else {
            tvBuyPrice.setText("N/A");
        }

        // Format P2P sell price based on currency pair
        if (rate.getP2pSellPrice() > 0) {
            if (currencyPair.startsWith("VND/")) {
                // For VND rates, show as VND amount
                tvSellPrice.setText(String.format(Locale.US, "%.0f VND", rate.getP2pSellPrice()));
            } else {
                tvSellPrice.setText(String.format(Locale.US, "%.2f", rate.getP2pSellPrice()));
            }
        } else {
            tvSellPrice.setText("N/A");
        }

        // Format last updated
        if (rate.getLastUpdated() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
            String formattedDate = sdf.format(new Date(rate.getLastUpdated()));
            tvLastUpdated.setText(getString(R.string.last_updated, formattedDate));
        } else {
            tvLastUpdated.setText("");
        }
    }
}

