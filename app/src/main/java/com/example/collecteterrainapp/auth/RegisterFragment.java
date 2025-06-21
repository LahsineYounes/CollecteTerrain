package com.example.collecteterrainapp.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.database.DatabaseHelper;
import com.example.collecteterrainapp.utils.AuthUtils;

public class RegisterFragment extends Fragment {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup userTypeGroup;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password);
        userTypeGroup = view.findViewById(R.id.user_type_group);
        Button signupButton = view.findViewById(R.id.signup_button);

        dbHelper = new DatabaseHelper(getActivity());

        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            int selectedId = userTypeGroup.getCheckedRadioButtonId();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedId == -1) {
                Toast.makeText(getActivity(), "Veuillez sélectionner un rôle", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = (selectedId == R.id.manager_radio) ? "manager" : "representative";
            String hashedPassword = AuthUtils.hashPassword(password);

            if (dbHelper.addUser(username, hashedPassword, role)) {
                Toast.makeText(getActivity(), "Inscription réussie", Toast.LENGTH_SHORT).show();

                // Aller au login avec infos pré-remplies
                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                bundle.putString("password", password);

                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, loginFragment)
                        .commit();
            } else {
                Toast.makeText(getActivity(), "Nom d'utilisateur déjà utilisé", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
