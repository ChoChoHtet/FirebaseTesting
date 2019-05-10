package com.android.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login_activitky.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*

class CreateAccountActivity : AppCompatActivity() {
    private val TAG = "Create Account"
    private var mDatabaseRef: DatabaseReference? = null
     private var mDb:FirebaseDatabase?=null
    private var auth: FirebaseAuth? = null
    private var mProgressBar: ProgressDialog? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var pwd: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * to read or write data from firebase database
         */
        mDb= FirebaseDatabase.getInstance()
        mDatabaseRef = mDb!!.reference.child("Users")
        /**
         * initialization firebase authentication
         */
        auth = FirebaseAuth.getInstance()

        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener {
                val token=it.token
                Log.e("Token",token)
            }
            .addOnFailureListener{
                Log.e("TokenNo",it.localizedMessage)
            }
        mProgressBar = ProgressDialog(this)
        //check till user signed in
        val currentUser=auth!!.currentUser
        if(currentUser!=null){
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_create.setOnClickListener {
            createAccount()
        }
        btn_sign_in_with_ph_no.setOnClickListener {
            openPhoneNumberScreen()
        }
        btn_sign_in.setOnClickListener {
            email=ed_email.text.toString().trim()
            pwd=ed_pwd.text.toString()
            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)){
                auth!!.signInWithEmailAndPassword(email!!,pwd!!)
                    .addOnCompleteListener(this){
                            task ->
                        if(task.isSuccessful){
                            val intent= Intent(this,MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }else{
                            Log.d(TAG,"Login Failed ${task.exception}")
                        }
                    }
            }else{
                Log.d(TAG,"Please fill info")
            }

        }
    }
    private fun openPhoneNumberScreen(){
        startActivity(Intent(this@CreateAccountActivity,PhoneNumberActivity::class.java))
    }


    private fun createAccount() {
        firstName = ed_first_name.text.toString()
        lastName = ed_last_name.text.toString()
        email = ed_email.text.toString()
        pwd = ed_pwd.text.toString()

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(
                pwd
            )
        ) {
            mProgressBar!!.setMessage("Register User....")
            mProgressBar!!.show()
            signUpNewUser(email!!, pwd!!,firstName!!,lastName!!)

        } else {
            Toast.makeText(this, "Please fill All Information", Toast.LENGTH_LONG).show()
        }

    }

    private fun signUpNewUser(email: String?, pwd: String?,first:String?,last:String?) {
        auth!!.createUserWithEmailAndPassword(email!!, pwd!!)
            .addOnCompleteListener(this) { task ->
                mProgressBar!!.hide()
                if (task.isSuccessful) {
                    Log.d(TAG, "Create Account Success")
                    /**
                     * get current user
                     */
                    var userId = auth!!.currentUser!!.uid
                    //verify user email
                    verifyEmail()
                    /**
                     * update user info
                     * store real time db
                     */
                    val currentUserDb = mDatabaseRef!!.child(userId)
                    currentUserDb.child("First Name").setValue(first)
                    currentUserDb.child("Last Name").setValue(last)
                    currentUserDb.child("Password").setValue(pwd)
                    updateUI()

                } else {
                    /**
                     * If sign in fail ,display message to user
                     */
                    Log.e(TAG,task.exception.toString())
                    Toast.makeText(this, "Authentication Failed ${task.exception}", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun updateUI() {
        val intent= Intent(this,LoginActivity::class.java)
        //clear CreateAccountActivity fro stack so that if user press back from Login Activity ,it should not taken back to CreateAccount Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }

    /**
     * verify user email
     */
    private fun verifyEmail() {
        val user = auth!!.currentUser!!
        user!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Send Verification Email to ${user.email}")
                    Toast.makeText(this, "Send Verification Email to ${user.email}", Toast.LENGTH_LONG).show()

                } else {
                    Log.d(TAG, "Send Verification Email to ${user.email} Failed")
                    Toast.makeText(this, "Send Verification Email to ${user.email} Failed", Toast.LENGTH_LONG).show()

                }

            }
    }
}
