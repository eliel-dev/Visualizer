
override fun draw(canvas: Canvas, helper: VisualizerHelper) {
    val barWidth = canvas.width / 25f
    for (i in 0 until 25) {
        val barHeight = interpolatedFft.value(i.toDouble()).toFloat()
        val left = i * barWidth
        val top = canvas.height - barHeight
        val right = left + barWidth
        val bottom = canvas.height.toFloat()
        canvas.drawRect(left, top, right, bottom, paint)
    }
}
val fft = helper.getFftMagnitudes()
val gravityModels = Array(fft.size) { GravityModel() }
fft.forEachIndexed { index, value -> gravityModels[index].update(value) }
val interpolatedFft = interpolateFft(gravityModels, 25, "linear")
