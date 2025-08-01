package de.lemke.audiovisualizer.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.pintores.misc.SimpleText
import de.lemke.audiovisualizer.pintores.modificador.Compose
import de.lemke.audiovisualizer.utils.FrameManager
import de.lemke.audiovisualizer.utils.VisualizerHelper

class VisualizerView : View {

    // Gerenciador de frames para controle de FPS e performance
    private val frameManager = FrameManager()
    
    // Paint com anti-aliasing para desenhos suaves
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Texto simples para mostrar informações como FPS (12dp convertido para pixels)
    private val simpleText: SimpleText = SimpleText().apply { paint.textSize = 12 * resources.displayMetrics.density }
    
    // Painter que define como a visualização será desenhada
    private lateinit var painter: Pintor
    
    // Helper que fornece dados de áudio processados
    private lateinit var visualizerHelper: VisualizerHelper

    // Controles de exibição
    private val anim = true  // Se deve animar continuamente
    private val fps = true   // Se deve mostrar contador de FPS

    // Construtores padrão do Android para diferentes formas de criação da view
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Configura o visualizador com helper de áudio e painter de visualização
     * Deve ser chamado antes de usar a view
     * 
     * @param visualizerHelper Helper que fornece dados de áudio processados
     * @param painter Painter que define como desenhar a visualização
     */
    fun setup(visualizerHelper: VisualizerHelper, painter: Pintor) {
        this.visualizerHelper = visualizerHelper
        // Compõe o painter principal com o texto de FPS
        this.painter = Compose(painter, simpleText)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Só desenha se painter e helper estiverem inicializados
        if (this::painter.isInitialized && this::visualizerHelper.isInitialized) {
            // Usa aceleração de hardware para melhor performance
            setLayerType(LAYER_TYPE_HARDWARE, paint)
            
            canvas.apply {
                // Atualiza texto do FPS se habilitado
                simpleText.text = if (fps) "FPS: ${frameManager.fps()}" else ""
                
                // Calcula dados da visualização baseado no áudio atual
                painter.calc(visualizerHelper)
                
                // Desenha a visualização no canvas
                painter.draw(canvas, visualizerHelper)
            }
            
            // Registra este frame para cálculo de FPS
            frameManager.tick()
            
            // Se animação estiver habilitada, agenda próximo frame
            if (anim) invalidate()
        }
    }
}