package com.example.chatapp.model

class Chat {
    lateinit var sender:String
    lateinit var receiver:String
    lateinit var message:String
    var isseen:Boolean = false
    constructor(sender:String, receiver:String, message:String, isseen:Boolean) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.isseen = isseen
    }
    constructor() {}
}