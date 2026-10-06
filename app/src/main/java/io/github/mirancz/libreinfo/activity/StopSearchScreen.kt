package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.settings.LocationSettingsScreen
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.parsing.storage.StopStorage
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.Location
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.parsing.types.stop.isFavourite
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.AppTextField
import io.github.mirancz.libreinfo.ui.components.AsyncContent
import io.github.mirancz.libreinfo.ui.components.Divider
import io.github.mirancz.libreinfo.ui.components.ShimmerBox
import io.github.mirancz.libreinfo.ui.components.ShimmerText
import io.github.mirancz.libreinfo.ui.components.rememberActivityShimmer
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.location.LocationProviderFactory
import io.github.mirancz.libreinfo.util.location.toAndroidLoc
import io.github.mirancz.libreinfo.util.request.RequestHelper
import io.github.mirancz.libreinfo.util.search.FuzzyStopSearch
import io.github.mirancz.libreinfo.util.search.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.math.ceil

@Serializable
sealed interface SearchOption {
    sealed interface StopOrLocation : SearchOption
    sealed interface StopOrPOI : SearchOption

    @Serializable
    data class PickedStop(val stop: Stop) : StopOrLocation, StopOrPOI


    @Serializable
    data class UserLocation(val location: Location) : StopOrLocation


    @Serializable
    data class POI(val location: Location, val name: String) : StopOrPOI
}

class SearchViewModel : ViewModel() {
    private val _liked = mutableStateOf(true)
    val liked = _liked

    fun toggleLiked() {
        _liked.value = !_liked.value
    }
}

@Serializable
data class SearchKinds(val stops: Boolean, val locations: Boolean, val pois: Boolean) {
    companion object {
        fun of(type: Class<out SearchOption>) = SearchKinds(
            stops = type.isAssignableFrom(SearchOption.PickedStop::class.java),
            locations = type.isAssignableFrom(SearchOption.UserLocation::class.java),
            pois = type.isAssignableFrom(SearchOption.POI::class.java),
        )
    }
}

@Composable
inline fun <reified T : SearchOption> SearchScreen(state: NavState, prefetchDelays: Boolean = false, noinline onPick: (T) -> Unit) {
    SearchScreen(state, SearchKinds.of(T::class.java), prefetchDelays) { onPick(it as T) }
}

@PublishedApi
@Composable
internal fun SearchScreen(state: NavState, kinds: SearchKinds, prefetchDelays: Boolean = false, onPick: (SearchOption) -> Unit) {
    val context = LocalContext.current
    if (prefetchDelays) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    // if route delays are more than 10 seconds old, fetch new one and cache them
                    RequestHelper.getRouteDelays(context, cacheTtl = 10)
                } catch (_: Exception){
                }
            }
        }
    }

    ScreenScaffold(stringResource(R.string.departures), onBack = state.onBack, actions = {
        val vm: SearchViewModel = viewModel()
        val liked by vm.liked

        IconButton(onClick = { vm.toggleLiked() }) {
            if (liked) {
                Icon(
                    painter = painterResource(R.drawable.heart_solid),
                    contentDescription = "Unlike",
                    tint = MaterialTheme.extendedColors.favourite,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.heart_regular),
                    contentDescription = "Like",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }) {
        SearchableList(kinds, onPick = onPick)
    }
}


@Composable
fun SearchableList(
    kinds: SearchKinds, onPick: (SearchOption) -> Unit
    ,vm: SearchViewModel = viewModel()) {
    val context = LocalContext.current

    val dataResult = rememberLoad {
        val location = if (LocationSettingsScreen.shouldSortByDistance()) {
            LocationProviderFactory.create(context).getLastKnownLocation()
        } else null

        Pair(
            AppContainer.storageProvider.get(StopStorage::class).searcher, location
        )
    }

//    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val query = rememberTextFieldState()

    Column(Modifier.padding(horizontal = 8.dp)) {
        AppTextField(
            state = query,
            placeHolder = "Zadejte zastávku",
            focusRequester = focusRequester,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingIcon = {
                if (query.text.isNotEmpty()) {
                    IconButton(onClick = { query.clearText() }) {
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

            StopList(searcher, query.text.toString(), location, kinds, onPick, vm)
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
    location: android.location.Location? = null,
    kinds: SearchKinds, onPick: (SearchOption) -> Unit,
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
                isFavourite = { liked && it.isFavourite() }
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
                            onPick(SearchOption.PickedStop(item))
                        })
                        .padding(17.dp)
                        .fillMaxWidth()
                ) {

                    if (item.isFavourite()) {
                        Icon(
                            painter = painterResource(R.drawable.heart_solid),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.extendedColors.favourite
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.stop),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
