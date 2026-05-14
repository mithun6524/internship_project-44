package com.example.kutirakone.data.local

import androidx.room.*
import com.example.kutirakone.model.FabricScrap
import com.example.kutirakone.model.SwapRequest
import com.example.kutirakone.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("DELETE FROM user_profile")
    fun clearProfile()

    // Fabric Scraps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScraps(scraps: List<FabricScrap>)

    @Query("SELECT * FROM fabric_scraps ORDER BY timestamp DESC")
    fun getAllScraps(): Flow<List<FabricScrap>>

    @Query("SELECT * FROM fabric_scraps WHERE id = :id")
    suspend fun getScrapById(id: String): FabricScrap?

    @Query("DELETE FROM fabric_scraps WHERE id = :id")
    fun deleteScrapById(id: String)

    @Query("DELETE FROM fabric_scraps")
    fun clearScraps()

    // Swap Requests
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSwapRequests(requests: List<SwapRequest>)

    @Query("SELECT * FROM swap_requests ORDER BY timestamp DESC")
    fun getAllSwapRequests(): Flow<List<SwapRequest>>

    @Query("DELETE FROM swap_requests")
    fun clearSwapRequests()
}
