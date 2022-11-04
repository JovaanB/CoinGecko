package com.example.relevelandroidproject.presentation.AfterNotification

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.relevelandroidproject.MainActivity
import com.example.relevelandroidproject.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AfterNotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_notification)
        onNewIntent(intent)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, MainActivity::class.java))
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val bundle: Bundle? = intent!!.extras

        if(bundle != null) {
            val name = bundle.getString("KEY_NAME")
            val email = bundle.getString("KEY_EMAIL")

            Toast.makeText(this, "$name \n$email", Toast.LENGTH_LONG).show()
        }
    }
}
