package com.android.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        val IMG_REQ=1
    }
    private lateinit var dbRef:DatabaseReference
    private lateinit var auth:FirebaseAuth
    private lateinit var storageRef:StorageReference
    private lateinit var progressBar:ProgressDialog
    private var path:Uri?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        supportActionBar!!.title="Main Activity"
        dbRef=FirebaseDatabase.getInstance().reference.child("Users")
        /**
         * create storage reference
         */
        storageRef=FirebaseStorage.getInstance().reference
        auth=FirebaseAuth.getInstance()
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener {
                val token=it.token
             Log.e("AA",token)
                tv_username.text=token
            }
            .addOnFailureListener{
                Log.e("TokenNo",it.localizedMessage)
            }
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(this,"Hello",Toast.LENGTH_SHORT).show()
        //current user
        val user=auth.currentUser
        //read user table
        val userRef=dbRef.child(user!!.uid)
        //user email
        tv_email.text=user!!.email
        //read data from database
        userRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapShot: DataSnapshot) {
                tv_username!!.text= "${dataSnapShot.child("First Name").value } ${dataSnapShot.child("Last Name").value}" as String
            }
        })

        btn_sign_out.setOnClickListener {
            auth.signOut()
            finish()
        }
        btn_choose_file.setOnClickListener {
            chooseFile()
        }
        btn_upload_file.setOnClickListener {
            uploadFile()
        }
    }


    private fun uploadFile() {
        if(path !=null){
            //storage image name by random number
            val imgREf=storageRef.child("images/${UUID.randomUUID()}")
            progressBar= ProgressDialog(this)
            progressBar.setTitle("Uploading...")
            progressBar.show()
            imgREf.putFile(path!!)
                .addOnSuccessListener{
                    progressBar.hide()
                    Toast.makeText(this@MainActivity,"Upload Success",Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener{
                    progressBar.hide()
                    Log.e("UploadFile",it.localizedMessage)
                    Toast.makeText(this@MainActivity,"Upload failed ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener {taskSnapshot ->
                    val progress=100.0*taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
                    progressBar.setMessage("Uploaded ${progress.toInt()}%...")
                }
        }

    }

    private fun chooseFile() {
        val intent=Intent()
        intent.type="image/*"
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), IMG_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode!= IMG_REQ && resultCode != Activity.RESULT_OK && data==null && data?.data ==null)
            Log.d("ChooseFile","Failed")
        else {
            /**
             * get file path
             */
           path=data!!.data
            /**
             * convert bitmap
             */
            val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,path)
            img.setImageBitmap(bitmap)
        }
    }
}
