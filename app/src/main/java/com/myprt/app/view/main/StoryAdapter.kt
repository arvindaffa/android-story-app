package com.myprt.app.view.main

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myprt.app.data.model.Story
import com.myprt.app.databinding.ItemStoryBinding
import java.util.Locale

class StoryAdapter : PagingDataAdapter<Story, StoryAdapter.StoryViewHolder>(
    StoryDiffCallback()
) {

    var onItemClick: ((Story) -> Unit?)? = null

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val story = getItem(bindingAdapterPosition)
                story?.let {
                    onItemClick?.invoke(it)
                }
            }
        }

        fun bind(story: Story?) {
            story?.let {
                Glide.with(itemView).load(story.photoUrl).into(binding.ivStory)

                binding.tvStoryName.text = story.name
                binding.tvStoryDescription.text = story.description

                if (story.lon != null && story.lat != null) {
                    binding.tvStoryLocation.isVisible = true
                    binding.tvStoryLocation.text = getLocation(story.lat, story.lon)
                }
            }
        }

        private fun getLocation(lat: Double, lon: Double): String {
            val geocoder = Geocoder(itemView.context, Locale.forLanguageTag("id-ID"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            addresses?.let {
                return it.first().adminArea.orEmpty()
            }
            return "Unknown Location"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StoryDiffCallback : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }
}