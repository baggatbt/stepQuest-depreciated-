package com.example.myapplication.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class CombatActivity : AppCompatActivity() {

    private lateinit var attackAnimation: ImageView
    private lateinit var attackButton: Button

    private val attackFrames = arrayOf(
        R.drawable.basicslash1,
        R.drawable.basicslash2,
        R.drawable.basicslash3,
        R.drawable.basicslash4
    )
    private var currentFrame = 0

    private var attackTimer: CountDownTimer? = null
    private var lastTapTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        attackAnimation = findViewById(R.id.attack_animation)
        attackButton = findViewById(R.id.attack_button)

        attackButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastTap = currentTime - lastTapTime
            lastTapTime = currentTime

            // Check if the tap was within the correct timing window
            val correctTiming = timeSinceLastTap in 250..750

            if (correctTiming) {
                // Increase the damage dealt
                // ...
            } else {
                // Decrease the damage dealt
                // ...
            }
        }
        //test

        startAttackAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        attackTimer?.cancel()
    }

     private fun startAttackAnimation() {
        attackTimer = object : CountDownTimer(3000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the animation frame
                currentFrame++
                if (currentFrame >= attackFrames.size) {
                    currentFrame = 0 // reset to the first frame
                }
                attackAnimation.setImageResource(attackFrames[currentFrame])
            }

            override fun onFinish() {
                // End the attack
                finish()
            }
        }

        attackTimer?.start()
    }

}
