package y3.mobiledev.mywallet.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;

import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.TransactionGroup;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;

public class StatisticsFragment extends Fragment {

    private PieChart pieChart;
    private RadioGroup rgTransactionType;
    private RadioButton rbExpense, rbIncome;
    private Button btnPreviousMonth, btnNextMonth;
    private TextView tvMonthYear, tvSummaryTitle, tvTotalAmount, tvCategoryBreakdown;
    private TransactionViewModel viewModel;

    private boolean isExpense = true;
    private Calendar startDate, endDate;
    private Calendar currentMonthCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupChart();
        setupRadioButtons();
        setupDatePickers();
        observeData();

        return view;
    }

    private void initViews(View view) {
        pieChart = view.findViewById(R.id.pieChart);
        rgTransactionType = view.findViewById(R.id.rgTransactionType);
        rbExpense = view.findViewById(R.id.rbExpense);
        rbIncome = view.findViewById(R.id.rbIncome);

        // Month navigation only
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);

        tvSummaryTitle = view.findViewById(R.id.tvSummaryTitle);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvCategoryBreakdown = view.findViewById(R.id.tvCategoryBreakdown);
    }

    private void setupChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
    }

    private void setupRadioButtons() {
        rbExpense.setChecked(true);
        isExpense = true;

        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            isExpense = (checkedId == R.id.rbExpense);
            observeData();
        });
    }

    private void setupDatePickers() {
        // Initialize current month
        currentMonthCalendar = Calendar.getInstance();
        setCurrentMonthDates();
        updateMonthDisplay();

        // Previous month button
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, -1);
            setCurrentMonthDates();
            updateMonthDisplay();
            observeData();
        });

        // Next month button
        btnNextMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, 1);
            setCurrentMonthDates();
            updateMonthDisplay();
            observeData();
        });
    }

    private void setCurrentMonthDates() {
        startDate = (Calendar) currentMonthCalendar.clone();
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);

        endDate = (Calendar) currentMonthCalendar.clone();
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
    }

    private void updateMonthDisplay() {
        SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.US);
        tvMonthYear.setText(format.format(currentMonthCalendar.getTime()));
    }

    private void observeData() {
        viewModel.getTransactionGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                updateChart(groups);
                updateSummaryReport(groups);
            }
        });
    }

    private void updateChart(List<TransactionGroup> groups) {
        Map<String, Float> categoryAmounts = calculateCategoryAmounts(groups);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryAmounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, isExpense ? "Expenses" : "Income");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(getColors());

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.animateY(1000);
    }

    private void updateSummaryReport(List<TransactionGroup> groups) {
        Map<String, Float> categoryAmounts = calculateCategoryAmounts(groups);
        float totalAmount = 0;

        for (Float amount : categoryAmounts.values()) {
            totalAmount += amount;
        }

        // Update summary title with current month
        SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.US);
        String dateRange = "(" + format.format(currentMonthCalendar.getTime()) + ")";

        tvSummaryTitle.setText((isExpense ? "Expense" : "Income") + " Summary " + dateRange);
        tvTotalAmount.setText(String.format(Locale.US, "Total: Rs. %.2f", totalAmount));

        // Build category breakdown
        if (totalAmount == 0) {
            tvCategoryBreakdown.setText("No transactions in this period");
            return;
        }

        StringBuilder breakdown = new StringBuilder("Breakdown:\n");
        for (Map.Entry<String, Float> entry : categoryAmounts.entrySet()) {
            float percentage = (entry.getValue() / totalAmount) * 100;
            breakdown.append(String.format(Locale.US, "• %s: Rs. %.2f (%.1f%%)\n",
                    entry.getKey(), entry.getValue(), percentage));
        }

        tvCategoryBreakdown.setText(breakdown.toString());
    }

    private Map<String, Float> calculateCategoryAmounts(List<TransactionGroup> groups) {
        Map<String, Float> categoryAmounts = new HashMap<>();
        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        for (TransactionGroup group : groups) {
            for (Transaction transaction : group.getTransactions()) {
                long transactionTime = transaction.getDate().getTime();

                // Filter by expense type and date range
                if (transaction.isExpense() == isExpense &&
                        transactionTime >= startTime &&
                        transactionTime <= endTime) {

                    String category = transaction.getCategory();
                    float amount = (float) transaction.getAmount();
                    categoryAmounts.put(category,
                            categoryAmounts.getOrDefault(category, 0f) + amount);
                }
            }
        }
        return categoryAmounts;
    }

    private ArrayList<Integer> getColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF6B6B"));
        colors.add(Color.parseColor("#4ECDC4"));
        colors.add(Color.parseColor("#45B7D1"));
        colors.add(Color.parseColor("#FFA07A"));
        colors.add(Color.parseColor("#98D8C8"));
        colors.add(Color.parseColor("#F7DC6F"));
        colors.add(Color.parseColor("#BB8FCE"));
        return colors;
    }
}