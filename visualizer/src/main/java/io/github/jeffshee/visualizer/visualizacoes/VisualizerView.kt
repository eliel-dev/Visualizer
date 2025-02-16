package io.github.jeffshee.visualizer.visualizacoes

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import io.github.jeffshee.visualizer.desenhadores.Painter
import io.github.jeffshee.visualizer.desenhadores.diversos.TextoSimples
import io.github.jeffshee.visualizer.desenhadores.espectro.BarraDeLedVertical
import io.github.jeffshee.visualizer.utilitarios.FrameManager
import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper
import io.github.jeffshee.visualizer.desenhadores.espectro.BarrasVerticais
import io.github.jeffshee.visualizer.desenhadores.espectro.BarrasVerticaisLED

class VisualizerView : View {

    private val frameManager = FrameManager()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var painterList: List<Painter>
    private lateinit var helper: VisualizerHelper
    private lateinit var textoSimples: TextoSimples
    private val barrasVerticais = BarrasVerticais()
    private val barraDeLedVertical = BarraDeLedVertical();
    private val barrasVerticaisLED = BarrasVerticaisLED();

    var anim = true
    var fps = true

    companion object {
        private fun dp2px(resources: Resources, dp: Float): Float {
            return dp * resources.displayMetrics.density
        }
    }

    constructor(context: Context) : super(context) {
        onCreateView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onCreateView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        onCreateView()
    }

    fun setPainterList(visualizerHelper: VisualizerHelper, list: List<Painter>) {
        helper = visualizerHelper
        painterList = list
    }

    private fun onCreateView() {
        textoSimples = TextoSimples(Paint().apply {
            color = Color.WHITE;textSize = dp2px(resources, 12f)
        })
        painterList = listOf(barrasVerticais, barraDeLedVertical, barrasVerticaisLED)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(this::painterList.isInitialized && this::helper.isInitialized){
            setLayerType(LAYER_TYPE_HARDWARE, paint)
            canvas?.apply {
                painterList.forEach {
                    it.calc(helper)
                    it.draw(canvas, helper) }
                textoSimples.text = "FPS: ${frameManager.fps()}"
                if (fps) textoSimples.draw(canvas, helper)
            }
            frameManager.tick()
            if (anim) invalidate()
        }
    }
}