package com.myprt.app.view.detail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myprt.app.data.model.Story
import com.myprt.app.databinding.DialogDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

class StoryDialogFragment(private val story: Story) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogDetailBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(0.4f)

            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(binding.root)
            .load(story.photoUrl)
            .into(binding.imageView)

        binding.tvName.text = story.name
        binding.tvDescription.text = story.description

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.forLanguageTag("id-ID"))
        val parsedDate = simpleDateFormat.parse(story.createdAt)
        val formattedDate = parsedDate?.let {
            SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(
                it
            )
        } ?: story.createdAt
        binding.tvDate.text = formattedDate
    }
}