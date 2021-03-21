package com.example.chatapp.fragments

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.*
import kotlin.collections.HashMap


class ProfileFragment: Fragment() {
    lateinit var image_profile: ImageView
    lateinit var username: TextView
   lateinit var email:TextView
    var reference: DatabaseReference? = null
    var fuser: FirebaseUser? = null
    var storageReference: StorageReference? = null
    private val IMAGE_REQUEST = 1
    lateinit var imageUri: Uri
    private var uploadTask: StorageTask<*>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_profile1, container, false)
        image_profile = rootView.findViewById<ImageView>(R.id.profile_image);
        username =rootView.findViewById(R.id.username);
        email=rootView.findViewById(R.id.email)
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fuser = FirebaseAuth.getInstance().currentUser;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid);
        reference!!.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                username.text = user!!.username
                email.text= user.email

                if (user.imageURL == "default")
                {
                    image_profile.setImageResource(R.drawable.ic_placeholder)
                }
                else
                {
                    Glide.with(context!!).load(user.imageURL).circleCrop().into(image_profile)


                }
            }
            override fun onCancelled(@NonNull databaseError: DatabaseError) {
            }
        })
        image_profile.setOnClickListener { openImage() }
        return rootView
    }
    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }
    private fun getFileExtension(uri:Uri):String {
        val contentResolver = context!!.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))!!
    }
    private fun uploadImage() {
        val pd = ProgressDialog(getContext())
        pd.setMessage("Uploading")
        pd.show()
        if (imageUri != null)
        {
            val fileReference = storageReference!!.child((System.currentTimeMillis().toString() + "." + getFileExtension(imageUri)))
            uploadTask = fileReference.putFile(imageUri)
            (uploadTask as UploadTask).continueWithTask(object: Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                @Throws(Exception::class)
                override fun then(@NonNull task:Task<UploadTask.TaskSnapshot>): Task<Uri> {
                    if (!task.isSuccessful)
                    {
                        throw task.exception!!
                    }
                    return fileReference.downloadUrl
                }
            }).addOnCompleteListener(object: OnCompleteListener<Uri> {
                override fun onComplete(@NonNull task:Task<Uri>) {
                    if (task.isSuccessful)
                    {
                        val downloadUri = task.result
                        val mUri = downloadUri.toString()
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
                        val map:HashMap<String, Any> = HashMap()
                        map.put("imageURL", "" + mUri)
                        reference!!.updateChildren(map)
                        pd.dismiss()
                    }
                    else
                    {
                        Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
                        pd.dismiss()
                    }
                }
            }).addOnFailureListener(object: OnFailureListener {
                override fun onFailure(@NonNull e:Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    pd.dismiss()
                }
            })
        }
        else
        {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null){
            imageUri = data.data!!;

            if (uploadTask != null && uploadTask!!.isInProgress){
                Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
}