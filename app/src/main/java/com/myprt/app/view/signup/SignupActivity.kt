package com.myprt.app.view.signup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myprt.app.databinding.ActivitySignupBinding
import com.myprt.app.util.fadeIn
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private val viewModel: SignupViewModel by viewModels { SignupViewModel.Factory }
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()
        setListeners()
        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signupUiState.collect { state ->
                    binding.signupButton.isEnabled = (state is SignupUiState.Loading).not()

                    when (state) {
                        is SignupUiState.Loading -> {
                            binding.signupButton.isEnabled = false
                        }

                        is SignupUiState.Success -> {
                            Toast.makeText(this@SignupActivity, "Sign up successful", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                        is SignupUiState.Error -> {
                            Toast.makeText(this@SignupActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            viewModel.register(name, email, password)
        }
    }

    private fun playAnimation() {
        binding.titleTextView.fadeIn()
        binding.nameTextView.fadeIn()
        binding.nameEditTextLayout.fadeIn()
        binding.emailTextView.fadeIn()
        binding.emailEditTextLayout.fadeIn()
        binding.passwordTextView.fadeIn()
        binding.passwordEditTextLayout.fadeIn()
        binding.signupButton.fadeIn()
    }
}