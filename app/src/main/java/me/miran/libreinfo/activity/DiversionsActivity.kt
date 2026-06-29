package me.miran.libreinfo.activity

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentinilk.shimmer.Shimmer
import kotlinx.coroutines.launch
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.manager.AppContainer
import me.miran.libreinfo.parsing.types.Diversion
import me.miran.libreinfo.parsing.types.LineAlias
import me.miran.libreinfo.util.load.rememberLoad
import me.miran.libreinfo.util.request.RequestHelper
import kotlin.random.Random

class DiversionsActivity : KBaseActivity(R.string.diversions) {


    class DiversionsViewModel : ViewModel() {
        var showFilterPopup: Boolean by mutableStateOf(false)
        var filters: Set<LineAlias> by mutableStateOf(emptySet())
    }

    override fun setBaseContent(
        actions: @Composable RowScope.() -> Unit,
        content: @Composable () -> Unit
    ) {


        super.setBaseContent({
            val vm: DiversionsViewModel = viewModel()

            actions()

            IconButton(onClick = { vm.showFilterPopup = true }) {
                val painter = if (vm.filters.isEmpty()) R.drawable.filter_empty else R.drawable.filter_full

                    Icon(
                        painter = painterResource(painter),
                        contentDescription = "filter",
                        tint = colorResource(R.color.light_blue),
                        modifier = Modifier.size(32.dp)
                    )
            }
        }, content)
    }

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current
        val vm: DiversionsViewModel = viewModel()

        val loadResult = rememberLoad {
            val storage = AppContainer.storageProvider.getInstance()
            Pair(storage, Diversion.parseDiversions(RequestHelper.getDiversions(context), storage.lineStorage))
        }

        AsyncContent(loadResult, loading = { DiversionListShimmer() }) { res ->
            val storage = res.first
            val diversionList = res.second

            if (vm.showFilterPopup) {
                val lines = HashSet(storage.lineStorage.allAliases)

                for (diversion in diversionList) {
                    lines.addAll(diversion.lines)
                }


                LineFilterSheet(lines.sortedBy {it.id})
            }

            if (diversionList.isEmpty()) {
                NothingHere()
            } else {
                var rendered = false

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (diversion in diversionList) {
                        if (shouldShowDiversion(diversion, vm.filters)) {
                            rendered = true
                            Diversion(diversion)
                        }
                    }
                }

                if (!rendered) {
                    NothingHere(stringResource(R.string.filters_found_nothing))
                }
            }
        }


    }

    private fun shouldShowDiversion(diversion: Diversion, filters: Set<LineAlias>): Boolean {
        if (filters.isEmpty()) return true

        for (alias in diversion.lines) {
            if (filters.contains(alias)) return true
        }

        return false;
    }

    @Composable
    fun DiversionListShimmer() {
        val shimmer = rememberActivityShimmer()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            repeat(4) { DiversionEntryShimmer(shimmer) }
        }
    }

    @Composable
    fun DiversionEntryShimmer(shimmer: Shimmer) {
        val lineIconCount = remember { Random.nextInt(1, 7) }

        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                ShimmerText(shimmer, widthFraction = 0.7f, variance = 0.35f, height = 20.dp)
                Spacer(Modifier.height(8.dp))
                ShimmerText(shimmer, widthFraction = 0.45f)
                Spacer(Modifier.height(6.dp))
                ShimmerText(shimmer, widthFraction = 0.45f)
                Spacer(Modifier.height(10.dp))
                Row {
                    repeat(lineIconCount) {
                        ShimmerLineIcon(shimmer)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun Diversion(item: Diversion) {
        Container(
            onClick = {
                startActivity(
                    DiversionInfoActivity::class
                ) { intent: Intent -> intent.putExtra("diversion", item) }
            },
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                EventHeader(item)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LineFilterSheet(aliases: List<LineAlias>) {
        val vm: DiversionsViewModel = viewModel()
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val filters = remember{ mutableStateSetOf<LineAlias>()}

        LaunchedEffect(Unit) {
            filters.addAll(vm.filters)
        }

        ModalBottomSheet(sheetState = sheetState, onDismissRequest = {
            vm.showFilterPopup = false
        }) {
            Column(Modifier
                .fillMaxHeight(0.8f)
                .padding(horizontal = 8.dp)) {
                var value by remember { mutableStateOf("") }

                val items = remember(value, aliases) {
                    if (value.isEmpty()) {
                        aliases.toList()
                    } else {
                        aliases.filter { it.lineDisplayName.contains(value, ignoreCase = true) }
                    }
                }


                AppTextField(value, placeHolder = stringResource(R.string.search), onValueChange = { value = it })

                Spacer(Modifier.padding(8.dp))

                val density = LocalDensity.current

                val size = with(density) { 31.sp.toDp() * 1.5f }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(size),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items) { line ->
                            key(line) {
                                val selected = filters.contains(line)
                                val alpha by animateFloatAsState(
                                    targetValue = if (selected) 1f else 0.4f,
                                    animationSpec = tween()
                                )

                                LineIcon(
                                    Modifier
                                        .alpha(alpha)
                                        .toggleable(
                                            value = selected,
                                            role = Role.Checkbox
                                        ) { checked ->
                                            if (checked) {
                                                filters.add(line)
                                            } else {
                                                filters.remove(line)
                                            }
                                        }, line = line, padding = 0.dp, scale = 1.5f
                                )
                            }
                        }
                    }
                }

                Divider()
                Spacer(Modifier.padding(8.dp))

                AppButton(
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, colorResource(R.color.secondary_color_tone)),
                    onClick = {
                    vm.filters = emptySet()
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) vm.showFilterPopup = false
                        }
                }) {
                    Text(stringResource(R.string.clear_filters), color = colorResource(R.color.secondary_color_light_tone))
                }

                AppButton(
                    color = colorResource(R.color.light_blue),
                    onClick = {
                    vm.filters = HashSet(filters)
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) vm.showFilterPopup = false
                        }
                }) {
                    Text(stringResource(R.string.apply), color = Color.White)
                }

            }

        }
    }

}