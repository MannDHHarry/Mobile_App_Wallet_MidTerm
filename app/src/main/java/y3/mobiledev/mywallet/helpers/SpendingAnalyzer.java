package y3.mobiledev.mywallet.helpers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import y3.mobiledev.mywallet.helpers.CurrencyUtils;
import y3.mobiledev.mywallet.models.SpendingAnalysisResult;
import y3.mobiledev.mywallet.models.TransactionWithCategory;

/**
 * Rule-based spending pattern analyzer
 */
public class SpendingAnalyzer {

    private static final String TAG = "SpendingAnalyzer";
    
    // Thresholds for detection
    private static final double SPIKE_THRESHOLD = 2.0; // 2x average = spike
    private static final double DAILY_SPIKE_THRESHOLD = 3.0; // 3x average daily = alert
    private static final double CATEGORY_INCREASE_THRESHOLD = 50.0; // 50% increase = flag
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;

    /**
     * Main analysis method
     */
    public static SpendingAnalysisResult analyzeSpendingPatterns(List<TransactionWithCategory> transactions) {
        try {
            if (transactions == null || transactions.isEmpty()) {
                Log.d(TAG, "No transactions to analyze");
                return createEmptyResult();
            }

            SpendingAnalysisResult result = new SpendingAnalysisResult();

            // Separate income and expenses
            List<TransactionWithCategory> expenses = new ArrayList<>();
            List<TransactionWithCategory> income = new ArrayList<>();
            
            for (TransactionWithCategory t : transactions) {
                if (t != null) {
                    if (t.isExpense()) {
                        expenses.add(t);
                    } else {
                        income.add(t);
                    }
                }
            }

            // Calculate totals
            double totalIncome = calculateTotal(income);
            double totalExpenses = calculateTotal(expenses);
            double netAmount = totalIncome - totalExpenses;
            
            result.setTotalIncome(totalIncome);
            result.setTotalExpenses(totalExpenses);
            result.setNetAmount(netAmount);
            result.setExpensesExceedIncome(netAmount < 0);
            
            // Calculate savings rate
            if (totalIncome > 0) {
                double savingsRate = (netAmount / totalIncome) * 100;
                result.setSavingsRate(savingsRate);
            }

            // Analyze expenses only (for spending patterns)
            if (!expenses.isEmpty()) {
                // Detect spending spikes
                List<SpendingAnalysisResult.SpendingSpike> spikes = detectSpendingSpikes(expenses);
                result.setSpendingSpikes(spikes);

                // Category-wise analysis
                List<SpendingAnalysisResult.CategoryAnalysis> categoryAnalyses = analyzeCategories(expenses);
                result.setCategoryAnalyses(categoryAnalyses);

                // Period comparison
                SpendingAnalysisResult.PeriodComparison weekComparison = compareWeeklyPeriods(expenses);
                SpendingAnalysisResult.PeriodComparison monthComparison = compareMonthlyPeriods(expenses);
                result.setPeriodComparison(monthComparison); // Use monthly by default

                // Detect if spending is increasing
                if (monthComparison != null) {
                    result.setSpendingIncreasing(monthComparison.getChangePercentage() > 0);
                }
            }

            // Generate insights
            List<String> insights = generateInsights(result, expenses, income);
            result.setInsights(insights);

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error in spending analysis", e);
            e.printStackTrace();
            // Return empty result instead of crashing
            return createEmptyResult();
        }
    }

    /**
     * Detect spending spikes (transactions significantly above average)
     */
    private static List<SpendingAnalysisResult.SpendingSpike> detectSpendingSpikes(
            List<TransactionWithCategory> expenses) {
        
        List<SpendingAnalysisResult.SpendingSpike> spikes = new ArrayList<>();
        
        if (expenses.isEmpty()) {
            return spikes;
        }

        // Calculate average per category
        Map<String, CategoryStats> categoryStats = calculateCategoryAverages(expenses);

        // Check each transaction for spikes
        for (TransactionWithCategory transaction : expenses) {
            if (transaction == null || transaction.getCategoryName() == null) {
                continue;
            }
            String categoryName = transaction.getCategoryName();
            CategoryStats stats = categoryStats.get(categoryName);
            
            if (stats != null && stats.average > 0 && transaction.getAmount() > 0) {
                double multiplier = transaction.getAmount() / stats.average;
                
                if (multiplier >= SPIKE_THRESHOLD) {
                    spikes.add(new SpendingAnalysisResult.SpendingSpike(
                            transaction,
                            categoryName,
                            stats.average,
                            multiplier
                    ));
                }
            }
        }

        return spikes;
    }

    /**
     * Calculate category averages
     */
    private static Map<String, CategoryStats> calculateCategoryAverages(
            List<TransactionWithCategory> expenses) {
        
        Map<String, CategoryStats> statsMap = new HashMap<>();
        
        for (TransactionWithCategory t : expenses) {
            if (t == null || t.getCategoryName() == null) {
                continue;
            }
            String category = t.getCategoryName();
            CategoryStats stats = statsMap.getOrDefault(category, new CategoryStats());
            stats.total += t.getAmount();
            stats.count++;
            statsMap.put(category, stats);
        }

        // Calculate averages
        for (CategoryStats stats : statsMap.values()) {
            if (stats.count > 0) {
                stats.average = stats.total / stats.count;
            }
        }

        return statsMap;
    }

    /**
     * Analyze categories for trends
     */
    private static List<SpendingAnalysisResult.CategoryAnalysis> analyzeCategories(
            List<TransactionWithCategory> expenses) {
        
        List<SpendingAnalysisResult.CategoryAnalysis> analyses = new ArrayList<>();
        
        if (expenses.isEmpty()) {
            return analyses;
        }

        // Get current period (last 30 days) and previous period (30-60 days ago)
        long now = System.currentTimeMillis();
        long currentPeriodStart = now - (30 * MILLIS_PER_DAY);
        long previousPeriodStart = currentPeriodStart - (30 * MILLIS_PER_DAY);
        long previousPeriodEnd = currentPeriodStart;

        Map<String, Double> currentPeriodAmounts = new HashMap<>();
        Map<String, Double> previousPeriodAmounts = new HashMap<>();
        Map<String, CategoryStats> categoryStats = calculateCategoryAverages(expenses);

        // Calculate amounts for each period
        for (TransactionWithCategory t : expenses) {
            String category = t.getCategoryName();
            long date = t.getDate();

            if (date >= currentPeriodStart) {
                currentPeriodAmounts.put(category,
                        currentPeriodAmounts.getOrDefault(category, 0.0) + t.getAmount());
            } else if (date >= previousPeriodStart && date < previousPeriodEnd) {
                previousPeriodAmounts.put(category,
                        previousPeriodAmounts.getOrDefault(category, 0.0) + t.getAmount());
            }
        }

        // Create analysis for each category
        for (String category : currentPeriodAmounts.keySet()) {
            double current = currentPeriodAmounts.get(category);
            double previous = previousPeriodAmounts.getOrDefault(category, 0.0);
            double average = categoryStats.containsKey(category) 
                    ? categoryStats.get(category).average : 0.0;

            analyses.add(new SpendingAnalysisResult.CategoryAnalysis(
                    category, current, previous, average
            ));
        }

        return analyses;
    }

    /**
     * Compare current week vs previous week
     */
    private static SpendingAnalysisResult.PeriodComparison compareWeeklyPeriods(
            List<TransactionWithCategory> expenses) {
        
        if (expenses.isEmpty()) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        
        long currentWeekStart = cal.getTimeInMillis();
        long previousWeekStart = currentWeekStart - MILLIS_PER_WEEK;
        long previousWeekEnd = currentWeekStart;

        double currentTotal = 0;
        double previousTotal = 0;

        for (TransactionWithCategory t : expenses) {
            long date = t.getDate();
            if (date >= currentWeekStart) {
                currentTotal += t.getAmount();
            } else if (date >= previousWeekStart && date < previousWeekEnd) {
                previousTotal += t.getAmount();
            }
        }

        return new SpendingAnalysisResult.PeriodComparison(
                currentTotal, previousTotal, "week"
        );
    }

    /**
     * Compare current month vs previous month
     */
    private static SpendingAnalysisResult.PeriodComparison compareMonthlyPeriods(
            List<TransactionWithCategory> expenses) {
        
        if (expenses.isEmpty()) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        long currentMonthStart = cal.getTimeInMillis();
        
        cal.add(Calendar.MONTH, -1);
        long previousMonthStart = cal.getTimeInMillis();
        
        cal.add(Calendar.MONTH, 1);
        long previousMonthEnd = currentMonthStart;

        double currentTotal = 0;
        double previousTotal = 0;

        for (TransactionWithCategory t : expenses) {
            long date = t.getDate();
            if (date >= currentMonthStart) {
                currentTotal += t.getAmount();
            } else if (date >= previousMonthStart && date < previousMonthEnd) {
                previousTotal += t.getAmount();
            }
        }

        return new SpendingAnalysisResult.PeriodComparison(
                currentTotal, previousTotal, "month"
        );
    }

    /**
     * Generate human-readable insights
     */
    private static List<String> generateInsights(SpendingAnalysisResult result,
                                                 List<TransactionWithCategory> expenses,
                                                 List<TransactionWithCategory> income) {
        
        List<String> insights = new ArrayList<>();

        // Savings insights
        if (result.isExpensesExceedIncome()) {
            insights.add(String.format(Locale.US,
                    "⚠️ Your expenses (%s) exceeded your income (%s) this period.",
                    CurrencyUtils.formatPlainAmount(result.getTotalExpenses()),
                    CurrencyUtils.formatPlainAmount(result.getTotalIncome())));
        } else if (result.getSavingsRate() > 0) {
            insights.add(String.format(Locale.US,
                    "✅ Great! You saved %.1f%% of your income this period.",
                    result.getSavingsRate()));
        }

        // Spending spikes
        if (!result.getSpendingSpikes().isEmpty()) {
            int spikeCount = result.getSpendingSpikes().size();
            insights.add(String.format(Locale.US,
                    "📈 Detected %d unusual spending spike(s). Review your transactions.",
                    spikeCount));
        }

        // Category trends
        for (SpendingAnalysisResult.CategoryAnalysis analysis : result.getCategoryAnalyses()) {
            if (Math.abs(analysis.getChangePercentage()) >= CATEGORY_INCREASE_THRESHOLD) {
                if (analysis.isIncreasing()) {
                    insights.add(String.format(Locale.US,
                            "📊 %s spending increased by %.1f%% compared to last period.",
                            analysis.getCategoryName(), analysis.getChangePercentage()));
                } else {
                    insights.add(String.format(Locale.US,
                            "📉 %s spending decreased by %.1f%% compared to last period.",
                            analysis.getCategoryName(), Math.abs(analysis.getChangePercentage())));
                }
            }
        }

        // Period comparison
        if (result.getPeriodComparison() != null) {
            SpendingAnalysisResult.PeriodComparison comparison = result.getPeriodComparison();
            if (Math.abs(comparison.getChangePercentage()) > 20) {
                if (comparison.getChangePercentage() > 0) {
                    insights.add(String.format(Locale.US,
                            "📈 Your spending increased by %.1f%% this %s.",
                            comparison.getChangePercentage(), comparison.getPeriodType()));
                } else {
                    insights.add(String.format(Locale.US,
                            "📉 Your spending decreased by %.1f%% this %s.",
                            Math.abs(comparison.getChangePercentage()), comparison.getPeriodType()));
                }
            }
        }

        // Top spending category
        if (!result.getCategoryAnalyses().isEmpty()) {
            SpendingAnalysisResult.CategoryAnalysis topCategory = result.getCategoryAnalyses().get(0);
            for (SpendingAnalysisResult.CategoryAnalysis analysis : result.getCategoryAnalyses()) {
                if (analysis.getCurrentAmount() > topCategory.getCurrentAmount()) {
                    topCategory = analysis;
                }
            }
            insights.add(String.format(Locale.US,
                    "💰 %s is your top spending category (%s).",
                    topCategory.getCategoryName(),
                    CurrencyUtils.formatPlainAmount(topCategory.getCurrentAmount())));
        }

        if (insights.isEmpty()) {
            insights.add("💡 Keep tracking your expenses to get personalized insights!");
        }

        return insights;
    }

    /**
     * Calculate total amount from transactions
     */
    private static double calculateTotal(List<TransactionWithCategory> transactions) {
        double total = 0;
        for (TransactionWithCategory t : transactions) {
            total += t.getAmount();
        }
        return total;
    }

    /**
     * Create empty result
     */
    private static SpendingAnalysisResult createEmptyResult() {
        SpendingAnalysisResult result = new SpendingAnalysisResult();
        result.setInsights(new ArrayList<>());
        result.getInsights().add("No transactions to analyze yet. Start adding transactions!");
        return result;
    }

    /**
     * Helper class for category statistics
     */
    private static class CategoryStats {
        double total = 0;
        int count = 0;
        double average = 0;
    }
}

