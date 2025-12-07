package y3.mobiledev.mywallet.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.adapters.HistoryPagerAdapter;

public class TransactionHistoryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HistoryPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // CHANGE THIS LINE:
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        initViews(view);
        setupViewPager();

        return view;
    }


    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        pagerAdapter = new HistoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.transactions_tab));
                    break;
                case 1:
                    tab.setText(getString(R.string.transfers_tab));
                    break;
            }
        }).attach();

        // Set default tab to Transactions (position 0)
        viewPager.setCurrentItem(0, false);
        Log.d("HistoryPager", "Total pages: " + pagerAdapter.getItemCount());
    }

}