package com.example.sasct.db;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sasct.model.Offer;
import com.example.sasct.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;


    public DatabaseHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Method to get the current user ID
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null;
        }
    }

    public void getCurrentUserRole(OnUserRoleListener listener) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            listener.onFailure("User is not logged in");
            return;
        }

        DocumentReference docRef = db.collection("users").document(currentUserId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            listener.onSuccess(role);
                        } else {
                            listener.onFailure("Role not found");
                        }
                    } else {
                        listener.onFailure("User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onFailure("Error fetching user role: " + e.getMessage());
                });
    }



    public void getAllOffers(final OnOffersRetrievedListener listener) {
        String userId = getCurrentUserId();
        List<Offer> offers = new ArrayList<>();
        db.collection("offers")
                .whereEqualTo("type", "public")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Offer offer = document.toObject(Offer.class);
                            if(Objects.equals(offer.getUserId(), userId)){
                                continue;
                            }
                            offers.add(offer);
                        }
                        listener.onOffersRetrieved(offers);
                    } else {
                        Log.d(TAG, "Error getting offers: ", task.getException());
                        listener.onOffersRetrieved(Collections.emptyList());
                    }
                });
    }

    public void getOffersByCategory(String category, final OnOffersRetrievedListener listener) {
        if (category == null) {
            Log.e(TAG, "Category is null");
            listener.onOffersRetrieved(Collections.emptyList());
            return;
        }

        String userId = getCurrentUserId();
        List<Offer> offers = new ArrayList<>();

        db.collection("offers")
                .whereEqualTo("category" , category)
                .whereEqualTo("type", "public")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Offer offer = document.toObject(Offer.class);
                            if(Objects.equals(offer.getUserId(), userId)){
                                continue;
                            }
                            offers.add(offer);
                        }
                        listener.onOffersRetrieved(offers);  // Call the listener with the list of offers
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        listener.onOffersRetrieved(Collections.emptyList());  // Call the listener with an empty list on failure
                    }
                });
    }



    // Method to create the "offers" collection in Firestore
    public void createOffersCollection() {
        db.collection("offers").document("anything").set(new HashMap<>())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Offers collection created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create offers collection: " + e.getMessage()));
    }

    // Method to CRUD the Offer
    // Method to Create Offer
    public void createOffer(String userId, String title, String description, String category, String shipment, double price, int quantity, Uri imageUri, OnOfferCreatedListener listener) {
        CollectionReference offersCollection = db.collection("offers");

        // Check if the offer title already exists for the current user
        offersCollection.whereEqualTo("userId", getCurrentUserId())
            .whereEqualTo("title", title.toLowerCase()) // Ensure the query is case-insensitive
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // If there are documents, the title is not unique
                    listener.onOfferCreationFailed("An offer with the same title already exists.");
                } else {
                    CollectionReference offersCollection2 = db.collection("offers");

                    // Upload the image to Firebase Storage
                    String imageName = System.currentTimeMillis() + ".jpg";
                    storageRef = FirebaseStorage.getInstance().getReference().child("offer_images").child(imageName);
                    storageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Image uploaded successfully, get the download URL
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Create an Offer object with the data
                                Offer offer = new Offer(getCurrentUserId(), title.toLowerCase(), description, category, shipment, price, quantity, uri.toString()); // Setting userID as null temporarily

                                // Add the offer to the "offers" collection
                                offersCollection2.add(offer)
                                    .addOnSuccessListener(documentReference -> {
                                        // Offer added successfully
                                        String offerId = documentReference.getId();
                                        offer.setId(offerId); // Set the generated ID

                                        // Update the offer with the correct user ID
                                        offer.setUserId(getCurrentUserId());

                                        // Update the offer in Firestore with the correct user ID
                                        documentReference.set(offer)
                                            .addOnSuccessListener(aVoid -> {
                                                listener.onOfferCreated(offer);
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle failure
                                                Log.e(TAG, "Failed to update offer with user ID: " + e.getMessage());
                                                listener.onOfferCreationFailed(e.getMessage());
                                            });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Log.e(TAG, "Failed to create offer: " + e.getMessage());
                                        listener.onOfferCreationFailed(e.getMessage());
                                    });
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Handle image upload failure
                            Log.e(TAG, "Failed to upload image: " + e.getMessage());
                            listener.onOfferCreationFailed(e.getMessage());
                    });
                }
            })
            .addOnFailureListener(e -> {
                // Handle failure in checking existing titles
                Log.e(TAG, "Failed to check for existing titles: " + e.getMessage());
                listener.onOfferCreationFailed(e.getMessage());
            });
    }

    // Method to Read offers for the current user
    public void getOffersForCurrentUser(OnOffersRetrievedListener listener) {
        String userId = getCurrentUserId();
        List<Offer> offers = new ArrayList<>();
        if (userId != null) {
            db.collection("offers")
                    .whereEqualTo("userId", userId) // Query offers for the current user
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Offer offer = document.toObject(Offer.class);
                                offers.add(offer);
                            }
                            listener.onOffersRetrieved(offers);
                        } else {
                            Log.d(TAG, "Error getting offers: ", task.getException());
                            listener.onOffersRetrieved(Collections.emptyList());
                        }
                    });
        }
    }


    // Method to Update Offer
    public void updateOffer(Offer offer, OnOfferUpdatedListener listener) {
        offer.setTitle(offer.getTitle().toLowerCase());
        db.collection("offers").document(offer.getId())
                .set(offer)
                .addOnSuccessListener(aVoid -> {
                    listener.onOfferUpdated();
                })
                .addOnFailureListener(e -> {
                    listener.onOfferUpdateFailed(e.getMessage());
                });
    }


    // Method to Delete Offer
    public void deleteOffer(Offer offer, OnOfferDeletedListener listener) {
        // Delete the offer document from FireStore
        db.collection("offers").document(offer.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Offer deleted successfully
                listener.onOfferDeleted();
            })
            .addOnFailureListener(e -> {
                // Failed to delete offer
                listener.onOfferDeletionFailed(e.getMessage());
            });
    }

    // Get User Role
    public void getUserData(String userId, OnUserRetrievedListener listener) {
        // Assuming you have a Firestore database reference
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the document reference for the user ID
        DocumentReference userRef = db.collection("users").document(userId);

        // Retrieve user data from Firestore
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Convert Firestore document to User object
                        User user = document.toObject(User.class);
                        // Pass the user object to the listener
                        listener.onUserRetrieved(user);
                    } else {
                        // User document does not exist
                        listener.onUserRetrievalFailed("User document does not exist");
                    }
                } else {
                    // Failed to retrieve user data
                    listener.onUserRetrievalFailed(task.getException().getMessage());
                }
            }
        });
    }

    public void updateUserData(User user, @Nullable Uri imageUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getCurrentUserId();
        if (userId == null) {
            // Handle case where user ID is not available
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (user.getCompanyName() != null && !user.getCompanyName().trim().isEmpty()) {
            updates.put("companyName", user.getCompanyName().trim());
        }
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            updates.put("email", user.getEmail().trim());
        }

        if (!updates.isEmpty()) {
            db.collection("users").document(userId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Data updated successfully
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occur during the update
                    });
        }

        if (imageUri != null) {
            // Assuming `uploadImage` is a method that handles image uploads
            uploadImage(userId, imageUri, new OnImageUploadedListener() {
                @Override
                public void onImageUploaded(String imageUrl) {
                    Toast.makeText(context, "Image uploaded successfully: " + imageUrl, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUploadFailed(String error) {
                    Toast.makeText(context, "Failed to upload image: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void uploadImage(String userId, Uri imageUri, OnImageUploadedListener listener) {
        if (userId == null || imageUri == null) {
            Log.e(TAG, "User ID or Image URI is null");
            listener.onUploadFailed("Invalid data");
            return;
        }

        String imageName = System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("user_images").child(imageName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Here we update the Firestore with the new image URL
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                                .update("imageUrl", downloadUri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    listener.onImageUploaded(downloadUri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update user image URL: " + e.getMessage());
                                    listener.onUploadFailed(e.getMessage());
                                });
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                        listener.onUploadFailed(e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle image upload failure
                    Log.e(TAG, "Failed to upload image: " + e.getMessage());
                    listener.onUploadFailed(e.getMessage());
                });
    }


    public void searchOffers(String query, String category, final OnOffersRetrievedListener listener) {
        CollectionReference offersCollection = db.collection("offers");

        // Convert the search query to lowercase to match the stored format
        query = query.toLowerCase();
        if(category != null && !category.equals("Select Category")) {
            offersCollection
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + '\uf8ff')
                    .whereEqualTo("category", category)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Offer> offers = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                offers.add(document.toObject(Offer.class));
                            }
                            listener.onOffersRetrieved(offers);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            listener.onOffersRetrieved(Collections.emptyList());
                        }
                    });
        } else {
            offersCollection
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + '\uf8ff')
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Offer> offers = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                offers.add(document.toObject(Offer.class));
                            }
                            listener.onOffersRetrieved(offers);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            listener.onOffersRetrieved(Collections.emptyList());
                        }
                    });
        }
    }

    // Interface for callback when image upload and URL update is complete
    public interface OnUserRoleListener {
        void onSuccess(String role);
        void onFailure(String errorMessage);
    }

    public interface OnImageUploadedListener {
        void onImageUploaded(String imageUrl);
        void onUploadFailed(String error);
    }

    // Interface to listen for user retrieval events
    public interface OnUserRetrievedListener {
        void onUserRetrieved(User user);
        void onUserRetrievalFailed(String errorMessage);
    }

    // Interfaces for callback when offers are CRUDed
    public interface OnOfferCreatedListener {
        void onOfferCreated(Offer offer);
        void onOfferCreationFailed(String errorMessage);
    }

    public interface OnOffersRetrievedListener {
        void onOffersRetrieved(List<Offer> offers);
    }


    public interface OnOfferUpdatedListener {
        void onOfferUpdated();
        void onOfferUpdateFailed(String errorMessage);
    }

    public interface OnOfferDeletedListener {
        void onOfferDeleted();
        void onOfferDeletionFailed(String errorMessage);
    }
}
