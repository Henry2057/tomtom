package com.example.tomtom.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.ui.SearchFragment
import com.tomtom.sdk.search.ui.SearchFragmentListener
import com.tomtom.sdk.search.ui.model.PlaceDetails
import java.io.Closeable
import javax.inject.Singleton

private var TAG = SearchManagerImpl::class.java.simpleName
@Singleton
class SearchManagerImpl(
    private val search: Search,
) : SearchManager {

    private val _searchResults = MutableLiveData<PlaceDetails>()
    override val searchResults: LiveData<PlaceDetails> = _searchResults
    override fun initSearch(position: GeoPoint, fragment: SearchFragment) {

        fragment.setSearchApi(search)
        fragment.enableSearchBackButton(false)
        fragment.setFragmentListener(object : SearchFragmentListener {

            override fun onSearchResultClick(placeDetails: PlaceDetails) {
                _searchResults.postValue(placeDetails)
            }

            override fun onSearchBackButtonClick() {
                Log.d(TAG, "onSearchBackButtonClick")
            }

            override fun onSearchError(throwable: Throwable) {
                Log.d(TAG, "onSearchError: $throwable")
            }

            override fun onSearchQueryChanged(input: String) {
                Log.d(TAG, "onSearchQueryChanged: $input")
            }

            override fun onCommandInsert(command: String) {
                Log.d(TAG, "onCommandInsert: $command")
            }
        })
    }

    override fun close() {
        search.close()
    }
}
