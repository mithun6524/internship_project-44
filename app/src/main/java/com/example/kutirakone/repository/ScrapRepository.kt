package com.example.kutirakone.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.kutirakone.data.local.AppDao
import com.example.kutirakone.model.FabricScrap
import com.example.kutirakone.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class ScrapRepository(
    private val context: Context? = null,
    private val appDao: AppDao? = null
) {

    private val db = FirebaseFirestore.getInstance()
    // Explicitly using the bucket from google-services.json to be safe
    private val storage = FirebaseStorage.getInstance("gs://kutira-kone-a6ff0.firebasestorage.app").reference
    private val auth = FirebaseAuth.getInstance()

    // Local Data
    val allScrapsLocal: Flow<List<FabricScrap>>? = appDao?.getAllScraps()
    val userProfileLocal: Flow<UserProfile?>? = appDao?.getProfile()

    suspend fun saveProfileLocal(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            appDao?.insertProfile(profile)
        }
    }

    suspend fun saveScrapsLocal(scraps: List<FabricScrap>) {
        withContext(Dispatchers.IO) {
            appDao?.insertScraps(scraps)
        }
    }

    suspend fun deleteScrapLocal(scrapId: String) {
        withContext(Dispatchers.IO) {
            appDao?.deleteScrapById(scrapId)
        }
    }

    fun uploadScrap(
        imageUri: Uri?,
        imageBitmap: android.graphics.Bitmap?,
        material: String,
        size: Double,
        price: Double,
        color: String,
        lat: Double,
        lng: Double,
        address: String,
        phone: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.uid
        if (uid == null) {
            onResult(false, "User not logged in")
            return
        }

        val id = UUID.randomUUID().toString()
        val imageRef = storage.child("scraps/${System.currentTimeMillis()}.jpg")

        Log.d("UPLOAD", "Preparing image for upload...")

        // Compress and get bytes
        val bytes = try {
            val bitmap = when {
                imageUri != null -> {
                    context?.contentResolver?.openInputStream(imageUri)?.use { 
                        android.graphics.BitmapFactory.decodeStream(it)
                    }
                }
                imageBitmap != null -> imageBitmap
                else -> null
            }
            
            if (bitmap == null) {
                onResult(false, "Failed to load image")
                return
            }

            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            Log.e("UPLOAD", "Image processing failed", e)
            onResult(false, "Image error: ${e.message}")
            return
        }

        Log.d("UPLOAD", "Uploading ${bytes.size} bytes...")

        // Using simple putBytes and then getting downloadUrl on success
        imageRef.putBytes(bytes)
            .addOnSuccessListener { taskSnapshot ->
                Log.d("UPLOAD", "Upload successful, getting URL...")
                imageRef.downloadUrl
                    .addOnSuccessListener { url ->
                        Log.d("UPLOAD", "URL fetched: $url")
                        val scrap = FabricScrap(
                            id = id,
                            ownerUid = uid,
                            material = material,
                            sizeMetres = size,
                            price = price,
                            color = color,
                            photoUrl = url.toString(),
                            lat = lat,
                            lng = lng,
                            address = address,
                            phone = phone,
                            timestamp = System.currentTimeMillis()
                        )

                        db.collection("scraps")
                            .document(id)
                            .set(scrap)
                            .addOnSuccessListener {
                                Log.d("UPLOAD", "Firestore success")
                                onResult(true, null)
                            }
                            .addOnFailureListener {
                                Log.e("UPLOAD", "Firestore error", it)
                                onResult(false, "Firestore Error: ${it.message}")
                            }
                    }
                    .addOnFailureListener {
                        Log.e("UPLOAD", "URL fetch failed", it)
                        onResult(false, "URL Error: ${it.message}")
                    }
            }
            .addOnFailureListener {
                Log.e("UPLOAD", "Upload failed", it)
                onResult(false, "Upload Error: ${it.message}")
            }
    }

    fun deleteScrap(scrapId: String, photoUrl: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("scraps").document(scrapId)
            .delete()
            .addOnSuccessListener {
                // Also try to delete the image from storage if possible
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
                    storageRef.delete()
                } catch (e: Exception) {
                    Log.e("DELETE", "Storage delete failed", e)
                }
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Failed to delete")
            }
    }

    fun listenScraps(onUpdate: (List<FabricScrap>) -> Unit) {
        db.collection("scraps")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                val list = value?.toObjects(FabricScrap::class.java) ?: emptyList()
                onUpdate(list)
            }
    }

    fun saveProfile(name: String, phone: String, onDone: (Boolean) -> Unit) {
        val uid = auth.uid ?: return
        val email = auth.currentUser?.email ?: ""
        val profile = UserProfile(uid, name, phone, email, "")
        
        db.collection("users").document(uid)
            .set(profile)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun getUserProfile(uid: String, onSuccess: (UserProfile?) -> Unit, onError: (String) -> Unit) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                onSuccess(profile)
            }
            .addOnFailureListener { 
                onError(it.message ?: "Failed to fetch profile")
            }
    }

    fun uploadProfilePhoto(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val uid = auth.uid
        if (uid == null) {
            onError("User not logged in")
            return
        }
        val ref = storage.child("profiles/$uid.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { url ->
                    onSuccess(url.toString())
                }?.addOnFailureListener {
                    onError("Failed to get download URL: ${it.message}")
                }
            }
            .addOnFailureListener {
                onError("Upload failed: ${it.message}")
            }
    }
}
