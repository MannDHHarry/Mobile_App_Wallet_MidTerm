package y3.mobiledev.mywallet.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import y3.mobiledev.mywallet.AuthViewModel;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.helpers.LocaleHelper;

public class RegisterFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnBackToLogin;
    private TextView tvChangeLanguage;
    private CheckBox cbRememberMe;
    private ProgressBar progressBar;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();
        updateLanguageDisplay();

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnBackToLogin = view.findViewById(R.id.btnBackToLogin);
        tvChangeLanguage = view.findViewById(R.id.tvChangeLanguage);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        btnBackToLogin.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        tvChangeLanguage.setOnClickListener(v -> showLanguageDialog());

        // Clear error when user starts typing
        android.text.TextWatcher clearErrorWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.clearError();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etName.addTextChangedListener(clearErrorWatcher);
        etEmail.addTextChangedListener(clearErrorWatcher);
        etPassword.addTextChangedListener(clearErrorWatcher);
        etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnRegister.setEnabled(!isLoading);
                btnBackToLogin.setEnabled(!isLoading);
                etName.setEnabled(!isLoading);
                etEmail.setEnabled(!isLoading);
                etPassword.setEnabled(!isLoading);
                etConfirmPassword.setEnabled(!isLoading);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Note: Registration success navigation is handled in AuthActivity
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);

        // Client-side validation
        boolean hasError = false;

        if (name.isEmpty()) {
            etName.setError(getString(R.string.name_required));
            etName.requestFocus();
            hasError = true;
        }

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.email_required));
            if (!hasError) etEmail.requestFocus();
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.valid_email_required));
            if (!hasError) etEmail.requestFocus();
            hasError = true;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.password_required));
            if (!hasError) etPassword.requestFocus();
            hasError = true;
        } else if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_min_length));
            if (!hasError) etPassword.requestFocus();
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(getString(R.string.confirm_password_required));
            if (!hasError) etConfirmPassword.requestFocus();
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_not_match));
            if (!hasError) etConfirmPassword.requestFocus();
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Perform registration (additional validation happens in ViewModel)
        boolean rememberMe = cbRememberMe.isChecked();
        viewModel.register(email, password, name, rememberMe);
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Tiếng Việt"};
        int currentSelection = LocaleHelper.isVietnamese(requireContext()) ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                    String selectedLang = (which == 0) ? LocaleHelper.ENGLISH : LocaleHelper.VIETNAMESE;

                    if (!selectedLang.equals(LocaleHelper.getLanguage(requireContext()))) {
                        LocaleHelper.setLocale(requireContext(), selectedLang);
                        dialog.dismiss();
                        // Recreate activity to apply new language
                        requireActivity().recreate();
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void updateLanguageDisplay() {
        String currentLang = LocaleHelper.isVietnamese(requireContext()) ? "🌐 Tiếng Việt" : "🌐 English";
        tvChangeLanguage.setText(currentLang);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear any error messages when leaving the screen
        viewModel.clearError();
    }
}
