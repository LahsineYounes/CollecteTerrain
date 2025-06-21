package com.example.collecteterrainapp.representative;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.database.DatabaseHelper;
import com.example.collecteterrainapp.services.FirebaseService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FieldValue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddCollecteFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private static final int REQUEST_LOCATION_PERMISSION = 202;
    private static final String TAG = "AddCollecteFragment";

    private EditText nomCollecteEditText;
    private ImageView collectePhotoImageView;
    private EditText amountEditText;
    private EditText commentEditText;
    private Button saveButton;

    private FirebaseService firebaseService;
    private FusedLocationProviderClient fusedLocationClient;

    private Uri photoUri; // To store the URI of the captured/selected photo
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher; // For taking a new picture
    private String currentPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_collecte, container, false);

        nomCollecteEditText = view.findViewById(R.id.nomCollecteEditText);
        collectePhotoImageView = view.findViewById(R.id.collectePhotoImageView);
        amountEditText = view.findViewById(R.id.amountEditText);
        commentEditText = view.findViewById(R.id.commentEditText);
        saveButton = view.findViewById(R.id.saveButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        firebaseService = new FirebaseService();

        // Initialize ActivityResultLaunchers
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        photoUri = result.getData().getData();
                        Glide.with(this).load(photoUri).into(collectePhotoImageView);
                    }
                });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        // photoUri is already set by createImageFile and passed to TakePicture()
                        Glide.with(this).load(photoUri).into(collectePhotoImageView);
                    } else {
                        photoUri = null; // Reset if capture failed or was cancelled
                    }
                });

        collectePhotoImageView.setOnClickListener(v -> showPhotoSourceDialog());

        saveButton.setOnClickListener(v -> prepareAndSaveCollecte());
        return view;
    }

    private void showPhotoSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Source de la photo");
        builder.setItems(new CharSequence[]{"Prendre une photo", "Choisir depuis la galerie"}, (dialog, which) -> {
            if (which == 0) { // Prendre une photo
                checkCameraPermissionAndOpenCamera();
            } else { // Choisir depuis la galerie
                openGallery();
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(getContext(), "Erreur lors de la création du fichier image.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                takePictureLauncher.launch(photoUri);
            }
        } else {
            Toast.makeText(getContext(), "Aucune application appareil photo trouvée.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Permission de localisation non accordée.", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String photoUrlString = null;
                if (photoUri != null) {
                    photoUrlString = photoUri.toString();
                    // TODO: Implement actual photo upload to Firebase Storage and get download URL
                }

                Map<String, Object> collecte = new HashMap<>();
                collecte.put("nomCollecte", nomCollecteEditText.getText().toString().trim());
                collecte.put("amount", Double.parseDouble(amountEditText.getText().toString().trim()));
                collecte.put("comment", commentEditText.getText().toString().trim());
                collecte.put("latitude", latitude);
                collecte.put("longitude", longitude);
                collecte.put("timestamp", FieldValue.serverTimestamp());
                collecte.put("username", requireActivity().getIntent().getStringExtra("username"));
                if (photoUrlString != null) {
                    collecte.put("photoUrl", photoUrlString);
                }

                String finalPhotoUrlString = photoUrlString;
                firebaseService.saveCollection(collecte, new FirebaseService.CollectionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), "Collecte enregistrée en ligne", Toast.LENGTH_SHORT).show();
                        clearForm();
                        navigateBack();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Firebase save failed, saving locally", e);
                        Toast.makeText(getActivity(), "Hors-ligne. Sauvegarde locale", Toast.LENGTH_LONG).show();
                        long timestamp = System.currentTimeMillis();
                        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                        dbHelper.saveLocalCollecte(
                                requireActivity().getIntent().getStringExtra("username"),
                                nomCollecteEditText.getText().toString().trim(),
                                Double.parseDouble(amountEditText.getText().toString().trim()),
                                commentEditText.getText().toString().trim(),
                                latitude,
                                longitude,
                                timestamp,
                                finalPhotoUrlString
                        );
                        clearForm();
                        navigateBack();
                    }
                });

            } else {
                Toast.makeText(getActivity(), "Position non disponible. Veuillez activer la localisation et réessayer.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Erreur lors de l'obtention de la position: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting location", e);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Permission de l'appareil photo refusée.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveCollecteWithLocation();
            } else {
                Toast.makeText(getContext(), "Permission de localisation refusée. Impossible d\'enregistrer la collecte.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prepareAndSaveCollecte() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            saveCollecteWithLocation();
        }
    }

    private void saveCollecteWithLocation() {
        String nomCollecte = nomCollecteEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String comment = commentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nomCollecte)) {
            Toast.makeText(getActivity(), "Veuillez entrer un nom pour la collecte", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(getActivity(), "Veuillez entrer un montant", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(getActivity(), "Montant invalide", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Format du montant incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Erreur: Permission de localisation manquante.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String photoUrlString = null;
                if (photoUri != null) {
                    photoUrlString = photoUri.toString();
                    // TODO: Implement actual photo upload to Firebase Storage and get download URL
                }

                Map<String, Object> collecte = new HashMap<>();
                collecte.put("nomCollecte", nomCollecte);
                collecte.put("amount", amount);
                collecte.put("comment", comment);
                collecte.put("latitude", latitude);
                collecte.put("longitude", longitude);
                collecte.put("timestamp", FieldValue.serverTimestamp());
                collecte.put("username", requireActivity().getIntent().getStringExtra("username"));
                if (photoUrlString != null) {
                    collecte.put("photoUrl", photoUrlString);
                }

                String finalPhotoUrlString = photoUrlString;
                firebaseService.saveCollection(collecte, new FirebaseService.CollectionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), "Collecte enregistrée en ligne", Toast.LENGTH_SHORT).show();
                        clearForm();
                        navigateBack();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Firebase save failed, saving locally", e);
                        Toast.makeText(getActivity(), "Hors-ligne. Sauvegarde locale", Toast.LENGTH_LONG).show();
                        long timestamp = System.currentTimeMillis();
                        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                        dbHelper.saveLocalCollecte(
                                requireActivity().getIntent().getStringExtra("username"),
                                nomCollecte,
                                amount,
                                comment,
                                latitude,
                                longitude,
                                timestamp,
                                finalPhotoUrlString
                        );
                        clearForm();
                        navigateBack();
                    }
                });

            } else {
                Toast.makeText(getActivity(), "Position non disponible. Veuillez activer la localisation et réessayer.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Erreur lors de l'obtention de la position: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting location during save", e);
        });
    }

    private void clearForm() {
        nomCollecteEditText.setText("");
        amountEditText.setText("");
        commentEditText.setText("");
        Glide.with(this).load(R.drawable.ic_add).into(collectePhotoImageView);
        photoUri = null; // Reset photo URI
    }

    private void navigateBack() {
        // Ensure view is available before navigating
        if (getView() != null) {
             NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
             navController.navigateUp();
        } else {
            Log.w(TAG, "View not available for navigation, cannot go back.");
        }
    }
}
