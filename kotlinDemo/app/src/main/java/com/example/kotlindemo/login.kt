package com.example.kotlindemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn = findViewById<Button>(R.id.loginn)
        val email = findViewById<EditText>(R.id.loginEmail)
        val password = findViewById<EditText>(R.id.loginPsd)

        loginBtn.setOnClickListener(){
            readData()
        }
    }

    fun readData(){
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnCompleteListener(){

            if (it.isSuccessful){
                for(document in it.result!!){
                    val finMail = document.data.getValue("email")
                    val finPsd = document.data.getValue("password")
                    val email = findViewById<EditText>(R.id.loginEmail)
                    val password = findViewById<EditText>(R.id.loginPsd)

                    if((email.text.toString().equals(finMail))&&(password.text.toString().equals(finPsd))){
                        intent = Intent(applicationContext, MainScreen::class.java)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(applicationContext, "Wrong credentials provided", Toast.LENGTH_SHORT)
                    }

                }
            }

        }
    }
}