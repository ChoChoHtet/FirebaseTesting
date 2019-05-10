package com.android.chatapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login_activitky.*
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : AppCompatActivity() {
    private var TAG="Login Activity"
    private var email:String?=null
    private var pwd:String?=null
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activitky)
       supportActionBar!!.title="Login"
        auth=FirebaseAuth.getInstance()

        btn_forgot.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }
        btn_login.setOnClickListener {
            login()
        }

    }

    private fun login() {
        email=login_email.text.toString()
        pwd=login_pwd.text.toString()
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)){
            auth.signInWithEmailAndPassword(email!!,pwd!!)
                .addOnCompleteListener(this){
                    task ->
                    if(task.isSuccessful){
                        updateUI()
                    }else{

                    }
                }
        }else{

        }

    }

    private fun updateUI() {
        val intent=Intent(this@LoginActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
