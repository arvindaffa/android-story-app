package com.myprt.app.view.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myprt.app.R
import com.myprt.app.data.repository.UserRepository
import com.myprt.app.databinding.ActivityMainBinding
import com.myprt.app.view.MyApplication
import com.myprt.app.view.detail.StoryDialogFragment
import com.myprt.app.view.maps.MapsActivity
import com.myprt.app.view.upload.UploadActivity
import com.myprt.app.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var userRepository: UserRepository
    private lateinit var binding: ActivityMainBinding
    private val storyAdapter by lazy { StoryAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as MyApplication
        userRepository = application.injection.userRepository

        showGreeting()
        setupRecyclerView()
        observeUiState()
        setListeners()
    }

    private fun setListeners() {
        binding.logoutButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this).setTitle(R.string.logout)
                .setMessage("Apakah anda yakin ingin keluar?").setPositiveButton("Ya") { _, _ ->
                    runBlocking { userRepository.logout() }

                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    finishAffinity()
                }.setNegativeButton("Tidak", null).create()
            alertDialog.show()
        }
        binding.addPhotoButton.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeUiState() {
        viewModel.getListStory().observe(this) { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        }
        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.progressBar.isVisible = loadStates.refresh is LoadState.Loading
            }
        }
    }

    private fun setupRecyclerView() {
        binding.storyRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = storyAdapter.withLoadStateFooter(
                footer = StoryLoadStateAdapter {
                    storyAdapter.retry()
                }
            )
            setHasFixedSize(false)
            isNestedScrollingEnabled = false

            storyAdapter.onItemClick = {
                val dialog = StoryDialogFragment(it)
                dialog.show(supportFragmentManager, dialog.tag)
            }

            storyAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.storyRecyclerView.scrollToPosition(0)
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showGreeting() {
        lifecycleScope.launch {
            val user = userRepository.getUser().first()
            binding.nameTextView.text = getString(R.string.greeting, user?.name)
            viewModel.getListStory()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getListStory()
        storyAdapter.refresh()
    }
}