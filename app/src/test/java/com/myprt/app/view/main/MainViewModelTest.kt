package com.myprt.app.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.myprt.app.data.DummyData
import com.myprt.app.data.model.Story
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.data.source.remote.PagingDataSourceTest
import com.myprt.app.util.Callback
import com.myprt.app.util.MainTestWatcher
import com.myprt.app.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainTestWatcher = MainTestWatcher()

    @Mock
    private lateinit var apiRepository: ApiRepository

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mainViewModel = MainViewModel(apiRepository = apiRepository)
    }

    private val dummyListStory = DummyData.getListStory()

    @Test
    fun `When get list story make sure return data is not null and the first item is match`() =
        runTest {
            val data = PagingDataSourceTest.snapshot(dummyListStory)
            val stories = MutableLiveData<PagingData<Story>>()
            stories.value = data
            Mockito.`when`(apiRepository.getPagingListStory()).thenReturn(stories)

            val actualListStory = mainViewModel.getListStory().getOrAwaitValue()
            Mockito.verify(apiRepository).getPagingListStory()

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoryAdapter.StoryDiffCallback(),
                updateCallback = Callback.listUpdateCallback,
                mainDispatcher = UnconfinedTestDispatcher(),
                workerDispatcher = UnconfinedTestDispatcher()
            )
            differ.submitData(actualListStory)
            advanceUntilIdle()

            assertNotNull(differ.snapshot())
            assertEquals(dummyListStory.size, differ.snapshot().size)
            assertEquals(dummyListStory.first(), differ.snapshot().first())
        }

    @Test
    fun `when get list story return empty list, make sure the list size is 0 or empty`() = runTest {
        val emptyList = emptyList<Story>()
        val emptyData = PagingDataSourceTest.snapshot(emptyList)

        val stories = MutableLiveData<PagingData<Story>>()
        stories.value = emptyData
        Mockito.`when`(apiRepository.getPagingListStory()).thenReturn(stories)

        val actualStories = mainViewModel.getListStory().getOrAwaitValue()

        Mockito.verify(apiRepository).getPagingListStory()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.StoryDiffCallback(),
            updateCallback = Callback.listUpdateCallback,
            mainDispatcher = UnconfinedTestDispatcher(),
            workerDispatcher = UnconfinedTestDispatcher()
        )
        differ.submitData(actualStories)
        advanceUntilIdle()

        assertTrue(differ.snapshot().isEmpty())
    }
}