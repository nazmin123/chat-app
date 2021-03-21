package com.example.chatapp.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.activities.MessageActivity
import com.example.chatapp.model.Chat
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UserAdapter(var items: List<User>,  private var mContext: Context, var ischat:Boolean): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    var theLastMessage: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.user_item,parent,false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
         return items.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user=items[position]
        holder.username.text = user.username
        if (user.imageURL.equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_person);
        } else {
            Glide.with(mContext).load(user.imageURL).circleCrop().into(holder.profile_image);
        }
        if (ischat){
            lastMessage(user.id, holder.last_msg);
        } else {
            holder.last_msg.visibility = View.GONE;
        }
        if (ischat){
            if (user.status.equals("online")){
                holder.img_on.visibility = View.VISIBLE;
                holder.img_off.visibility = View.GONE;
            } else {
                holder.img_on.visibility = View.GONE;
                holder.img_off.visibility = View.VISIBLE;
            }
        } else {
            holder.img_on.visibility = View.GONE;
            holder.img_off.visibility = View.GONE;
        }
      holder.itemView.setOnClickListener {
          val intent = Intent(mContext, MessageActivity::class.java)
          intent.putExtra("userid", user.id)
          mContext.startActivity(intent)
      }
    }
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.username);
        val profile_image = itemView.findViewById<ImageView>(R.id.profile_image);
        val img_on = itemView.findViewById<ImageView>(R.id.img_on);
       val  img_off = itemView.findViewById<ImageView>(R.id.img_off);
        val last_msg = itemView.findViewById<TextView>(R.id.last_msg);

    }
    private fun lastMessage(userid:String, last_msg: TextView) {
        theLastMessage = "default"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children)
                {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (firebaseUser != null && chat != null)
                    {
                        if ((chat.receiver == firebaseUser.uid && chat.sender == userid || chat.receiver == userid && chat.sender == firebaseUser.uid))
                        {
                            theLastMessage = chat.message
                        }
                    }
                }
                when (theLastMessage) {
                    "default" -> last_msg.setText("No Message")
                    else -> last_msg.setText(theLastMessage)
                }
                theLastMessage = "default"
            }
            override fun onCancelled(@NonNull databaseError: DatabaseError) {
            }
        })
    }
}