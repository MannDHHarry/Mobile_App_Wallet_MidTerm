package y3.mobiledev.mywallet;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import y3.mobiledev.mywallet.helpers.NotificationTestHelper;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
public class NotificationUnitTest {
    @Test
    public void noTransactions_returnsZeroSummary() {
        var summary = NotificationTestHelper.calculateTodaySummary(new ArrayList<>());

        assertEquals(0.0, summary.getTotalIncome(), 0.001);
        assertEquals(0.0, summary.getTotalExpense(), 0.001);
        assertEquals(0, summary.getIncomeCount());
        assertEquals(0, summary.getExpenseCount());
        assertFalse(summary.hasTransactions());
    }

    @Test
    public void onlyIncome_calculatesCorrectly() {
        List<TransactionWithCategory> txns = new ArrayList<>();
        txns.add(NotificationTestHelper.createTodayTransaction(500.0, false));
        txns.add(NotificationTestHelper.createTodayTransaction(250.75, false));

        var summary = NotificationTestHelper.calculateTodaySummary(txns);

        assertEquals(750.75, summary.getTotalIncome(), 0.001);
        assertEquals(0.0, summary.getTotalExpense(), 0.001);
        assertEquals(2, summary.getIncomeCount());
        assertEquals(0, summary.getExpenseCount());
        assertEquals(750.75, summary.getNetAmount(), 0.001);
    }

    @Test
    public void mixedTransactions_netCorrect() {
        List<TransactionWithCategory> txns = new ArrayList<>();
        txns.add(NotificationTestHelper.createTodayTransaction(1000.0, false)); // income
        txns.add(NotificationTestHelper.createTodayTransaction(350.0, true));   // expense
        txns.add(NotificationTestHelper.createTodayTransaction(75.5, true));    // expense

        var summary = NotificationTestHelper.calculateTodaySummary(txns);

        assertEquals(1000.0, summary.getTotalIncome(), 0.001);
        assertEquals(425.5, summary.getTotalExpense(), 0.001);
        assertEquals(1, summary.getIncomeCount());
        assertEquals(2, summary.getExpenseCount());
        assertEquals(574.5, summary.getNetAmount(), 0.001);
    }

    @Test
    public void notificationText_matchesExpected() {
        double income = 1200.50;
        double expense = 800.75;
        int incomeCnt = 3;
        int expenseCnt = 5;
        double net = income - expense;

        String expectedBigText = " Income: $1,200.50 (3 transactions)\n" +
                " Expenses: $800.75 (5 transactions)\n" +
                " You saved $399.75 today!";

        String actual = NotificationTestHelper.expectedNotificationContent(
                income, expense, incomeCnt, expenseCnt, net);

        assertEquals(expectedBigText, actual);
    }

    @Test
    public void shortContent_withPositiveNet() {
        String expected = "Income: $1,200.50 | Expenses: $800.75 | Net: +$399.75";
        String actual = NotificationTestHelper.expectedShortContent(1200.50, 800.75, 399.75);
        assertEquals(expected, actual);
    }

    @Test
    public void shortContent_withNegativeNet() {
        String expected = "Income: $500.00 | Expenses: $850.00 | Net: -$350.00";
        String actual = NotificationTestHelper.expectedShortContent(500.0, 850.0, -350.0);
        assertEquals(expected, actual);
    }
}
