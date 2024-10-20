package com.myprt.app.view.welcome

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myprt.app.databinding.ActivityWelcomeBinding
import com.myprt.app.view.login.LoginActivity
import com.myprt.app.view.main.MainActivity
import com.myprt.app.view.signup.SignupActivity
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {

    private val viewModel: WelcomeViewModel by viewModels { WelcomeViewModel.Factory }
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIfUserIsLoggedIn()
        setListeners()
        playAnimation()
    }

    private fun setListeners() {
        binding.signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkIfUserIsLoggedIn() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.welcomeUiState.collect { state ->
                    when (state) {
                        is WelcomeUiState.Authenticated -> {
                            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }

                        else -> return@collect
                    }
                }
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).apply {
            duration = ANIMATION_DURATION
        }.start()

        ObjectAnimator.ofFloat(binding.descTextView, View.ALPHA, 0f, 1f).apply {
            duration = ANIMATION_DURATION
        }.start()

        ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 0f, 1f).apply {
            duration = ANIMATION_DURATION
        }.start()

        ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 0f, 1f).apply {
            duration = ANIMATION_DURATION
        }.start()
    }

    companion object {
        private const val ANIMATION_DURATION = 3000L
    }
}