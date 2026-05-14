package com.example.kutirakone.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kutirakone.model.FabricScrap
import com.example.kutirakone.model.UserProfile
import com.example.kutirakone.repository.ScrapRepository
import com.example.kutirakone.utils.calculateDistance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database by lazy { com.example.kutirakone.data.local.AppDatabase.getDatabase(application) }
    private val appDao by lazy { database.appDao() }
    private val repository by lazy { ScrapRepository(application, appDao) }
    private val auth = FirebaseAuth.getInstance()
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _currentUser = mutableStateOf(auth.currentUser)
    val currentUser: State<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    var scraps by mutableStateOf<List<FabricScrap>>(emptyList())
        private set
        
    var filteredScraps by mutableStateOf<List<FabricScrap>>(emptyList())
        private set

    var acceptedSwapScrapIds by mutableStateOf<Set<String>>(emptySet())
        private set
    
    var selectedMaterial by mutableStateOf("All")
        private set
    var radiusLimit by mutableStateOf(10.0)
        private set
    var showNearbyOnly by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf("")
        private set

    var userLat by mutableStateOf(0.0)
        private set
    var userLng by mutableStateOf(0.0)
        private set

    private var localScrapCollectorStarted = false
    private var localProfileCollectorStarted = false
    private var swapListenersStarted = false
    private var filterJob: Job? = null

    private fun startLocalScrapCollector() {
        if (localScrapCollectorStarted) return
        localScrapCollectorStarted = true
        viewModelScope.launch {
            repository.allScrapsLocal?.collect { localScraps ->
                if (scraps.isEmpty() && localScraps.isNotEmpty()) {
                    scraps = localScraps
                    applyRadius(userLat, userLng, radiusLimit)
                }
            }
        }
    }

    private fun startLocalProfileCollector() {
        if (localProfileCollectorStarted) return
        localProfileCollectorStarted = true
        viewModelScope.launch {
            repository.userProfileLocal?.collect { localProfile ->
                if (_userProfile.value == null) {
                    _userProfile.value = localProfile
                }
            }
        }
    }

    private fun startSwapListeners() {
        if (swapListenersStarted || auth.currentUser == null) return
        swapListenersStarted = true
        listenForSwaps()
        listenForAcceptedSwaps()
    }

    var pendingSwapsCount by mutableStateOf(0)
        private set

    fun listenForSwaps() {
        val uid = auth.uid ?: return
        db.collection("swaps")
            .whereEqualTo("toUser", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { value, _ ->
                pendingSwapsCount = value?.size() ?: 0
            }
    }

    fun listenForAcceptedSwaps() {
        db.collection("swaps")
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { value, _ ->
                acceptedSwapScrapIds = value?.documents
                    ?.mapNotNull { it.getString("scrapId") }
                    ?.toSet()
                    ?: emptySet()
                applyRadius(userLat, userLng, radiusLimit)
            }
    }

    private fun availableScraps(): List<FabricScrap> {
        return scraps.filterNot { it.id in acceptedSwapScrapIds }
    }

    fun updateSelectedMaterial(material: String) {
        selectedMaterial = material
        applyRadius(userLat, userLng, radiusLimit)
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        applyRadius(userLat, userLng, radiusLimit)
    }

    fun updateShowNearbyOnly(enabled: Boolean) {
        showNearbyOnly = enabled
        applyRadius(userLat, userLng, radiusLimit)
    }

    fun loadUserProfile() {
        startLocalProfileCollector()
        auth.currentUser?.uid?.let { uid ->
            repository.getUserProfile(uid, { profile ->
                _userProfile.value = profile
                profile?.let {
                    viewModelScope.launch { repository.saveProfileLocal(it) }
                }
            }, {
                // Handle error
            })
        }
    }

    fun saveProfile(name: String, phone: String, photoUrl: String = "", onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val userId = auth.uid ?: return
        val email = auth.currentUser?.email ?: ""
        val profile = UserProfile(userId, name, phone, email, photoUrl)
        
        db.collection("users")
            .document(userId)
            .set(profile)
            .addOnSuccessListener { 
                _userProfile.value = profile
                viewModelScope.launch { repository.saveProfileLocal(profile) }
                onSuccess() 
            }
            .addOnFailureListener { onError(it.message ?: "Failed to save profile") }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
                loadUserProfile()
                startSwapListeners()
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Login failed") }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Could not send reset email") }
    }

    fun signUp(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Sign up failed") }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        acceptedSwapScrapIds = emptySet()
    }

    fun loadScraps() {
        startLocalScrapCollector()
        startSwapListeners()
        db.collection("scraps")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                viewModelScope.launch(Dispatchers.Default) {
                    val list = value?.toObjects(FabricScrap::class.java) ?: emptyList()
                    // Save to local DB
                    repository.saveScrapsLocal(list)

                    withContext(Dispatchers.Main) {
                        scraps = list
                        applyRadius(userLat, userLng, radiusLimit)
                    }
                }
            }
    }

    fun applyRadius(lat: Double, lng: Double, radius: Double) {
        userLat = lat
        userLng = lng
        radiusLimit = radius

        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            val currentUid = auth.uid
            val searchTerms = searchQuery
                .trim()
                .lowercase()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }

            val filtered = availableScraps().filter { scrap ->
                val isOwnScrap = scrap.ownerUid == currentUid
                val hasScrapLocation = scrap.lat != 0.0 || scrap.lng != 0.0
                val matchesRadius = if (userLat == 0.0 && userLng == 0.0) {
                    true
                } else if (!hasScrapLocation) {
                    isOwnScrap
                } else {
                    calculateDistance(userLat, userLng, scrap.lat, scrap.lng) <= radius
                }
                val matchesMaterial = selectedMaterial == "All" || scrap.material == selectedMaterial
                val matchesSearch = searchTerms.isEmpty() || searchTerms.all { term ->
                    scrap.matchesSearchTerm(term)
                }
                val matchesLocation = !showNearbyOnly || isOwnScrap || matchesRadius
                matchesLocation && matchesMaterial && matchesSearch
            }.sortedWith(
                compareByDescending<FabricScrap> { it.ownerUid == currentUid }
                    .thenByDescending { it.timestamp }
            )
            withContext(Dispatchers.Main) {
                filteredScraps = filtered
            }
        }
    }

    fun refreshScrapsAfterUpload() {
        loadScraps()
        applyRadius(userLat, userLng, radiusLimit)
    }

    fun sendSwapRequest(scrap: FabricScrap, onSuccess: () -> Unit = {}) {
        val currentUserId = auth.uid ?: return
        if (scrap.ownerUid == currentUserId) return // Can't swap with self
        if (scrap.id in acceptedSwapScrapIds) return

        val swap = hashMapOf(
            "scrapId" to scrap.id,
            "scrapMaterial" to scrap.material,
            "scrapColor" to scrap.color,
            "scrapPhotoUrl" to scrap.photoUrl,
            "fromUser" to currentUserId,
            "toUser" to scrap.ownerUid,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("swaps").add(swap).addOnSuccessListener { onSuccess() }
    }

    fun acceptSwap(swapId: String, onSuccess: () -> Unit = {}) {
        val swapRef = db.collection("swaps").document(swapId)
        swapRef.get()
            .addOnSuccessListener { document ->
                val scrapId = document.getString("scrapId")
                if (scrapId.isNullOrBlank()) {
                    swapRef.update("status", "accepted").addOnSuccessListener { onSuccess() }
                    return@addOnSuccessListener
                }

                db.collection("swaps")
                    .whereEqualTo("scrapId", scrapId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { pendingSwaps ->
                        val batch: WriteBatch = db.batch()
                        pendingSwaps.documents.forEach { pending ->
                            batch.update(
                                pending.reference,
                                "status",
                                if (pending.id == swapId) "accepted" else "rejected"
                            )
                        }
                        if (pendingSwaps.documents.none { it.id == swapId }) {
                            batch.update(swapRef, "status", "accepted")
                        }
                        batch.commit().addOnSuccessListener { onSuccess() }
                    }
            }
    }

    fun rejectSwap(swapId: String, onSuccess: () -> Unit = {}) {
        db.collection("swaps").document(swapId)
            .update("status", "rejected")
            .addOnSuccessListener { onSuccess() }
    }

    fun requestSwap(scrap: FabricScrap) {
        sendSwapRequest(scrap)
    }

    fun uploadScrap(
        imageUri: android.net.Uri?,
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
        repository.uploadScrap(imageUri, imageBitmap, material, size, price, color, lat, lng, address, phone, onResult)
    }

    fun uploadProfilePhoto(imageUri: android.net.Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        repository.uploadProfilePhoto(imageUri, onSuccess, onError)
    }

    fun deleteScrap(scrapId: String, photoUrl: String, onResult: (Boolean, String?) -> Unit) {
        repository.deleteScrap(scrapId, photoUrl) { success, msg ->
            if (success) {
                scraps = scraps.filterNot { it.id == scrapId }
                filteredScraps = filteredScraps.filterNot { it.id == scrapId }
                viewModelScope.launch {
                    repository.deleteScrapLocal(scrapId)
                }
            }
            onResult(success, msg)
        }
    }
}

private fun FabricScrap.matchesSearchTerm(term: String): Boolean {
    val searchableText = listOf(
        material,
        color,
        address,
        phone,
        sizeMetres.searchableNumberText("m"),
        price.searchableNumberText("rs")
    ).joinToString(" ").lowercase()

    if (searchableText.contains(term)) return true

    val numericTerm = term.toDoubleOrNull() ?: return false
    return price == numericTerm ||
        sizeMetres == numericTerm ||
        price.toInt().toString() == term ||
        sizeMetres.toInt().toString() == term
}

private fun Double.searchableNumberText(prefixOrSuffix: String): String {
    val wholeNumber = if (this % 1.0 == 0.0) toInt().toString() else toString()
    return "$this $wholeNumber $prefixOrSuffix$this $prefixOrSuffix$wholeNumber"
}
