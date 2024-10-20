package com.myprt.app.view.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myprt.app.databinding.ActivityLoginBinding
import com.myprt.app.util.fadeIn
import com.myprt.app.view.main.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels { LoginViewModel.Factory }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()
        setListeners()
        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginUiState.collect { state ->
                    binding.loginButton.isEnabled = (state is LoginUiState.Loading).not()

                    when (state) {
                        is LoginUiState.Loading -> {
                            binding.loginButton.isEnabled = false
                        }

                        is LoginUiState.Success -> {
                            runBlocking { viewModel.storeUser(state.loginResult) }
                            Toast.makeText(
                                this@LoginActivity, "Login successful", Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            finishAffinity()
                        }

                        is LoginUiState.Error -> {
                            Toast.makeText(
                                this@LoginActivity, state.errorMessage, Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            viewModel.login(email, password)
        }
    }

    private fun playAnimation() {
        binding.titleTextView.fadeIn()
        binding.messageTextView.fadeIn()
        binding.emailTextView.fadeIn()
        binding.emailEditTextLayout.fadeIn()
        binding.passwordTextView.fadeIn()
        binding.passwordEditTextLayout.fadeIn()
        binding.loginButton.fadeIn()
    }
}