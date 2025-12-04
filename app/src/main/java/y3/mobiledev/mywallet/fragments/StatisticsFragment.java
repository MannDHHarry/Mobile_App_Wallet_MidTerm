package y3.mobiledev.mywallet.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;

import y3.mobiledev.mywallet.Converters;
import y3.mobiledev.mywallet.models.SpendingAnalysisResult;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.helpers.CurrencyUtils;
import y3.mobiledev.mywallet.TransactionViewModel;

public class StatisticsFragment extends Fragment {

    private LineChart lineChart;
    private PieChart pieChart;
    private RadioGroup rgTransactionType;
    private RadioButton rbExpense, rbIncome;
    private Button btnPreviousMonth, btnNextMonth;
    private TextView tvMonthYear, tvSummaryTitle, tvTotalAmount, tvCategoryBreakdown, tvAIInsights;
    private TextView tvSummaryIncome, tvSummaryExpenses, tvSummaryNet;
    private View emptyStateView;
    private View statsScrollView;
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

        setupLineChart();
        setupPieChart();
        setupRadioButtons();
        setupDatePickers();
        observeData();
        observeStatisticsAlerts();

        return view;
    }

    private void initViews(View view) {
        lineChart = view.findViewById(R.id.lineChart);
        pieChart = view.findViewById(R.id.pieChart);
        rgTransactionType = view.findViewById(R.id.rgTransactionType);
        rbExpense = view.findViewById(R.id.rbExpense);
        rbIncome = view.findViewById(R.id.rbIncome);

        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);

        tvSummaryTitle = view.findViewById(R.id.tvSummaryTitle);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvCategoryBreakdown = view.findViewById(R.id.tvCategoryBreakdown);
        tvAIInsights = view.findViewById(R.id.tvAIInsights);

        tvSummaryIncome = view.findViewById(R.id.tvSummaryIncome);
        tvSummaryExpenses = view.findViewById(R.id.tvSummaryExpenses);
        tvSummaryNet = view.findViewById(R.id.tvSummaryNet);
        emptyStateView = view.findViewById(R.id.emptyStateStatistics);
        statsScrollView = view.findViewById(R.id.statsScrollView);
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("No data for this period");
        lineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        lineChart.getAxisLeft().setDrawGridLines(true);
    }

    private void setupPieChart() {
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
        currentMonthCalendar = Calendar.getInstance();
        setCurrentMonthDates();
        updateMonthDisplay();

        btnPreviousMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, -1);
            setCurrentMonthDates();
            updateMonthDisplay();
            observeData();
        });

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

    // Observe TransactionGroups
    private void observeData() {
        viewModel.getTransactionGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                updateSummaryReport(groups);
                updateLineChart(groups);
                updatePieChart(groups);
                toggleEmptyState(hasTransactionsInRange(groups));
            } else {
                toggleEmptyState(false);
            }
        });
    }

    private void updateLineChart(List<TransactionGroup> groups) {
        if (lineChart == null) return;

        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        // Aggregate amounts by day within the current month and type (expense/income)
        Map<String, Float> dailyAmounts = new HashMap<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);

        for (TransactionGroup group : groups) {
            for (Object transactionObj : group.getTransactions()) {
                long transactionTime;
                double amount;
                boolean isTransactionExpense;

                if (transactionObj instanceof TransactionWithCategory) {
                    TransactionWithCategory twc = (TransactionWithCategory) transactionObj;
                    transactionTime = twc.getDate();
                    amount = twc.getAmount();
                    isTransactionExpense = twc.isExpense();
                } else if (transactionObj instanceof Transaction) {
                    Transaction transaction = (Transaction) transactionObj;
                    transactionTime = transaction.getDate();
                    amount = transaction.getAmount();
                    isTransactionExpense = transaction.isExpense();
                } else {
                    continue;
                }

                if (transactionTime >= startTime && transactionTime <= endTime &&
                        isTransactionExpense == isExpense) {
                    Date date = new Date(transactionTime);
                    String dayKey = dayFormat.format(date);
                    float current = dailyAmounts.getOrDefault(dayKey, 0f);
                    dailyAmounts.put(dayKey, current + (float) amount);
                }
            }
        }

        if (dailyAmounts.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        // Sort by day number
        List<String> days = new ArrayList<>(dailyAmounts.keySet());
        java.util.Collections.sort(days, (d1, d2) -> {
            try {
                int i1 = Integer.parseInt(d1);
                int i2 = Integer.parseInt(d2);
                return Integer.compare(i1, i2);
            } catch (NumberFormatException e) {
                return d1.compareTo(d2);
            }
        });

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            float value = dailyAmounts.get(day);
            entries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, isExpense ? "Daily Expenses" : "Daily Income");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        int color = getResources().getColor(isExpense ? R.color.expense_red : R.color.income_green);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);

        LineData lineData = new LineData(dataSet);

        // Format X axis with day numbers
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < days.size()) {
                    return days.get(index);
                }
                return "";
            }
        });

        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void updatePieChart(List<TransactionGroup> groups) {
        if (pieChart == null) return;

        Map<String, Float> categoryAmounts = calculateCategoryAmounts(groups);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryAmounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
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
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void updateSummaryReport(List<TransactionGroup> groups) {
        Map<String, Float> categoryAmounts = calculateCategoryAmounts(groups);
        float totalAmount = 0;

        for (Float amount : categoryAmounts.values()) {
            totalAmount += amount;
        }

        // Update top summary cards
        float totalExpenses = 0f;
        float totalIncome = 0f;

        // Recalculate income/expense totals from original groups within date range
        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        for (TransactionGroup group : groups) {
            for (Object transactionObj : group.getTransactions()) {
                long transactionTime;
                double amount;
                boolean isTransactionExpense;

                if (transactionObj instanceof TransactionWithCategory) {
                    TransactionWithCategory twc = (TransactionWithCategory) transactionObj;
                    transactionTime = twc.getDate();
                    amount = twc.getAmount();
                    isTransactionExpense = twc.isExpense();
                } else if (transactionObj instanceof Transaction) {
                    Transaction transaction = (Transaction) transactionObj;
                    transactionTime = transaction.getDate();
                    amount = transaction.getAmount();
                    isTransactionExpense = transaction.isExpense();
                } else {
                    continue;
                }

                if (transactionTime >= startTime && transactionTime <= endTime) {
                    if (isTransactionExpense) {
                        totalExpenses += amount;
                    } else {
                        totalIncome += amount;
                    }
                }
            }
        }

        float net = totalIncome - totalExpenses;

        tvSummaryIncome.setText(CurrencyUtils.formatPlainAmount(totalIncome));
        tvSummaryExpenses.setText(CurrencyUtils.formatPlainAmount(totalExpenses));
        tvSummaryNet.setText(CurrencyUtils.formatPlainAmount(net));

        SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.US);
        String dateRange = "(" + format.format(currentMonthCalendar.getTime()) + ")";

        tvSummaryTitle.setText((isExpense ? "Expense" : "Income") + " Summary " + dateRange);
        tvTotalAmount.setText("Total: " + CurrencyUtils.formatPlainAmount(totalAmount));

        if (totalAmount == 0) {
            tvCategoryBreakdown.setText("No transactions in this period");
            return;
        }

        StringBuilder breakdown = new StringBuilder("Breakdown:\n");
        for (Map.Entry<String, Float> entry : categoryAmounts.entrySet()) {
            float percentage = (entry.getValue() / totalAmount) * 100;
            breakdown.append(String.format(Locale.US, "• %s: %s (%.1f%%)\n",
                    entry.getKey(),
                    CurrencyUtils.formatPlainAmount(entry.getValue()),
                    percentage));
        }

        tvCategoryBreakdown.setText(breakdown.toString());
    }

    // Helper that calculate the amount for each category
    private Map<String, Float> calculateCategoryAmounts(List<TransactionGroup> groups) {
        Map<String, Float> categoryAmounts = new HashMap<>();
        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        for (TransactionGroup group : groups) {
            for (Object transactionObj : group.getTransactions()) {

                // Handle both Transaction and TransactionWithCategory
                String category;
                double amount;
                long transactionTime;
                boolean isTransactionExpense;

                if (transactionObj instanceof TransactionWithCategory) {
                    TransactionWithCategory twc = (TransactionWithCategory) transactionObj;
                    category = twc.getCategoryName();
                    amount = twc.getAmount();
                    transactionTime = twc.getDate(); // Already a long
                    isTransactionExpense = twc.isExpense();

                } else if (transactionObj instanceof Transaction) {
                    Transaction transaction = (Transaction) transactionObj;
                    category = transaction.getCategory();
                    amount = transaction.getAmount();
                    transactionTime = transaction.getDate(); // Already a long
                    isTransactionExpense = transaction.isExpense();

                } else {
                    continue; // Skip unknown types
                }

                // Filter by expense type and date range
                if (isTransactionExpense == isExpense &&
                        transactionTime >= startTime &&
                        transactionTime <= endTime) {

                    float floatAmount = (float) amount;
                    categoryAmounts.put(category,
                            categoryAmounts.getOrDefault(category, 0f) + floatAmount);
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

    private void toggleEmptyState(boolean hasData) {
        if (emptyStateView == null || statsScrollView == null) return;
        emptyStateView.setVisibility(hasData ? View.GONE : View.VISIBLE);
        statsScrollView.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private boolean hasTransactionsInRange(List<TransactionGroup> groups) {
        if (groups == null || startDate == null || endDate == null) {
            return false;
        }
        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();

        for (TransactionGroup group : groups) {
            for (Object transactionObj : group.getTransactions()) {
                long transactionTime;
                if (transactionObj instanceof TransactionWithCategory) {
                    transactionTime = ((TransactionWithCategory) transactionObj).getDate();
                } else if (transactionObj instanceof Transaction) {
                    transactionTime = ((Transaction) transactionObj).getDate();
                } else {
                    continue;
                }

                if (transactionTime >= startTime && transactionTime <= endTime) {
                    return true;
                }
            }
        }
        return false;
    }

    private void observeStatisticsAlerts() {
        viewModel.getStatisticsAlerts().observe(getViewLifecycleOwner(), alerts -> {
            if (alerts != null && !alerts.isEmpty()) {
                String text = TextUtils.join("\n\n", alerts);
                tvAIInsights.setText(text);
            } else {
                tvAIInsights.setText("Add transactions to see this month's alerts.");
            }
        });
    }
}