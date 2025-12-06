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

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvChangeLanguage;
    private CheckBox cbRememberMe;
    private ProgressBar progressBar;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();
        updateLanguageDisplay();

        return view;
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);
        tvChangeLanguage = view.findViewById(R.id.tvChangeLanguage);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> navigateToRegister());
        tvChangeLanguage.setOnClickListener(v -> showLanguageDialog());

        // Clear error when user starts typing
        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.clearError();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.clearError();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnLogin.setEnabled(!isLoading);
                etEmail.setEnabled(!isLoading);
                etPassword.setEnabled(!isLoading);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Note: Login success navigation is handled in AuthActivity
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean rememberMe = cbRememberMe.isChecked();

        // Basic client-side validation
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.email_required));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.password_required));
            etPassword.requestFocus();
            return;
        }

        // Clear errors
        etEmail.setError(null);
        etPassword.setError(null);

        // Perform login (validation happens in ViewModel)
        viewModel.login(email, password, rememberMe);
    }

    private void navigateToRegister() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
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
