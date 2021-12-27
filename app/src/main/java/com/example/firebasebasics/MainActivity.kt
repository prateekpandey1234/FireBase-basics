package com.example.firebasebasics

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var auth:FirebaseAuth//it is the reference to firebase authentication which is a singleton which holds everything we need
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth= FirebaseAuth.getInstance()
        auth.signOut()//it allows to log out the current user from the project
        btnRegister.setOnClickListener {
            registerUser()
        }
        btnLogin.setOnClickListener {
            loginUser()
        }
        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
        // but we need to enable email/password authentication from our firebase console or the app will not run
    }


    override fun onStart() {//here we can see if the user is logged in or not
                            //because even we restart our app the firebase holds the logged in state hence we can
                            //make ui to respond to that..this doesn't mean that user is logged in forever
        super.onStart()
        checkLoggedInState()
    }
    private fun registerUser() {
        val email = etEmailRegister.text.toString()
        val password = etPasswordRegister.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {//it i s an networking function hence we use IO thread to do function
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()//we will only continue if the login is successful hence use await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {//here we will show error to user through UI so we change the thread to Main
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    private fun checkLoggedInState() {
        val user = auth.currentUser
        if (auth.currentUser == null) { // not logged in
            tvLoggedIn.text = "You are not logged in"
        } else {
            tvLoggedIn.text = "You are logged in!"
            ivProfilePicture.setImageURI(user?.photoUrl)
        }
    }
    private fun loginUser() {
        //firebase console stores the user mail and user id that was used to register
        //we can access them on the console
        val email = etEmailLogin.text.toString()
        val password = etPasswordLogin.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    private fun updateProfile() {
        val user = auth.currentUser
        user?.let { user ->
            val username = etUsername.text.toString()
            val photoURI = Uri.parse("android.resource://$packageName/${R.mipmap.image_foreground}")//it is for the profile photo of the
                                                                            //user we need to update
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(photoURI)
                .build()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateProfile(profileUpdates).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                        Toast.makeText(this@MainActivity, "Successfully updated profile",
                            Toast.LENGTH_LONG).show()
                    }
                } catch(e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    }
}