/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.timeline

import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.douya.api.info.TimelineItem
import me.zhanghai.android.douya.api.util.uriOrUrl
import me.zhanghai.android.douya.arch.EventLiveData
import me.zhanghai.android.douya.arch.ResumedLifecycleOwner
import me.zhanghai.android.douya.arch.mapDistinct
import me.zhanghai.android.douya.arch.valueCompat
import me.zhanghai.android.douya.databinding.TimelineItemBinding
import me.zhanghai.android.douya.link.UriHandler
import me.zhanghai.android.douya.util.ListAdapter
import me.zhanghai.android.douya.util.layoutInflater
import me.zhanghai.android.douya.util.takeIfNotEmpty

class TimelineAdapter : ListAdapter<TimelineItem, TimelineAdapter.ViewHolder>() {
    private val imageRecyclerViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            TimelineItemBinding.inflate(parent.context.layoutInflater, parent, false),
            imageRecyclerViewPool
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setTimelineItem(items[position])
    }

    class ViewHolder(
        private val binding: TimelineItemBinding,
        imageRecyclerViewPool: RecyclerView.RecycledViewPool
    ) : RecyclerView.ViewHolder(binding.root) {
        private val lifecycleOwner = ResumedLifecycleOwner()

        private val viewModel = ViewModel()

        init {
            binding.timelineItemLayout.setImageRecyclerViewPool(imageRecyclerViewPool)

            binding.lifecycleOwner = lifecycleOwner
            binding.viewModel = viewModel
            viewModel.openUriEvent.observe(lifecycleOwner) {
                UriHandler.open(it, binding.root.context)
            }
        }

        fun setTimelineItem(timelineItem: TimelineItem?) {
            viewModel.setTimelineItem(timelineItem)
            binding.executePendingBindings()
        }

        class ViewModel {
            data class State(
                val resharer: String,
                val resharerUri: String,
                val timelineItem: TimelineItem?,
                val uri: String
            ) {
                companion object {
                    val INITIAL = State(
                        resharer = "",
                        resharerUri = "",
                        timelineItem = null,
                        uri = ""
                    )
                }
            }

            private val state = MutableLiveData(State.INITIAL)

            val resharer = state.mapDistinct { it.resharer }
            val timelineItem = state.mapDistinct { it.timelineItem }

            private val _openUriEvent = EventLiveData<String>()
            val openUriEvent: LiveData<String> = _openUriEvent

            fun setTimelineItem(timelineItem: TimelineItem?) {
                state.value = if (timelineItem != null) {
                    State(
                        resharer = timelineItem.resharer?.name ?: "",
                        resharerUri = timelineItem.resharer?.uriOrUrl ?: "",
                        timelineItem = timelineItem,
                        uri = timelineItem.uriOrUrl
                    )
                } else {
                    State.INITIAL
                }
            }

            fun openResharer() {
                state.valueCompat.resharerUri.takeIfNotEmpty()?.let { _openUriEvent.value = it }
            }

            fun open() {
                state.valueCompat.uri.takeIfNotEmpty()?.let { _openUriEvent.value = it }
            }
        }
    }
}
