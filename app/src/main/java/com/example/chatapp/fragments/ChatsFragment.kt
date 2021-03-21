package com.example.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.UserAdapter
import com.example.chatapp.R
import com.example.chatapp.model.Chatlist
import com.example.chatapp.model.User
import com.example.chatapp.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_chats.*


class ChatsFragment:Fragment() {
    lateinit var recyclerView:RecyclerView
    lateinit var fuser: FirebaseUser
    lateinit var reference: DatabaseReference

    lateinit var userAdapter: UserAdapter
    var mUsers: ArrayList<User> = ArrayList()
    var usersList: ArrayList<Chatlist> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_chats, container, false)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(mUsers, activity!!, true)
        recyclerView.adapter = userAdapter
        fuser = FirebaseAuth.getInstance().currentUser!!
        usersList = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.uid)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot:DataSnapshot) {
                usersList.clear()
                for (snapshot in dataSnapshot.children)
                {
                    val chatlist = snapshot.getValue(Chatlist::class.java)
                    usersList.add(chatlist!!)
                }
                chatList()
            }
            override fun onCancelled(@NonNull databaseError:DatabaseError) {
            }
        })
        updateToken(FirebaseInstanceId.getInstance().token)
        return rootView
    }
    private fun updateToken(token:String?) {
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(fuser.uid).setValue(token1)
    }
    private fun chatList() {
        mUsers = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                mUsers.clear()
                for (snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    for (chatlist in usersList)
                    {
                        if (user!!.id == chatlist.id)
                        {
                            if(user.id.isNotEmpty())
                            mUsers.add(user)
                        }
                    }

                }
                recyclerView
                    .layoutManager = LinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL,false)
                recyclerView.itemAnimator = DefaultItemAnimator()
                userAdapter = UserAdapter(mUsers, context!!, true)
                recyclerView.adapter = userAdapter
                userAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(@NonNull databaseError: DatabaseError) {
            }
        })
    }
}