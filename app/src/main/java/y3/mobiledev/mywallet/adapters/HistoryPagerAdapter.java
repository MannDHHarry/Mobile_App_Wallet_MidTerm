package y3.mobiledev.mywallet.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import y3.mobiledev.mywallet.fragments.TransactionsTabFragment;
import y3.mobiledev.mywallet.fragments.TransfersFragment;

public class HistoryPagerAdapter extends FragmentStateAdapter {

    public HistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TransactionsTabFragment(); // Your existing transaction list
            case 1:
                return new TransfersFragment(); // New transfers list
            default:
                return new TransactionsTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Transactions and Transfers
    }
}