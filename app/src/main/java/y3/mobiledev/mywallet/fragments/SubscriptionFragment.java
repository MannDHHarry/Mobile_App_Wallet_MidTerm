package y3.mobiledev.mywallet.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import y3.mobiledev.mywallet.adapters.SubscriptionAdapter;
import y3.mobiledev.mywallet.helpers.PickersAndDialog;
import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionFragment extends Fragment {

    private TextView tvTotalSubscriptions;
    private RecyclerView rvSubscriptions;
    private View emptyState;
    private FloatingActionButton fabAddSubscription;

    private SubscriptionAdapter adapter;
    private TransactionViewModel viewModel;
    private List<Subscription> subscriptions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeData();

        return view;
    }

    private void initViews(View view) {
        tvTotalSubscriptions = view.findViewById(R.id.tvTotalSubscriptions);
        rvSubscriptions = view.findViewById(R.id.rvSubscriptions);
        emptyState = view.findViewById(R.id.emptyStateSubscriptions);
        fabAddSubscription = view.findViewById(R.id.fabAddSubscription);
    }

    private void setupRecyclerView() {
        adapter = new SubscriptionAdapter(requireContext(), subscriptions,
                new SubscriptionAdapter.OnSubscriptionActionListener() {
                    @Override
                    public void onEditClick(Subscription subscription) {
                        showEditSubscriptionDialog(subscription);
                    }

                    @Override
                    public void onToggleClick(Subscription subscription) {
                        toggleSubscriptionStatus(subscription);
                    }

                    @Override
                    public void onDeleteClick(Subscription subscription) {
                        showDeleteConfirmation(subscription);
                    }
                });

        rvSubscriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubscriptions.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddSubscription.setOnClickListener(v -> showAddSubscriptionDialog());
    }

    private void observeData() {
        viewModel.getAllSubscriptions().observe(getViewLifecycleOwner(), subscriptionList -> {
            if (subscriptionList != null) {
                subscriptions = subscriptionList;
                adapter.updateSubscriptions(subscriptions);
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (subscriptions.isEmpty()) {
            rvSubscriptions.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            tvTotalSubscriptions.setText("Total Monthly: 0 ₫");
        } else {
            rvSubscriptions.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            // Calculate total monthly amount
            double total = 0;
            for (Subscription sub : subscriptions) {
                if (sub.isActive()) {
                    total += sub.getAmount();
                }
            }
            tvTotalSubscriptions.setText(
                    "Total Monthly: " + y3.mobiledev.mywallet.helpers.CurrencyUtils.formatPlainAmount(total)
            );
        }
    }

    private void showAddSubscriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_subscription, null);

        EditText etName = dialogView.findViewById(R.id.etSubscriptionName);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        RelativeLayout layoutWalletPicker = dialogView.findViewById(R.id.layoutWalletPicker);
        TextView tvSelectedWallet = dialogView.findViewById(R.id.tvSelectedWallet);
        RelativeLayout layoutDatePicker = dialogView.findViewById(R.id.layoutDatePicker);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        final Wallet[] selectedWallet = {null};
        final Date[] selectedDate = {new Date()};

        // Update date display
        updateDateDisplay(tvSelectedDate, selectedDate[0]);

        // Wallet picker
        layoutWalletPicker.setOnClickListener(v -> {
            List<Wallet> wallets = viewModel.getWallets().getValue();
            if (wallets != null && !wallets.isEmpty()) {
                PickersAndDialog.showWalletPicker(requireContext(), wallets, item -> {
                    if (item instanceof Wallet) {
                        selectedWallet[0] = (Wallet) item;
                        tvSelectedWallet.setText(selectedWallet[0].getName());
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Please create a wallet first",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Date picker
        layoutDatePicker.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate[0]);

            new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, dayOfMonth);
                        selectedDate[0] = cal.getTime();
                        updateDateDisplay(tvSelectedDate, selectedDate[0]);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter subscription name",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter amount",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedWallet[0] == null) {
                Toast.makeText(requireContext(), "Please select a wallet",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Amount must be greater than 0",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.addSubscription(
                        selectedWallet[0].getWalletId(),
                        name,
                        amount,
                        selectedDate[0].getTime(),
                        notes
                );

                Toast.makeText(requireContext(), "Subscription added successfully!",
                        Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid amount",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditSubscriptionDialog(Subscription subscription) {
        // Similar to add dialog but pre-filled with subscription data
        Toast.makeText(requireContext(), "Edit: " + subscription.getName(),
                Toast.LENGTH_SHORT).show();
    }

    private void toggleSubscriptionStatus(Subscription subscription) {
        boolean newStatus = !subscription.isActive();
        viewModel.toggleSubscriptionStatus(subscription.getSubscriptionId(), newStatus);

        String message = newStatus ? "Subscription resumed" : "Subscription paused";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(Subscription subscription) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Subscription?")
                .setMessage("Are you sure you want to delete \"" +
                        subscription.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSubscription(subscription);
                    Toast.makeText(requireContext(), "Subscription deleted",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDateDisplay(TextView textView, Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US);
        textView.setText(format.format(date));
    }
}