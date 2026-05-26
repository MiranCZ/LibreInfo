package me.miran.libreinfo.activity

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
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
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.activity.data.DelaysDataHolder
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.storage.IdStorage
import me.miran.libreinfo.parsing.storage.StopStorage
import me.miran.libreinfo.parsing.types.stop.Stop
import me.miran.libreinfo.util.FuzzySearch
import me.miran.libreinfo.util.request.RequestHelper

class SearchActivity : KBaseActivity(R.string.departures) {

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
            val delays: JsonObject?
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
        val inst = IdStorage.getInstanceOrBlock(StopStorage::class.java).searcher

        SearchableList(inst)
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
    fun SearchableList(
        searcher: FuzzySearch<Stop>,
        vm: SearchViewModel = viewModel()
    ) {
        val liked by vm.liked

        val color = colorResource(R.color.widget_background);

        var forceRecompose by remember { mutableStateOf(0) }
        var query by remember { mutableStateOf("") }

        val filteredItems = remember(query, forceRecompose, liked) {
            val res = searcher.getResults(query)

            if (liked) {
                val first: ArrayList<Stop> = ArrayList()
                val second: ArrayList<Stop> = ArrayList()

                for (v in res) {
                    if (v.isFavourite) {
                        first.add(v)
                    } else {
                        second.add(v)
                    }
                }
                first.addAll(second)

                first
            } else {
                res
            }
        }

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

            key(forceRecompose, liked) {
                LazyColumn(Modifier.padding(top = 8.dp)) {
                    items(filteredItems) { item ->

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(null, ripple(), onClick = {
                                    startActivity(
                                        DeparturesActivity::class
                                    ) { intent -> intent.putExtra("stop", item) }
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
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        }
                        Divider()
                    }
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

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

}