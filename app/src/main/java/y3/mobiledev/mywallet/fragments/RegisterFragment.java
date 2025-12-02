package y3.mobiledev.mywallet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import y3.mobiledev.mywallet.AuthViewModel;
import y3.mobiledev.mywallet.R;

public class RegisterFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnBackToLogin;
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

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnBackToLogin = view.findViewById(R.id.btnBackToLogin);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        btnBackToLogin.setOnClickListener(v -> getParentFragmentManager().popBackStack());

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
            etName.setError("Name is required");
            etName.requestFocus();
            hasError = true;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            if (!hasError) etEmail.requestFocus();
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            if (!hasError) etEmail.requestFocus();
            hasError = true;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            if (!hasError) etPassword.requestFocus();
            hasError = true;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            if (!hasError) etPassword.requestFocus();
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            if (!hasError) etConfirmPassword.requestFocus();
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear any error messages when leaving the screen
        viewModel.clearError();
    }
}