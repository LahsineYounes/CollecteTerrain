package com.example.collecteterrainapp.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.database.DatabaseHelper;
import com.example.collecteterrainapp.manager.ManagerDashboardActivity;
import com.example.collecteterrainapp.representative.RepresentantDashboardActivity;
import com.example.collecteterrainapp.utils.AuthUtils;
import com.example.collecteterrainapp.utils.SessionManager;

public class LoginFragment extends Fragment {

    private EditText usernameEditText, passwordEditText;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        Button loginButton = view.findViewById(R.id.login_button);

        dbHelper = new DatabaseHelper(getActivity());

        // Pré-remplir les champs si arguments reçus
        Bundle args = getArguments();
        if (args != null) {
            usernameEditText.setText(args.getString("username", ""));
            passwordEditText.setText(args.getString("password", ""));
            loginButton.requestFocus();
        }

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedPassword = AuthUtils.hashPassword(password);

            if (dbHelper.checkUser(username, hashedPassword)) {
                Cursor cursor = dbHelper.getUserByUsername(username);
                if (cursor != null && cursor.moveToFirst()) {
                    // Retrieve all necessary user details from cursor
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String userUsername = cursor.getString(cursor.getColumnIndexOrThrow("username")); // This is the display name
                    String userRole = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                    String userPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));
                    // String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("email")); // Assuming 'email' column exists
                    // String userUid = cursor.getString(cursor.getColumnIndexOrThrow("uid")); // Assuming 'uid' column for Firebase UID exists

                    // Sauvegarder la session en utilisant SessionManager
                    SessionManager sessionManager = SessionManager.getInstance(requireActivity());
                    sessionManager.createLoginSession(userId, userUsername, userUsername, userRole, userPhotoPath);

                    // Rediriger selon le rôle
                    Intent intent = "manager".equals(userRole)
                            ? new Intent(getActivity(), ManagerDashboardActivity.class)
                            : new Intent(getActivity(), RepresentantDashboardActivity.class);

                    // Pass data via intent (can be reduced if SessionManager is fully implemented)
                    intent.putExtra("userId", userId);
                    intent.putExtra("username", userUsername);
                    intent.putExtra("displayName", userUsername);
                    intent.putExtra("role", userRole);
                    if (userPhotoPath != null) {
                        intent.putExtra("photoUrl", userPhotoPath);
                    }
                    // intent.putExtra("email", userEmail);
                    // intent.putExtra("uid", userUid);

                    startActivity(intent);
                    getActivity().finish();
                    cursor.close();
                }
            } else {
                Toast.makeText(getActivity(), "Identifiants incorrects", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
