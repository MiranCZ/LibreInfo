package io.github.mirancz.libreinfo.activity

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.JsonObject
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.activity.data.DelaysDataHolder
import io.github.mirancz.libreinfo.activity.settings.DepartureSource
import io.github.mirancz.libreinfo.activity.settings.LocationSettingsActivity
import io.github.mirancz.libreinfo.util.AppSettings
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.StopStorage
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.location.LocationProviderFactory
import io.github.mirancz.libreinfo.util.request.RequestHelper
import io.github.mirancz.libreinfo.util.search.FuzzyStopSearch
import io.github.mirancz.libreinfo.util.search.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class SearchActivity : KBaseActivity(R.string.departures) {

    companion object {
        const val EXTRA_PICKER_MODE = "picker_mode"
        const val EXTRA_RESULT_STOP = "stop"
    }

    class SearchViewModel : ViewModel() {
        private val _liked = mutableStateOf(true)
        val liked = _liked

        fun toggleLiked() {
            _liked.value = !_liked.value
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIXME remove use of thread
        Thread(Runnable {
            val delays: RouteDelaysResponse?
            try {
                delays = RequestHelper.getRouteDelays(this)
            } catch (e: RequestException) {
                showErrorSnackBar(e)
                return@Runnable
            }
            runOnUiThread { DelaysDataHolder.setDelays(delays) }
        }).start()
    }

    @Composable
    @Preview
    override fun CreateElements() {
        SearchableList()
    }

    override fun setBaseContent(
        actions: @Composable RowScope.() -> Unit,
        content: @Composable () -> Unit
    ) {

        super.setBaseContent({
            actions()

            val vm: SearchViewModel = viewModel()
            val liked by vm.liked

            IconButton(onClick = { vm.toggleLiked() }) {
                if (liked) {
                    Icon(
                        painter = painterResource(R.drawable.heart_solid),
                        contentDescription = "Unlike",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.heart_regular),
                        contentDescription = "Like",
                        tint = colorResource(R.color.light_blue),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }, content)
    }

    @Composable
    fun SearchableList(vm: SearchViewModel = viewModel()) {
        val context = LocalContext.current

        val dataResult = rememberLoad {
            val location = if (LocationSettingsActivity.shouldSortByDistance()) {
                LocationProviderFactory.create(context).getLastKnownLocation()
            } else null

            Pair(
                AppContainer.storageProvider.get(StopStorage::class).searcher, location
            )
        }

        var query by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        Column(Modifier.padding(horizontal = 8.dp)) {
            AppTextField(
                value = query,
                placeHolder = "Zadejte zastávku",
                onValueChange = { query = it },
                focusRequester = focusRequester,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(24.dp),
                        tint = colorResource(R.color.light_blue),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear text"
                            )
                        }
                    }
                }
            )

            AsyncContent(dataResult, loading = { StopListShimmer() }) { data ->
                val searcher = data.first
                val location = data.second

                StopList(searcher, query, location, vm)
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    @Composable
    fun StopList(
        searcher: FuzzyStopSearch,
        query: String,
        location: Location? = null,
        vm: SearchViewModel = viewModel()
    ) {
        val liked by vm.liked

        var forceRecompose by remember { mutableIntStateOf(0) }

        val listState = rememberLazyListState()

        var filteredItems: List<Stop> by remember { mutableStateOf(emptyList()) }

        LaunchedEffect(query, forceRecompose, liked) {
            val newItems = withContext(Dispatchers.Default) {
                val sortType = if (location != null) {
                    SortType.LocationBased(location)
                } else {
                    SortType.Alphabetical
                }

                val res = searcher.search(
                    query,
                    sortType = sortType,
                    isFavourite = { liked && it.isFavourite }
                )

                val result = ArrayList(res.favourites)
                result.addAll(res.others)

                result
            }

            filteredItems = newItems

            if (filteredItems.isNotEmpty()) {
                listState.scrollToItem(0)
            }
        }

        key(forceRecompose, liked) {
            LazyColumn(
                Modifier.padding(top = 8.dp),
                state = listState
            ) {
                items(
                    filteredItems,
                    key = { it.id.internal }
                ) { item ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(null, ripple(), onClick = {
                                if (intent.getBooleanExtra(EXTRA_PICKER_MODE, false)) {
                                    setResult(
                                        RESULT_OK,
                                        Intent().apply { putExtra(EXTRA_RESULT_STOP, item) })
                                    finish()
                                } else {
                                    // read on click so a source change mid-session applies right away
                                    val target =
                                        if (AppSettings.Departures.source == DepartureSource.SERVER) ServerDeparturesActivity::class
                                        else DeparturesActivity::class

                                    startActivity(target) { i ->
                                        i.putExtra(
                                            "stop",
                                            item
                                        )
                                    }
                                }
                            })
                            .padding(17.dp)
                            .fillMaxWidth()
                    ) {

                        if (item.isFavourite) {
                            Icon(
                                painter = painterResource(R.drawable.heart_solid),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Red
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.stop),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(R.color.light_blue)
                            )
                        }

                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 20.dp).weight(1f)
                        )

                        if (location != null) {
                            val distance = ceil(item.location.toAndroidLoc().distanceTo(location)).toInt()

                            val text = if (distance < 1_000) {
                                "$distance m"
                            } else {
                                "%.1f km".format(distance.toDouble()/1000.0)
                            }

                            Text(
                                text = text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(R.color.secondary_color_tone)
                            )
                        }
                    }
                    Divider()
                }
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current

        // FIXME this is not optimal optimal way to refresh (but I dont really care right now)
        // note: we are refreshing cuz favourite stops might change
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                forceRecompose += 1
            }
        }
    }

    @Composable
    fun StopListShimmer() {
        val shimmer = rememberActivityShimmer()
        Column(Modifier.padding(top = 8.dp)) {
            repeat(12) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(17.dp)
                        .fillMaxWidth()
                ) {
                    ShimmerBox(Modifier.size(20.dp), shimmer)
                    Spacer(Modifier.width(20.dp))
                    ShimmerText(shimmer, widthFraction = 0.6f, variance = 0.3f, height = 16.dp)
                }
                Divider()
            }
        }
    }

}