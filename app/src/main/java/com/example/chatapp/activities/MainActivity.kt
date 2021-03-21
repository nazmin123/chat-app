package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.fragments.ChatsFragment
import com.example.chatapp.fragments.ProfileFragment
import com.example.chatapp.fragments.UsersFragment
import com.example.chatapp.model.Chat
import com.example.chatapp.model.User
import com.example.chatapp.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
        supportActionBar!!.title = ""
        firebaseUser = FirebaseAuth.getInstance().currentUser;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        //reference.addValueEventListener()
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val user =
                    dataSnapshot.getValue(
                        User::class.java
                    )!!
                username.text = user.username
                if (user.imageURL == "default") {
                    profile_image.setImageResource(R.drawable.ic_person)
                } else {

                    //change this
                    Glide.with(applicationContext).load(user.imageURL).circleCrop().into(
                        profile_image
                    )
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var unread = 0
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.receiver == firebaseUser!!.uid && !chat.isseen) {
                        unread++
                    }
                }
                if (unread == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
                } else {
                    viewPagerAdapter.addFragment(ChatsFragment(), "(" + unread + ") Chats")
                }
                viewPagerAdapter.addFragment(UsersFragment(), "Users")
                viewPagerAdapter.addFragment(ProfileFragment(), "Profile")
                view_pager.adapter = viewPagerAdapter
                tab_layout.setupWithViewPager(view_pager)
            }
        })
        FirebaseInstanceId.getInstance().token?.let { updateToken(it) };
    }
    private fun updateToken(token: String) {
        val tokenRef = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        tokenRef.child(firebaseUser!!.uid).setValue(token1)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                // change this code beacuse your app will crash
                startActivity(
                    Intent(
                        this@MainActivity,
                        SplashScreenActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                return true
            }
        }
        return false
    }
    internal class ViewPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
        private val fragments: ArrayList<Fragment> = ArrayList()
        private val titles: ArrayList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        // Ctrl + O
        @Nullable
        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }
        private fun status(status: String) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
            val hashMap: HashMap<String, String> = HashMap()
            hashMap.put("status", status)
            reference!!.updateChildren(hashMap as Map<String, Any>)
        }
         override fun onResume() {
            super.onResume()
            status("online")
        }
         override fun onPause() {
            super.onPause()
            status("offline")
        }
    }
