package me.miran.libreinfo.activity.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.DeparturePostDetailActivity
import me.miran.libreinfo.activity.TripDetailActivity
import me.miran.libreinfo.activity.base.snackbar.CustomSnackBarVisuals
import me.miran.libreinfo.activity.base.snackbar.SnackBarType
import me.miran.libreinfo.activity.settings.DelayRenderType
import me.miran.libreinfo.exception.AppException
import me.miran.libreinfo.parsing.storage.ApiStorage
import me.miran.libreinfo.parsing.types.DateTime
import me.miran.libreinfo.parsing.types.Diversion
import me.miran.libreinfo.parsing.types.LineAlias
import me.miran.libreinfo.parsing.types.Post
import me.miran.libreinfo.parsing.types.Time
import me.miran.libreinfo.parsing.types.departure.Departure
import me.miran.libreinfo.parsing.types.departure.DepartureEntry
import me.miran.libreinfo.ui.theme.AppTheme
import me.miran.libreinfo.ui.theme.AppTypography
import me.miran.libreinfo.util.HtmlHelper
import me.miran.libreinfo.util.LocalDeparturesSettings
import me.miran.libreinfo.util.Pair
import me.miran.libreinfo.util.Text
import java.util.function.Consumer
import kotlin.math.max
import kotlin.random.Random
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
abstract class KBaseActivity(name: Text) : ComponentActivity() {

    constructor(nameId: Int) : this(Text.translatable(nameId))
    constructor(nameStr: String) : this(Text.literal(nameStr))
    var name by mutableStateOf(name)

    private val snackBarHostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            CreateElements()
        }
    }

    open fun setBaseContent(actions: @Composable RowScope.() -> Unit = {}, content: @Composable () -> Unit) {
        setContent {
            AppTheme {
                val context = LocalContext.current

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) { data ->
                        val customVisuals = data.visuals as? CustomSnackBarVisuals

                        val type = customVisuals?.type ?: SnackBarType.INFO

                        // TODO make theme colors
                        val backgroundColor = when (type) {
                            SnackBarType.SUCCESS -> Color(0xFF4CAF50)
                            SnackBarType.ERROR -> colorResource(R.color.ui_warning)
                            SnackBarType.INFO -> Color(0xFF323232)
                        }

                        Snackbar(
                            snackbarData = data,
                            containerColor = backgroundColor,
                            contentColor = colorResource(R.color.secondaryColor)
                        )
                    } },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = name.getName(context),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            navigationIcon = {
                                if (parentActivityIntent != null) {
                                    IconButton(onClick = { onBackPressed() }) {
                                        Icon(
                                            painterResource(R.drawable.chevron_left),
                                            "Go back",
                                            tint = colorResource(R.color.light_blue)
                                        )
                                    }
                                }
                            },
                            actions = actions
                        )
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    abstract fun CreateElements()

    fun startActivity(clazz: KClass<out Activity>) {
        startActivity(clazz) {}
    }

    fun startActivity(clazz: KClass<out Activity>, intentSetup: Consumer<Intent>) {
        val intent = Intent(this, clazz.java)

        intentSetup.accept(intent)
        startActivity(intent)
        overridePendingTransition(R.anim.fast_scale_up, R.anim.fast_fade_out)
    }

    fun showSnackBar(message: String, type: SnackBarType) {
        lifecycleScope.launch {
            snackBarHostState.showSnackbar(
                CustomSnackBarVisuals(
                    message = message,
                    type = type
                )
            )
        }
    }

    fun showErrorSnackBar(e: AppException) {
        showSnackBar(e.getPrettyText(this), type = SnackBarType.ERROR)
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down)
    }

    override fun onNavigateUp(): Boolean {
        val result = super.onNavigateUp()
        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down)
        return result
    }

    @Composable
    fun Container(onClick: () -> Unit,modifier: Modifier = Modifier, innerPadding: Dp = 16.dp, content: @Composable BoxScope.() -> Unit) {
        Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors().copy(containerColor =  colorResource(R.color.widget_background)), onClick = onClick) {
            Box(Modifier.padding(innerPadding), content= content)
        }
    }

    @Composable
    fun Container(modifier: Modifier = Modifier, innerPadding: Dp = 16.dp, content: @Composable BoxScope.() -> Unit) {
        Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors().copy(containerColor =  colorResource(R.color.widget_background))) {
            Box(Modifier.padding(innerPadding), content= content)
        }
    }

    @Composable
    fun ErrorWidget(error: AppException, modifier: Modifier = Modifier) {
        val context = LocalContext.current

        Box(modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                        painter = painterResource(R.drawable.triangle_exclamation_regular),
                        "error",
                        tint = Color.Red
                )

                Text("An error occurred", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(error.getPrettyText(context), fontSize = 24.sp, fontWeight = FontWeight.Normal)
            }
        }
    }

    @Composable
    fun Divider(modifier: Modifier = Modifier) {
        HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.mid_gray), modifier = modifier)
    }

    @Composable
    fun LineList(lines: List<LineAlias>, modifier: Modifier = Modifier) {
        FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (line in lines) {
                LineIcon(line, padding = 0.dp)
            }
        }
    }

    @Composable
    fun LineIcon(line: LineAlias, padding: Dp = 4.dp) {
        LineIcon(line.lineDisplayName, Color(line.textColor), Color(line.backgroundColor()), padding)
    }

    @Composable
    fun LineIcon(text: String, textColor: Color, backgroundColor: Color, padding: Dp = 4.dp) {
        val shape = RoundedCornerShape(8.dp)
        Surface(color = backgroundColor, shape = shape, modifier = Modifier
            .padding(padding)
            .layout { measurable, constraints ->
                val measured = measurable.measure(constraints)

                var w = measured.width
                val h = measured.height

                w = max(w, h)

                val squareConstraints = Constraints.fixed(width = w, height = h)
                val placeable = measurable.measure(squareConstraints)

                layout(w, h) {
                    placeable.place(0, 0)
                }
            }
            .then(
                if (backgroundColor == Color.Black) {
                    Modifier.border(1.5.dp, textColor, shape = shape)
                } else {
                    Modifier
                }
            )) {
            Text(text, textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp), color = textColor)
        }
    }

    @Composable
    fun EventHeader(item: Diversion, content: @Composable ColumnScope.() -> Unit = {}) {
        Column {
            Text(
                text = item.title,
                fontWeight = FontWeight.Black,
                style = AppTypography.titleMedium
            )

            if (item.from != DateTime.NONE) {
                Row {
                    Text(
                        "Od: ",
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        item.from.toString(),
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (item.to != DateTime.NONE) {
                Row {
                    Text(
                        "Do: ",
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        item.to.toString(),
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LineList(item.lines, Modifier.padding(top = 8.dp))

            content()
        }
    }

    @Composable
    fun HTML(htmlString: String, modifier: Modifier = Modifier, update: (TextView) -> Unit = NoOpUpdate) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                TextView(context).apply {
                    text = HtmlHelper.parseHtml(htmlString)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            },
            update = update,

        )
    }

    @Composable
    fun rememberActivityShimmer() = rememberShimmer(ShimmerBounds.Window)

    @Composable
    fun ShimmerBox(modifier: Modifier, shimmer: Shimmer, shape: Shape = RoundedCornerShape(4.dp)) {
        Box(modifier.shimmer(shimmer).background(colorResource(R.color.mid_gray), shape))
    }

    @Composable
    fun ShimmerText(shimmer: Shimmer, widthFraction: Float = 0.85f, variance: Float = 0.15f, height: Dp = 14.dp) {
        val width = remember { (widthFraction + Random.nextFloat() * variance - variance / 2f).coerceIn(0.1f, 1f) }
        ShimmerBox(Modifier.fillMaxWidth(width).height(height), shimmer)
    }

    @Composable
    fun ShimmerLineIcon(shimmer: Shimmer) {
        ShimmerBox(Modifier.size(32.dp), shimmer, shape = RoundedCornerShape(8.dp))
    }

    @Composable
    fun Loading() {
        Box(Modifier
            .fillMaxWidth()
            .fillMaxHeight(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.size(80.dp), strokeWidth = 6.dp)
        }
    }

    @Composable
    fun NothingHere() {
        Container(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth(),contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.nothing_here),
                    fontWeight = FontWeight.Medium,
                    style = AppTypography.titleMedium
                )
            }
        }
    }

    @Composable
    fun AppTextField(
        value: String,
        modifier: Modifier = Modifier,
        onValueChange: (String) -> Unit = {},
        placeHolder: String = "",
        focusRequester: FocusRequester = remember { FocusRequester() },
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        color: Color = colorResource(R.color.widget_background),
        readOnly: Boolean = false,
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text(placeHolder) },
            shape = RoundedCornerShape(8.dp),
            leadingIcon = leadingIcon,
            singleLine = true,
            trailingIcon = trailingIcon,
            readOnly = readOnly,
            colors = TextFieldDefaults.colors()
                .copy(
                    unfocusedContainerColor = color,
                    focusedContainerColor = color,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
        )
    }

    @Composable
    fun AppButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {},
        color: Color = colorResource(R.color.widget_background),
        minHeight: Dp? = null,
        content: @Composable RowScope.() -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "button_scale")

        var modifier = modifier
        if (minHeight != null) {
            modifier = modifier.heightIn(min = minHeight)
        }

        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(color = colorResource(R.color.light_gray))
        ) {
            Button(
                onClick = onClick,
                content = content,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = color
                ),
                interactionSource = interactionSource,
                modifier = modifier.fillMaxWidth().scale(scale)
            )
        }
    }


    @Composable
    fun AppSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = colorResource(R.color.light_blue),
                checkedThumbColor = Color.White,
            )
        )
    }

    @Composable
    fun <T> AppDropdown(
        selected: T,
        options: List<T>,
        onSelect: (T) -> Unit,
        modifier: Modifier = Modifier,
        color: Color = colorResource(R.color.widget_background),
        displayString: (T) -> String = { it.toString() },
    ) {
        var expanded by remember { mutableStateOf(false) }
        val accentColor = colorResource(R.color.light_blue)
        val textColor = colorResource(R.color.secondaryColor)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier,
        ) {
            AppTextField(
                value = displayString(selected),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                onValueChange = {},
                placeHolder = "",
                trailingIcon = {
                    CompositionLocalProvider(LocalContentColor provides accentColor) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    }
                },
                color = color,
                readOnly = true,
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = color,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                displayString(option),
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            )
                        },
                        trailingIcon = if (isSelected) ({
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }) else null,
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(textColor = textColor),
                    )
                }
            }
        }
    }


    @Composable
    fun Departure(departure: Departure, post: Post?) {
        val content: @Composable BoxScope.() -> Unit = {
            Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                DeparturePostHeader(departure.name, Modifier.padding(bottom = 4.dp))
                val depSettings = LocalDeparturesSettings.current
                for (dep in departure.entries.take(depSettings.maxEntries)) {
                    DepartureEntry(dep)
                }
            }
        }
        val mod = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        if (post != null) {
            Container({ startActivity(DeparturePostDetailActivity::class) {
                it.putExtra("post", post)
            } }, innerPadding = 0.dp, modifier = mod, content = content)
        } else {
            Container(innerPadding = 0.dp, modifier = mod, content = content)
        }
    }

    @Composable
    fun DepartureDetail(departure: Departure, apiStorage: ApiStorage, stopDelays: JsonObject) {
        val color = colorResource(R.color.widget_background)

        fun alreadyLeft(entry: DepartureEntry): Boolean {
            return entry.timeMark.time().isBefore(Time.now()) && !entry.timeMark.leaving
        }

        Container(
            innerPadding = 0.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val first = departure.entries.indexOfFirst { entry -> !alreadyLeft(entry) }
            val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = first)

            LazyColumn(Modifier.padding(vertical = 8.dp, horizontal = 6.dp), state = lazyListState) {
                stickyHeader {
                    DeparturePostHeader(departure.name, Modifier
                        .background(color)
                        .clickable(interactionSource = null, indication = null) {})
                }
                items(departure.entries) { entry ->
                    val alreadyLeft = alreadyLeft(entry)

                    var modifier: Modifier = Modifier

                    if (alreadyLeft) {
                        modifier = modifier.alpha(0.35f)
                    }
                    val lineRoute: Pair<Int?, Int?> = apiStorage.getLineIdAndRoute(entry.tripId)

                    val lineId = lineRoute.left.toString()
                    val routeId = lineRoute.right.toString()

                    var showDelay = !alreadyLeft
                    if (alreadyLeft) {
                        var delay = -1
                        if (stopDelays.has(lineId)) {
                            val delays: JsonObject = stopDelays.getAsJsonObject(lineId)

                            if (delays.has(routeId)) {
                                delay = delays.getAsJsonObject(routeId).get("delay").asInt
                            }
                        }
                        entry.vehicleInfo.setDelay(delay)

                        showDelay = delay != -1
                    }

                    DepartureEntry(entry, modifier, showDelay)
                }
            }
        }
    }

    @Composable
    fun DeparturePostHeader(name: String, modifier: Modifier = Modifier) {
        Column(modifier) {
            Text(
                name,
                color = colorResource(R.color.secondaryColor),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )

            Divider(Modifier
                .padding(horizontal = 10.dp)
                .padding(top = 4.dp))
        }
    }


    @Composable
    fun DepartureEntry(departure: DepartureEntry, modifier: Modifier = Modifier, showDelay: Boolean = true) {
        val vehicleInfo = departure.vehicleInfo
        val depSettings = LocalDeparturesSettings.current

        Box(
            modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(null, ripple(), onClick = {
                    startActivity(
                        TripDetailActivity::class
                    ) { intent: Intent ->
                        if (vehicleInfo.hasDelay()) {
                            intent.putExtra("delay", vehicleInfo.delay())
                        }
                        if (vehicleInfo.hasId()) {
                            intent.putExtra("vehicleId", vehicleInfo.id())
                        }

                        intent.putExtra("stopId", departure.stopId)
                        intent.putExtra("tripId", departure.tripId)
                    }
                })
                .padding(horizontal = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Row(Modifier.weight(3f)) {
                    LineIcon(departure.line)
                    Text(
                        departure.finalStop,
                        fontSize = 14.sp,
                        color = colorResource(R.color.secondaryColor),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp)
                    )
                }

                if (departure.lowFloor && depSettings.showLowFloor) {
                    Icon(
                        painter = painterResource(R.drawable.wheelchair_regular),
                        "lowfloor",
                        Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        tint = colorResource(R.color.secondary_color_light_tone)
                    )
                }

                Row(
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    if (vehicleInfo.hasDelay() && showDelay) {
                        val delay: Int = vehicleInfo.delay()
                        val color: Int = vehicleInfo.delayColor

                        departure.timeMark.stopTime.delay = delay
                        val arrivalText: String = departure.timeMark.getFormattedString(30, true)


                        Spacer(Modifier.weight(1f))

                        var delayStr = ""

                        if (delay > 0) {
                            when (depSettings.delayRender) {
                                DelayRenderType.PARENTHESES -> {
                                    delayStr = " ($delay) "
                                }
                                DelayRenderType.BOX -> {
                                    Surface(
                                        color = Color(color).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(" +$delay ", color = Color(color), fontSize = 14.sp)
                                    }
                                }
                                else -> {}
                            }
                        }

                        Text(
                            delayStr + arrivalText,
                            color = Color(color),
                            fontSize = 14.sp
                        )
                    } else {
                        val arrivalText: String = departure.timeMark.getFormattedString(30, false)

                        Spacer(Modifier.weight(1f))
                        Text(text = arrivalText, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    @Composable
    fun DepartureEntryRowShimmer(shimmer: Shimmer) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.weight(3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerLineIcon(shimmer)
                Spacer(Modifier.width(4.dp))
                ShimmerText(shimmer, widthFraction = 0.55f, variance = 0.2f)
            }
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                ShimmerText(shimmer, widthFraction = 0.85f, variance = 0.1f)
            }
        }
    }



}