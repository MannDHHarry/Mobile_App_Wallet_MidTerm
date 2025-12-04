package y3.mobiledev.mywallet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.Locale;

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;
import y3.mobiledev.mywallet.helpers.CurrencyUtils;
import y3.mobiledev.mywallet.models.SpendingAnalysisResult;

public class SpendingInsightsFragment extends Fragment {

    private TextView tvInsights;
    private TextView tvTotalIncome;
    private TextView tvTotalExpenses;
    private TextView tvNetAmount;
    private TextView tvSavingsRate;
    private TextView tvSpendingSpikes;
    private TextView tvCategoryTrends;
    private Button btnRefresh;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spending_insights, container, false);

        initViews(view);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupListeners();
        observeData();

        // Trigger initial analysis
        viewModel.analyzeSpendingPatterns();

        return view;
    }

    private void initViews(View view) {
        tvInsights = view.findViewById(R.id.tvInsights);
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvNetAmount = view.findViewById(R.id.tvNetAmount);
        tvSavingsRate = view.findViewById(R.id.tvSavingsRate);
        tvSpendingSpikes = view.findViewById(R.id.tvSpendingSpikes);
        tvCategoryTrends = view.findViewById(R.id.tvCategoryTrends);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> {
            viewModel.analyzeSpendingPatterns();
        });
    }

    private void observeData() {
        viewModel.getSpendingInsights().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                updateUI(result);
            } else {
                showEmptyState();
            }
        });
    }

    private void updateUI(SpendingAnalysisResult result) {
        // Update insights
        List<String> insights = result.getInsights();
        if (insights != null && !insights.isEmpty()) {
            StringBuilder insightsText = new StringBuilder();
            for (String insight : insights) {
                insightsText.append(insight).append("\n\n");
            }
            tvInsights.setText(insightsText.toString().trim());
        } else {
            tvInsights.setText("No insights available yet.");
        }

        // Update financial summary (VND)
        tvTotalIncome.setText("Total Income: " + CurrencyUtils.formatPlainAmount(result.getTotalIncome()));
        tvTotalExpenses.setText("Total Expenses: " + CurrencyUtils.formatPlainAmount(result.getTotalExpenses()));
        
        double netAmount = result.getNetAmount();
        String netText = "Net Amount: " + CurrencyUtils.formatPlainAmount(netAmount);
        if (netAmount < 0) {
            netText += " (Overspent)";
        } else {
            netText += " (Saved)";
        }
        tvNetAmount.setText(netText);
        
        tvSavingsRate.setText(String.format(Locale.US, "Savings Rate: %.1f%%", result.getSavingsRate()));

        // Update spending spikes
        List<SpendingAnalysisResult.SpendingSpike> spikes = result.getSpendingSpikes();
        if (spikes != null && !spikes.isEmpty()) {
            StringBuilder spikesText = new StringBuilder();
            for (SpendingAnalysisResult.SpendingSpike spike : spikes) {
                spikesText.append(String.format(Locale.US,
                        "• %s: %s (%.1fx above average)\n",
                        spike.getCategoryName(),
                        CurrencyUtils.formatPlainAmount(spike.getTransaction().getAmount()),
                        spike.getSpikeMultiplier()));
            }
            tvSpendingSpikes.setText(spikesText.toString().trim());
        } else {
            tvSpendingSpikes.setText("No unusual spending spikes detected. Great job!");
        }

        // Update category trends
        List<SpendingAnalysisResult.CategoryAnalysis> categoryAnalyses = result.getCategoryAnalyses();
        if (categoryAnalyses != null && !categoryAnalyses.isEmpty()) {
            StringBuilder trendsText = new StringBuilder();
            for (SpendingAnalysisResult.CategoryAnalysis analysis : categoryAnalyses) {
                if (Math.abs(analysis.getChangePercentage()) > 10) {
                    String trend = analysis.isIncreasing() ? "↑" : "↓";
                    trendsText.append(String.format(Locale.US,
                            "• %s %s: %s (%.1f%% change)\n",
                            trend,
                            analysis.getCategoryName(),
                            CurrencyUtils.formatPlainAmount(analysis.getCurrentAmount()),
                            Math.abs(analysis.getChangePercentage())));
                }
            }
            if (trendsText.length() > 0) {
                tvCategoryTrends.setText(trendsText.toString().trim());
            } else {
                tvCategoryTrends.setText("No significant category trends detected.");
            }
        } else {
            tvCategoryTrends.setText("No category trends available.");
        }
    }

    private void showEmptyState() {
        tvInsights.setText("No data available. Add some transactions to get insights!");
        tvTotalIncome.setText("Total Income: 0 ₫");
        tvTotalExpenses.setText("Total Expenses: 0 ₫");
        tvNetAmount.setText("Net Amount: 0 ₫");
        tvSavingsRate.setText("Savings Rate: 0%");
        tvSpendingSpikes.setText("No data available.");
        tvCategoryTrends.setText("No data available.");
    }
}

