package com.earthlyapps.hervault.viewmodels

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavHostController
import com.earthlyapps.hervault.models.User
import com.earthlyapps.hervault.utilities.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.BuildConfig
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    var navHostController: NavHostController,
    var context: Context
) {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val progress: ProgressDialog = ProgressDialog(context).apply {
        setTitle("Loading")
        setMessage("Please wait...")
    }
    private val sessionManager = SessionManager(context)
    private val database = Firebase.database("https://hervault-620d7-default-rtdb.europe-west1.firebasedatabase.app/").apply {
        if (BuildConfig.DEBUG) {
            reference.child(".info/connected").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FIREBASE_CONN", "Connected: ${snapshot.getValue(Boolean::class.java)}")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FIREBASE_CONN", "Connection error", error.toException())
                }
            })
        }
    }

    // Helper Extensions
    private fun navigateTo(route: String, clearBackstack: Boolean = false) {
        navHostController.navigate(route) {
            if (clearBackstack) {
                popUpTo(0) // Clears entire backstack
            }
        }
    }

    private fun showToast(message: String?) {
        val text = message ?: "An unknown error occurred"
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun generateAndSavePartnerCode(): String {
        // Get current user from session
        val currentUser = sessionManager.getUser() ?: run {
            Log.e("PARTNER_CODE", "No logged-in user")
            return ""
        }

        return try {
            val allowedChars = ('A'..'Z') + ('0'..'9') + ('a'..'z')
            val code =  (1..6)
                .map { allowedChars.random() }
                .joinToString("")
            Log.d("PARTNER_CODE", "Generated code: $code")

            database.reference
                .child("Users")
                .child(currentUser.uid)
                .child("partnerCode")
                .setValue(code)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("PARTNER_CODE", "Code saved for ${currentUser.uid}")
                    } else {
                        Log.e("PARTNER_CODE", "Firebase save failed", task.exception)
                    }
                }

            sessionManager.saveUser(
                currentUser.copy(partnerCode = code)
            )

            code
        }
        catch (e: Exception) {
            Log.e("PARTNER_CODE", "Code generation failed", e)
            ""
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    // Women register (ONLY WOMEN CAN CREATE ACCOUNTS DIRECTLY)
    fun registerAsFemale(name: String, email: String, password: String) {
        progress.show()
        Log.d("REGISTER", "Starting female registration")

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    progress.dismiss()
                    Log.e("AUTH_DEBUG", "Auth failed", authTask.exception)
                    showToast("Registration failed: ${authTask.exception?.message ?: "Unknown error"}")
                    return@addOnCompleteListener
                }

                val user = authTask.result?.user ?: run {
                    progress.dismiss()
                    Log.e("AUTH_DEBUG", "User object is null")
                    showToast("User creation error")
                    return@addOnCompleteListener
                }

                val firebaseUser = User(
                    uid = user.uid,
                    name = name,
                    email = email,
                    role = "female"
                )

                try {
                    database.reference.child("Users/${user.uid}")
                        .setValue(firebaseUser)
                        .addOnCompleteListener { writeTask ->
                            progress.dismiss()

                            if (writeTask.isSuccessful) {
                                sessionManager.saveUser(firebaseUser)
                                Log.d("DB_WRITE", "User saved successfully")
                                navigateTo("generate-code", clearBackstack = true)
                            } else {
                                val errorMessage =
                                    writeTask.exception?.message ?: "Database write failed"
                                Log.e("DB_WRITE", errorMessage, writeTask.exception)
                                showToast(errorMessage)
                            }
                        }
                }catch(it: Exception) {
                    progress.dismiss()
                    Log.e("REGISTER_USER", "Error: ${it.message}")
                    showToast("Error: ${it.message}")
                }
            }
    }

    // Men registration (REQUIRES PARTNER CODE)
    fun registerAsMan(name: String, email: String, password: String, partnerCode: String) {
        if (!isNetworkAvailable()) {
            showToast("No internet connection")
            return
        }

        progress.show()
        Log.d("REGISTER", "Starting male registration with code: $partnerCode")

        // 1. Query for EXACT female partner code match
        database.reference.child("Users")
            .orderByChild("partnerCode")
            .equalTo(partnerCode)
            .get()
            .addOnCompleteListener { codeTask ->
                if (!codeTask.isSuccessful) {
                    progress.dismiss()
                    Log.e("NETWORK_ERROR", "Partner code query failed", codeTask.exception)
                    showToast("Network error. Try again.")
                    return@addOnCompleteListener
                }

                val snapshot = codeTask.result
                Log.d("PARTNER_QUERY", "Found ${snapshot.childrenCount} matches")

                // Temporary debug - print all matching users
                snapshot.children.forEach {
                    Log.d("PARTNER_DEBUG", "User: ${it.key} - ${it.getValue(User::class.java)}")
                }

                // 2. Verify exactly one female user has this code
                val femaleUsers = snapshot.children.mapNotNull {
                    it.getValue(User::class.java)
                }.filter { it.role == "female" }

                if (femaleUsers.size != 1) {
                    progress.dismiss()
                    Log.e("PARTNER_CODE", "Invalid matches: ${femaleUsers.size}")
                    showToast("Invalid partner code")
                    return@addOnCompleteListener
                }

                val partnerUser = femaleUsers.first()
                Log.d("PARTNER_FOUND", "Valid female partner: ${partnerUser.uid}")

                // 2. Create male user account
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { authTask ->
                        if (!authTask.isSuccessful) {
                            progress.dismiss()
                            showToast("Registration failed: ${authTask.exception?.message}")
                            return@addOnCompleteListener
                        }

                        val maleUser = authTask.result?.user ?: run {
                            progress.dismiss()
                            showToast("Account creation error")
                            return@addOnCompleteListener
                        }

                        // 3. Create user data with bidirectional link
                        val newMaleUser = User(
                            uid = maleUser.uid,
                            name = name,
                            email = email,
                            role = "male",
                            linkedPartnerId = partnerUser.uid  // Link to female
                        )

                        // 4. Atomic update of both records
                        val updates = hashMapOf<String, Any>(
                            "Users/${maleUser.uid}" to newMaleUser,
                            "Users/${partnerUser.uid}/linkedPartnerId" to maleUser.uid
                        )

                        database.reference.updateChildren(updates)
                            .addOnCompleteListener { updateTask ->
                                progress.dismiss()

                                if (updateTask.isSuccessful) {
                                    sessionManager.saveUser(newMaleUser)
                                    navigateTo("men-dashboard", true)
                                } else {
                                    showToast("Registration incomplete - please relink later")
                                }
                            }
                    }
            }
    }

    // Login (UNISEX)
    suspend fun login(email: String, password: String) {
        try {
            // Show loading immediately
            withContext(Dispatchers.Main) {
                progress.show()
            }

            // Authenticate with Firebase Auth
            val authResult = withContext(Dispatchers.IO) {
                Firebase.auth.signInWithEmailAndPassword(email, password).await()
            }

            val userId = authResult.user?.uid ?: throw Exception("Authentication succeeded but user ID is null")

            // Fetch user data from Realtime Database
            val userSnapshot = withContext(Dispatchers.IO) {
                database.reference.child("Users/$userId").get().await()
            }

            val user = userSnapshot.getValue(User::class.java)
                ?: throw Exception("User data not found in database")

            // Save to session and navigate
            withContext(Dispatchers.Main) {
                sessionManager.saveUser(user)
                progress.dismiss()

                val destination = when (user.role.lowercase()) {
                    "female" -> "ladies-dashboard"
                    "male" -> "men-dashboard"
                    else -> throw Exception("Invalid user role: ${user.role}")
                }

                navigateTo(destination, clearBackstack = true)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                progress.dismiss()
                showToast("Login failed: ${e.message}")
                Log.e("LOGIN_ERROR", "Login failed", e)
            }
        }
    }

    fun isCurrentUserFemale(callback: (Boolean) -> Unit) {
        val currentUser = sessionManager.getUser()

        if (currentUser != null && currentUser.role == "female"){
            callback(true)
            return
        }
        else{
            callback(false)
        }
    }

    fun isLoggedIn(): Boolean = mAuth.currentUser != null

    fun logout() {
        sessionManager.clearSession()
        mAuth.signOut()
        navHostController.navigate("login")
    }

    fun getCurrentUser(): User? {
        return sessionManager.getUser()
    }
}