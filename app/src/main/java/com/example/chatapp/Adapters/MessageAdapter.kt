package com.example.chatapp.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MessageAdapter(var mChat: List<Chat>, private var mContext: Activity,var imageurl:String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1
    var fuser: FirebaseUser? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageAdapter.MessageViewHolder {
        return if (viewType ==MSG_TYPE_RIGHT) {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false)
            MessageViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false)
            MessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChat.size;
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int) {

        val chat = mChat[position]

        holder.show_message.text = chat.message

        if (imageurl.equals("default")) {
            holder.profile_image.setImageResource(R.drawable.ic_person)
        } else {
            Glide.with(mContext).load(imageurl).circleCrop().into(holder.profile_image)
        }

        if (position == mChat.size - 1) {
            if (chat.isseen) {
                holder.txt_seen.text = "Seen"
            } else {
                holder.txt_seen.text = "Delivered"
            }
        } else {
            holder.txt_seen.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        fuser = FirebaseAuth.getInstance().currentUser;
        if (mChat[position].sender.equals(fuser!!.uid)){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       val show_message = itemView.findViewById<TextView>(R.id.show_message);
       val profile_image = itemView.findViewById<ImageView>(R.id.profile_image);
       val txt_seen = itemView.findViewById<TextView>(R.id.txt_seen);
    }

}