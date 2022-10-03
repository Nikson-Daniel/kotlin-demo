package com.example.kotlindemo

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        val signUpBtn = findViewById<Button>(R.id.signup)
        val loginNavigation = findViewById<TextView>(R.id.loginNavigation)


        signUpBtn.setOnClickListener(){

            storeDate()
        }

        textView.setOnClickListener(){
            demo()
        }
        loginNavigation.setOnClickListener(){

            intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
        }
    }

    fun demo(){
        val myTst = Toast.makeText(applicationContext, "Hello", Toast.LENGTH_SHORT)
        myTst.setGravity(Gravity.LEFT, 200,200)
        myTst.show()
    }

    fun storeDate(){

        val email = findViewById<EditText>(R.id.signUpMail)
        val createPassword = findViewById<EditText>(R.id.cretePsd)
        val confPsd = findViewById<EditText>(R.id.confPsd)

        if (createPassword.text.toString().equals(confPsd.text.toString())){
            val db = FirebaseFirestore.getInstance()
            val user : MutableMap<String, Any> = HashMap()
            user["email"] = email.text.toString()
            user["password"] = confPsd.text.toString()
            Toast.makeText(applicationContext, email.text.toString(), Toast.LENGTH_SHORT).show()

            db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Record added sucessfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener(){
                    Toast.makeText(applicationContext, "Error occured", Toast.LENGTH_SHORT).show()
                }
        }

        else{

        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }
}