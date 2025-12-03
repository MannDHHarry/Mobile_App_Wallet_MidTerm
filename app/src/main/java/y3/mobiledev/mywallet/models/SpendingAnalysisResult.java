package y3.mobiledev.mywallet.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to store spending analysis results
 */
public class SpendingAnalysisResult {

    // Spending spikes detected
    private List<SpendingSpike> spendingSpikes = new ArrayList<>();

    // Category-wise analysis
    private List<CategoryAnalysis> categoryAnalyses = new ArrayList<>();

    // Period comparison
    private PeriodComparison periodComparison;

    // Generated insights
    private List<String> insights = new ArrayList<>();

    // Savings analysis
    private double totalIncome;
    private double totalExpenses;
    private double netAmount;
    private double savingsRate; // Percentage

    // Trend indicators
    private boolean expensesExceedIncome;
    private boolean spendingIncreasing;

    // Getters and Setters
    public List<SpendingSpike> getSpendingSpikes() {
        return spendingSpikes;
    }

    public void setSpendingSpikes(List<SpendingSpike> spendingSpikes) {
        this.spendingSpikes = spendingSpikes;
    }

    public List<CategoryAnalysis> getCategoryAnalyses() {
        return categoryAnalyses;
    }

    public void setCategoryAnalyses(List<CategoryAnalysis> categoryAnalyses) {
        this.categoryAnalyses = categoryAnalyses;
    }

    public PeriodComparison getPeriodComparison() {
        return periodComparison;
    }

    public void setPeriodComparison(PeriodComparison periodComparison) {
        this.periodComparison = periodComparison;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(double savingsRate) {
        this.savingsRate = savingsRate;
    }

    public boolean isExpensesExceedIncome() {
        return expensesExceedIncome;
    }

    public void setExpensesExceedIncome(boolean expensesExceedIncome) {
        this.expensesExceedIncome = expensesExceedIncome;
    }

    public boolean isSpendingIncreasing() {
        return spendingIncreasing;
    }

    public void setSpendingIncreasing(boolean spendingIncreasing) {
        this.spendingIncreasing = spendingIncreasing;
    }

    // Inner classes for structured data
    public static class SpendingSpike {
        private TransactionWithCategory transaction;
        private String categoryName;
        private double averageAmount;
        private double spikeMultiplier; // How many times above average

        public SpendingSpike(TransactionWithCategory transaction, String categoryName,
                           double averageAmount, double spikeMultiplier) {
            this.transaction = transaction;
            this.categoryName = categoryName;
            this.averageAmount = averageAmount;
            this.spikeMultiplier = spikeMultiplier;
        }

        public TransactionWithCategory getTransaction() {
            return transaction;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getAverageAmount() {
            return averageAmount;
        }

        public double getSpikeMultiplier() {
            return spikeMultiplier;
        }
    }

    public static class CategoryAnalysis {
        private String categoryName;
        private double currentAmount;
        private double previousAmount;
        private double changePercentage;
        private double averageAmount;
        private boolean isIncreasing;

        public CategoryAnalysis(String categoryName, double currentAmount,
                               double previousAmount, double averageAmount) {
            this.categoryName = categoryName;
            this.currentAmount = currentAmount;
            this.previousAmount = previousAmount;
            this.averageAmount = averageAmount;
            
            if (previousAmount > 0) {
                this.changePercentage = ((currentAmount - previousAmount) / previousAmount) * 100;
                this.isIncreasing = currentAmount > previousAmount;
            } else {
                this.changePercentage = currentAmount > 0 ? 100 : 0;
                this.isIncreasing = currentAmount > 0;
            }
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getCurrentAmount() {
            return currentAmount;
        }

        public double getPreviousAmount() {
            return previousAmount;
        }

        public double getChangePercentage() {
            return changePercentage;
        }

        public double getAverageAmount() {
            return averageAmount;
        }

        public boolean isIncreasing() {
            return isIncreasing;
        }
    }

    public static class PeriodComparison {
        private double currentPeriodTotal;
        private double previousPeriodTotal;
        private double changePercentage;
        private String periodType; // "week" or "month"

        public PeriodComparison(double currentPeriodTotal, double previousPeriodTotal, String periodType) {
            this.currentPeriodTotal = currentPeriodTotal;
            this.previousPeriodTotal = previousPeriodTotal;
            this.periodType = periodType;
            
            if (previousPeriodTotal > 0) {
                this.changePercentage = ((currentPeriodTotal - previousPeriodTotal) / previousPeriodTotal) * 100;
            } else {
                this.changePercentage = currentPeriodTotal > 0 ? 100 : 0;
            }
        }

        public double getCurrentPeriodTotal() {
            return currentPeriodTotal;
        }

        public double getPreviousPeriodTotal() {
            return previousPeriodTotal;
        }

        public double getChangePercentage() {
            return changePercentage;
        }

        public String getPeriodType() {
            return periodType;
        }
    }
}

