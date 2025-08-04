
// Classe utilitária para facilitar o uso do Android Visualizer API
// Permite capturar dados de áudio (waveform e FFT) e calcular magnitudes de frequência
package de.lemke.audiovisualizer.utils


import android.media.audiofx.Visualizer
import kotlin.math.hypot


/**
 * Helper para capturar e processar dados de áudio usando a API Visualizer do Android.
 * @param sessionId ID da sessão de áudio a ser visualizada (ex: MediaPlayer.audioSessionId)
 */
class VisualizerHelper(sessionId: Int) {


    // Instância do Visualizer Android
    private val visualizer: Visualizer = Visualizer(sessionId)
    // Buffer para dados FFT brutos
    private val fftBuff: ByteArray
    // Buffer para magnitudes FFT (float, não usado diretamente)
    private val fftMF: FloatArray
    // Buffer para magnitudes FFT (double, usado nos cálculos)
    private val fftM: DoubleArray
    // Buffer para dados de waveform (onda bruta)
    private val waveBuff: ByteArray


    init {
        // Usa o maior tamanho de captura suportado pelo dispositivo
        visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        fftBuff = ByteArray(visualizer.captureSize)
        waveBuff = ByteArray(visualizer.captureSize)
        fftMF = FloatArray(fftBuff.size / 2 - 1)
        fftM = DoubleArray(fftBuff.size / 2 - 1)
        // Ativa o visualizer para começar a capturar dados
        visualizer.enabled = true
    }


    /**
     * Captura e retorna o buffer FFT bruto (valores intercalados real/imag).
     */
    fun getFft(): ByteArray {
        if (visualizer.enabled) visualizer.getFft(fftBuff)
        return fftBuff
    }


    /**
     * Captura e retorna o buffer de waveform (amostras PCM normalizadas).
     */
    fun getWave(): ByteArray {
        if (visualizer.enabled) visualizer.getWaveForm(waveBuff)
        return waveBuff
    }


    /**
     * Calcula o módulo (magnitude) de cada bin de frequência do FFT.
     * Retorna um array de double com as magnitudes.
     */
    fun getFftMagnitude(): DoubleArray {
        getFft()
        for (k in 0 until fftMF.size) {
            val i = (k + 1) * 2
            // Calcula a magnitude do bin k usando pitágoras: sqrt(real^2 + imag^2)
            fftM[k] = hypot(fftBuff[i].toDouble(), fftBuff[i + 1].toDouble())
        }
        return fftM
    }


    /**
     * Retorna as magnitudes FFT apenas no intervalo de frequências desejado (em Hz).
     * @param startHz frequência inicial (Hz)
     * @param endHz frequência final (Hz)
     */
    fun getFftMagnitudeRange(startHz: Int, endHz: Int): DoubleArray {
        val sIndex = hzToFftIndex(startHz)
        val eIndex = hzToFftIndex(endHz)
        return getFftMagnitude().copyOfRange(sIndex, eIndex)
    }


    /**
     * Converte uma frequência (Hz) para o índice correspondente no array FFT.
     * Fórmula baseada na documentação: k = f * n / Fs
     * (Aproximação para 1024 bins e Fs=44100Hz)
     */
    fun hzToFftIndex(hz: Int): Int {
        return (hz * 1024 / (44100 * 2)).coerceAtLeast(0).coerceAtMost(255)
    }


    /**
     * Libera recursos do Visualizer (deve ser chamado ao finalizar o uso).
     */
    fun release() {
        visualizer.enabled = false
        visualizer.release()
    }


}