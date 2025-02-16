package io.github.jeffshee.visualizerdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import io.github.jeffshee.visualizer.desenhadores.espectro.BarraDeLedVertical
import io.github.jeffshee.visualizer.desenhadores.espectro.BarrasVerticais
import io.github.jeffshee.visualizer.desenhadores.espectro.BarrasVerticaisLED
import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper

class MainActivity : AppCompatActivity() {

    private lateinit var helper: VisualizerHelper
    private lateinit var background: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var circleBitmap: Bitmap
    private var current = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        } else init()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0 && grantResults[0] == 0) init()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }

    private fun init() {
        background = BitmapFactory.decodeResource(resources, R.drawable.background)
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.chino512)

        helper = VisualizerHelper(0)
        val painterLists = listOf(
            listOf(BarraDeLedVertical()), // Adicionar a nova visualização aqui
            listOf(BarrasVerticais()),     // Adicionar a visualização BarrasVerticais aqui
            listOf(BarrasVerticaisLED())
        )
        visual.setPainterList(
            helper, painterLists[current]
        )
        visual.setOnLongClickListener {
            if (current < painterLists.lastIndex) current++ else current = 0
            visual.setPainterList(helper, painterLists[current])
            true
        }

        Toast.makeText(this, "Try long-click \ud83d\ude09", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        helper.release()
        super.onDestroy()
    }
}