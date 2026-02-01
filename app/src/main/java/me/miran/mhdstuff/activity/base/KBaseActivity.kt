package me.miran.mhdstuff.activity.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.miran.mhdstuff.R
import me.miran.mhdstuff.parsing.types.LineAlias
import me.miran.mhdstuff.ui.theme.AppTheme
import me.miran.mhdstuff.ui.theme.AppTypography
import java.util.function.Consumer
import kotlin.math.max
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
abstract class KBaseActivity(var nameId: Int) : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            CreateElements()}
    }

    fun setBaseContent(content: @Composable () -> Unit) {
        setContent {
            AppTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(this.nameId),
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
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
                        }
                    )
                }) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    abstract fun CreateElements();

    fun startActivity(clazz: KClass<out Activity>) {
        startActivity(clazz) {}
    }

    fun startActivity(clazz: KClass<out Activity>, intentSetup: Consumer<Intent>) {
        val intent = Intent(this, clazz.java)

        intentSetup.accept(intent)
        startActivity(intent)
        overridePendingTransition(R.anim.fast_scale_up, R.anim.fast_fade_out)
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
            Box(Modifier.padding(innerPadding), content= content);
        }
    }

    @Composable
    fun Container(modifier: Modifier = Modifier, innerPadding: Dp = 16.dp, content: @Composable BoxScope.() -> Unit) {
        Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors().copy(containerColor =  colorResource(R.color.widget_background))) {
            Box(Modifier.padding(innerPadding), content= content);
        }
    }

    @Composable
    fun Divider() {
        HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.mid_gray))
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
        val shape = RoundedCornerShape(8.dp);
        Surface(color = backgroundColor, shape = shape, modifier = Modifier.padding(padding).layout { measurable, constraints ->
            val measured = measurable.measure(constraints)

            var w = measured.width
            val h = measured.height

            w = max(w, h);

            val squareConstraints = Constraints.fixed(width = w, height = h)
            val placeable = measurable.measure(squareConstraints)

            layout(w, h) {
                placeable.place(0, 0)
            }
        }.then(
            if (backgroundColor == Color.Black) {
                Modifier.border(1.5.dp, textColor, shape = shape)
            } else {
                Modifier
            }
        )) {
            Text(text, textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp), color = textColor);
        }
    }

    @Composable
    fun Loading() {
        Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
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

}