package com.example.collecteterrainapp.shared;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.collecteterrainapp.MainActivity;
import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.database.DatabaseHelper;
import com.example.collecteterrainapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_LOCATION = 2;

    private ImageView profilePhoto;
    private TextView nameText, roleText, birthText;
    private EditText cityEdit;
    private Button changePhotoBtn, editBtn, logoutBtn, detectCityBtn;
    private String photoPath = null;
    private int userId;
    private String username;

    private DatabaseHelper dbHelper;
    private boolean isEditing = false;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePhoto = view.findViewById(R.id.profile_photo);
        nameText = view.findViewById(R.id.profile_name);
        roleText = view.findViewById(R.id.profile_role);
        birthText = view.findViewById(R.id.profile_birth);
        cityEdit = view.findViewById(R.id.profile_city);
        changePhotoBtn = view.findViewById(R.id.change_photo_button);
        editBtn = view.findViewById(R.id.edit_profile_button);
        logoutBtn = view.findViewById(R.id.logout_button);
        detectCityBtn = view.findViewById(R.id.detect_city_button);

        dbHelper = new DatabaseHelper(getActivity());
        sessionManager = SessionManager.getInstance(requireActivity());

        // username = sessionManager.getUsername(); // Keep for db query if needed for other fields
        // if (username == null) username = "Utilisateur";

        // Directly use session data for non-editable name and role
        String currentDisplayName = sessionManager.getDisplayName();
        String currentRole = sessionManager.getRole();

        if (nameText != null) {
            nameText.setText(currentDisplayName != null ? currentDisplayName : "Utilisateur");
        }
        if (roleText != null && currentRole != null) {
            String roleLabel = "représentant".equals(currentRole) ? "représentant" : "manager";
            roleText.setText("Je suis un " + roleLabel);
        } else if (roleText != null) {
            roleText.setText("Rôle non défini");
        }

        username = sessionManager.getUsername(); // Needed for loadProfile to fetch editable fields
        loadProfile(); // Load editable fields (birth, city, photo) and get userId

        setEditing(false);

        changePhotoBtn.setOnClickListener(v -> {
            if (isEditing) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
            }
        });

        birthText.setOnClickListener(v -> {
            if (isEditing) {
                showDatePicker();
            }
        });

        detectCityBtn.setOnClickListener(v -> {
            if (isEditing) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                } else {
                    detectCity();
                }
            }
        });

        editBtn.setOnClickListener(v -> {
            if (isEditing) {
                saveProfile();
                setEditing(false);
            } else {
                setEditing(true);
            }
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            sessionManager.clearSession();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void setEditing(boolean editing) {
        isEditing = editing;
        cityEdit.setEnabled(editing);
        changePhotoBtn.setEnabled(editing);
        detectCityBtn.setEnabled(editing);
        birthText.setEnabled(editing);
        editBtn.setText(editing ? "Enregistrer" : "Modifier");
        if (editing) {
            cityEdit.requestFocus();
        }
    }

    private void loadProfile() {
        if (username == null) { // Get username from session if not already set
            username = sessionManager.getUsername();
            if (username == null) {
                // Handle case where username is still null (e.g., show error, return)
                if (nameText != null) nameText.setText("Erreur: Utilisateur inconnu");
                return;
            }
        }

        Cursor cursor = dbHelper.getUserByUsername(username);
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            // String name = cursor.getString(cursor.getColumnIndexOrThrow("username")); // Already set from session
            String birth = cursor.getString(cursor.getColumnIndexOrThrow("birth"));
            String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
            // String role = cursor.getString(cursor.getColumnIndexOrThrow("role")); // Already set from session
            photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));

            // nameText.setText(name); // Already set
            birthText.setText(birth != null ? birth : "Non renseignée");
            cityEdit.setText(city != null ? city : "");

            // Role text already set from session data directly in onCreateView
            // if (roleText != null) {
            // String roleLabel = "représentant".equals(role) ? "représentant" : "manager";
            // roleText.setText("Je suis un " + roleLabel);
            // }

            if (photoPath != null && !photoPath.isEmpty()) {
                profilePhoto.setImageURI(Uri.fromFile(new File(photoPath)));
            } else {
                 // Use photo from session if available and path is empty, otherwise placeholder
                String sessionPhotoUrl = sessionManager.getPhotoUrl();
                if (sessionPhotoUrl != null && !sessionPhotoUrl.isEmpty()) {
                    profilePhoto.setImageURI(Uri.fromFile(new File(sessionPhotoUrl))); 
                } else {
                    profilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }
        }
        cursor.close();
    }

    private void saveProfile() {
        String birth = birthText.getText().toString();
        String city = cityEdit.getText().toString();
        dbHelper.updateUserProfile(userId, birth, city, photoPath);
        Toast.makeText(getActivity(), "Profil enregistré", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String currentBirth = birthText.getText().toString();
        if (currentBirth != null && currentBirth.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            String[] parts = currentBirth.split("/");
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
        }
        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
            birthText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null) {
            Uri selectedImage = data.getData();
            profilePhoto.setImageURI(selectedImage);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                photoPath = saveImageToInternalStorage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = getActivity().getDir("profile", getActivity().MODE_PRIVATE);
        String fileName = "profile_photo_" + userId + ".jpg";
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectCity();
        } else {
            Toast.makeText(getActivity(), "Permission localisation refusée", Toast.LENGTH_SHORT).show();
        }
    }

    private void detectCity() {
        com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient =
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getCityFromLocation(location);
                    } else {
                        Toast.makeText(getActivity(), "Impossible de détecter la localisation", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getCityFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                cityEdit.setText(city != null ? city : "");
            } else {
                Toast.makeText(getActivity(), "Ville non trouvée", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Erreur lors de la détection de la ville", Toast.LENGTH_SHORT).show();
        }
    }
}

