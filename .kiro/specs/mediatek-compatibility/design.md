# Design Document

## Overview

Esta funcionalidade implementa detecção automática de chipsets e configurações específicas de compatibilidade para dispositivos MediaTek. O sistema utiliza APIs nativas do Android para contornar limitações conhecidas dos chipsets MediaTek relacionadas ao `android.media.audiofx.Visualizer` e renderização com hardware acceleration.

### Problemas Identificados com MediaTek

Baseado na análise do código e pesquisa sobre compatibilidade:

1. **Hardware Acceleration**: MediaTek pode ter problemas com `LAYER_TYPE_HARDWARE`
2. **Visualizer API**: Diferenças na implementação do `android.media.audiofx.Visualizer`
3. **Threading**: Possíveis problemas com threading de áudio em chipsets MediaTek
4. **Capture Size**: Tamanhos de captura podem variar entre chipsets

## Architecture

### Component Overview

```
CompatibilityManager
├── ChipsetDetector
├── MediaTekCompatibilityConfig
├── CompatibilityLogger
└── PerformanceMonitor
```

### Core Components

1. **ChipsetDetector**: Detecta o tipo de chipset do dispositivo
2. **CompatibilityManager**: Gerencia configurações específicas por chipset
3. **MediaTekCompatibilityConfig**: Configurações específicas para MediaTek
4. **CompatibilityLogger**: Sistema de logging para debugging
5. **PerformanceMonitor**: Monitora performance e aplica otimizações

## Components and Interfaces

### ChipsetDetector

```kotlin
interface ChipsetDetector {
    fun detectChipset(): ChipsetType
    fun getChipsetInfo(): ChipsetInfo
}

enum class ChipsetType {
    SNAPDRAGON,
    MEDIATEK,
    EXYNOS,
    KIRIN,
    UNKNOWN
}

data class ChipsetInfo(
    val type: ChipsetType,
    val model: String,
    val manufacturer: String
)
```

### CompatibilityManager

```kotlin
interface CompatibilityManager {
    fun getCompatibilityConfig(chipsetType: ChipsetType): CompatibilityConfig
    fun applyCompatibilitySettings(config: CompatibilityConfig)
    fun forceCompatibilityMode(mode: CompatibilityMode)
}

data class CompatibilityConfig(
    val useHardwareAcceleration: Boolean,
    val visualizerCaptureSize: Int?,
    val useAlternativeThreading: Boolean,
    val renderingOptimizations: List<RenderingOptimization>
)
```

### MediaTekCompatibilityConfig

Configurações específicas para chipsets MediaTek:

```kotlin
object MediaTekCompatibilityConfig {
    val DEFAULT_CONFIG = CompatibilityConfig(
        useHardwareAcceleration = false, // Usar software rendering
        visualizerCaptureSize = 512,     // Tamanho menor e mais compatível
        useAlternativeThreading = true,   // Threading alternativo
        renderingOptimizations = listOf(
            RenderingOptimization.REDUCE_DRAW_CALLS,
            RenderingOptimization.OPTIMIZE_PAINT_OBJECTS,
            RenderingOptimization.BATCH_CANVAS_OPERATIONS
        )
    )
}
```

## Data Models

### CompatibilityMode

```kotlin
enum class CompatibilityMode {
    AUTO_DETECT,
    FORCE_SNAPDRAGON,
    FORCE_MEDIATEK,
    FORCE_SAFE_MODE
}
```

### RenderingOptimization

```kotlin
enum class RenderingOptimization {
    REDUCE_DRAW_CALLS,
    OPTIMIZE_PAINT_OBJECTS,
    BATCH_CANVAS_OPERATIONS,
    USE_BITMAP_CACHING,
    REDUCE_ANTI_ALIASING
}
```

## Error Handling

### Fallback Strategy

1. **Detecção Falha**: Se a detecção de chipset falhar, usar configurações seguras
2. **Visualizer Falha**: Se o Visualizer falhar, tentar configurações alternativas
3. **Performance Baixa**: Se FPS < 20, aplicar otimizações adicionais
4. **Rendering Falha**: Se hardware acceleration falhar, alternar para software

### Error Recovery

```kotlin
class CompatibilityErrorHandler {
    fun handleVisualizerError(error: VisualizerError): RecoveryAction
    fun handleRenderingError(error: RenderingError): RecoveryAction
    fun handlePerformanceIssue(fps: Int): List<OptimizationAction>
}
```

## Testing Strategy

### Unit Tests

1. **ChipsetDetector**: Testar detecção com diferentes Build.MODEL values
2. **CompatibilityManager**: Testar aplicação de configurações
3. **MediaTekCompatibilityConfig**: Validar configurações específicas
4. **PerformanceMonitor**: Testar monitoramento e otimizações

### Integration Tests

1. **VisualizerHelper**: Testar com configurações MediaTek
2. **VisualizerView**: Testar renderização com software/hardware
3. **End-to-End**: Testar fluxo completo de compatibilidade

### Device Testing

1. **MediaTek Devices**: Testar em dispositivos MediaTek reais
2. **Snapdragon Devices**: Garantir que não quebra funcionalidade existente
3. **Performance**: Medir FPS antes e depois das otimizações

## Implementation Details

### Detecção de Chipset

Usar `Build.HARDWARE`, `Build.BOARD`, e `Build.MODEL` para detectar MediaTek:

```kotlin
private fun isMediaTekChipset(): Boolean {
    val hardware = Build.HARDWARE.lowercase()
    val board = Build.BOARD.lowercase()
    val model = Build.MODEL.lowercase()
    
    return hardware.contains("mt") || 
           board.contains("mediatek") || 
           model.contains("mt")
}
```

### Configurações VisualizerHelper

Modificar `VisualizerHelper` para aceitar configurações de compatibilidade:

```kotlin
class VisualizerHelper(
    sessionId: Int,
    private val compatibilityConfig: CompatibilityConfig? = null
) {
    init {
        val captureSize = compatibilityConfig?.visualizerCaptureSize 
            ?: Visualizer.getCaptureSizeRange()[1]
        visualizer.captureSize = captureSize
    }
}
```

### Configurações VisualizerView

Modificar `VisualizerView` para usar configurações de renderização:

```kotlin
override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (this::painter.isInitialized && this::visualizerHelper.isInitialized) {
        val layerType = if (compatibilityConfig.useHardwareAcceleration) {
            LAYER_TYPE_HARDWARE
        } else {
            LAYER_TYPE_SOFTWARE
        }
        setLayerType(layerType, paint)
        // ... resto da implementação
    }
}
```

## Performance Considerations

### MediaTek Optimizations

1. **Software Rendering**: Usar `LAYER_TYPE_SOFTWARE` em vez de `LAYER_TYPE_HARDWARE`
2. **Reduced Capture Size**: Usar tamanhos menores de captura (512 em vez de 1024)
3. **Paint Optimization**: Reutilizar objetos Paint quando possível
4. **Canvas Batching**: Agrupar operações de desenho

### Monitoring

Implementar monitoramento contínuo de performance:

```kotlin
class PerformanceMonitor {
    private val fpsHistory = mutableListOf<Int>()
    
    fun recordFps(fps: Int) {
        fpsHistory.add(fps)
        if (fpsHistory.size > 30) fpsHistory.removeAt(0)
        
        val avgFps = fpsHistory.average()
        if (avgFps < 20) {
            applyAdditionalOptimizations()
        }
    }
}
```