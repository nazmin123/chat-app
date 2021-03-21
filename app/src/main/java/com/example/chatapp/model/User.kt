package com.example.chatapp.model

class User {
    lateinit var id:String
    lateinit var username:String
    lateinit var imageURL:String
    var status: String? = null
    lateinit var search:String
    lateinit var email:String
    constructor(id:String, username:String, imageURL:String, status:String, search:String, email:String) {
        this.id = id
        this.username = username
        this.imageURL = imageURL
        this.status = status
        this.search = search
        this.email=email
    }
    constructor() {
    }
}