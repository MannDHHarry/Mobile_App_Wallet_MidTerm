package y3.mobiledev.mywallet.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import y3.mobiledev.mywallet.AuthViewModel;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.models.User;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvAccountCreated;
    private TextView tvUserId;
    private MaterialButton btnChangeName;
    private MaterialButton btnChangePassword;
    
    private AuthViewModel authViewModel;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupListeners();
        observeUserData();

        return view;
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvAccountCreated = view.findViewById(R.id.tvAccountCreated);
        tvUserId = view.findViewById(R.id.tvUserId);
        btnChangeName = view.findViewById(R.id.btnChangeName);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
    }

    private void setupListeners() {
        btnChangeName.setOnClickListener(v -> showChangeNameDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void observeUserData() {
        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                updateProfileInfo(user);
            }
        });
    }

    private void updateProfileInfo(User user) {
        tvProfileName.setText(user.getName());
        tvProfileEmail.setText(user.getEmail());
        tvUserId.setText(String.format(Locale.getDefault(), "ID: %d", user.getUserId()));

        // Format account creation date
        if (user.getCreatedAt() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(user.getCreatedAt()));
            tvAccountCreated.setText(formattedDate);
        } else {
            tvAccountCreated.setText(getString(R.string.not_available));
        }

        // Set default profile picture
        ivProfilePicture.setImageResource(R.drawable.user);
        ivProfilePicture.setPadding(16, 16, 16, 16);
    }

    private void showChangeNameDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_name, null);
        EditText etNewName = dialogView.findViewById(R.id.etNewName);
        etNewName.setText(currentUser != null ? currentUser.getName() : "");

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.change_name))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String newName = etNewName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        authViewModel.updateUserName(newName);
                        Toast.makeText(requireContext(), getString(R.string.name_updated), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.name_required), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.change_password))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String currentPassword = etCurrentPassword.getText().toString();
                    String newPassword = etNewPassword.getText().toString();
                    String confirmPassword = etConfirmPassword.getText().toString();

                    if (currentPassword.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.current_password_required), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.new_password_required), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(requireContext(), getString(R.string.password_min_length), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(requireContext(), getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    authViewModel.updatePassword(currentPassword, newPassword);
                    Toast.makeText(requireContext(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
