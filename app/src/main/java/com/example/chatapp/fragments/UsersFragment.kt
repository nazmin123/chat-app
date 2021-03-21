package com.example.chatapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.UserAdapter
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UsersFragment:Fragment() {
    lateinit var recyclerView: RecyclerView

    lateinit var userAdapter: UserAdapter
    var mUser: ArrayList<User> = ArrayList()
    lateinit var search_users: EditText
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_users, container, false)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(mUser,activity!!,false)
        recyclerView.adapter = userAdapter
        mUser = ArrayList()
        readUsers()
        search_users = rootView.findViewById(R.id.search_users)
        search_users.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                searchUsers(charSequence.toString().toLowerCase())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        return rootView
    }
    private fun searchUsers(s:String) {
        val fuser = FirebaseAuth.getInstance().currentUser
        val query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
            .startAt(s)
            .endAt(s + "\uf8ff")
        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
               mUser.clear()
                for (snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    assert(user != null)
                    assert(fuser != null)
                    if (!user!!.id.equals(fuser!!.uid))
                    {
                        mUser.add(user)
                    }
                }
                userAdapter = UserAdapter(mUser,activity!!,false)
                recyclerView.adapter = userAdapter
                userAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(@NonNull databaseError: DatabaseError) {
            }
        })

    }
    private fun readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot:DataSnapshot) {
                if (search_users.text.toString() == "")
                {
                    mUser.clear()
                    for (snapshot in dataSnapshot.children)
                    {
                        val user = snapshot.getValue(User::class.java)
                        if (user!!.id != firebaseUser!!.uid)
                        {
                            if(user.id.isNotEmpty())
                            mUser.add(user)
                        }
                        userAdapter.notifyDataSetChanged()
                    }

                }
                recyclerView
                    .layoutManager = LinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL,false)
                recyclerView.itemAnimator = DefaultItemAnimator()
                userAdapter = UserAdapter(mUser,context!!,false)
                recyclerView.adapter = userAdapter

            }
            override fun onCancelled(@NonNull databaseError:DatabaseError) {
            }
        })
    }
}