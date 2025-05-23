package voice.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import kotlinx.coroutines.launch
import voice.common.AppInfoProvider
import voice.common.DARK_THEME_SETTABLE
import voice.common.DispatcherProvider
import voice.common.MainScope
import voice.common.autoSleepTimer.AutoSleepTimer
import voice.common.grid.GridCount
import voice.common.grid.GridMode
import voice.common.navigation.Destination
import voice.common.navigation.Navigator
import voice.common.pref.AutoRewindAmountStore
import voice.common.pref.AutoSleepTimerStore
import voice.common.pref.DarkThemeStore
import voice.common.pref.GridModeStore
import voice.common.pref.SeekTimeStore
import java.time.LocalTime
import javax.inject.Inject

class SettingsViewModel
@Inject constructor(
  @DarkThemeStore
  private val useDarkThemeStore: DataStore<Boolean>,
  @AutoRewindAmountStore
  private val autoRewindAmountStore: DataStore<Int>,
  @SeekTimeStore
  private val seekTimeStore: DataStore<Int>,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @AutoSleepTimerStore
  private val autoSleepTimer: DataStore<AutoSleepTimer>,
  private val gridCount: GridCount,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)

  @Composable
  fun viewState(): SettingsViewState {
    val useDarkTheme by remember { useDarkThemeStore.data }.collectAsState(initial = false)
    val autoRewindAmount by remember { autoRewindAmountStore.data }.collectAsState(initial = 0)
    val seekTime by remember { seekTimeStore.data }.collectAsState(initial = 0)
    val gridMode by remember { gridModeStore.data }.collectAsState(initial = GridMode.GRID)
    val autoSleepTimer by remember { autoSleepTimer.data }.collectAsState(
      initial = AutoSleepTimer(
        enabled = false,
        startTime = LocalTime.of(22, 0),
        endTime = LocalTime.of(6, 0),
      ),
    )
    return SettingsViewState(
      useDarkTheme = useDarkTheme,
      showDarkThemePref = DARK_THEME_SETTABLE,
      seekTimeInSeconds = seekTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      useGrid = when (gridMode) {
        GridMode.LIST -> false
        GridMode.GRID -> true
        GridMode.FOLLOW_DEVICE -> gridCount.useGridAsDefault()
      },
      autoSleepTimer = autoSleepTimer,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun toggleDarkTheme() {
    mainScope.launch {
      useDarkThemeStore.updateData { !it }
    }
  }

  override fun toggleGrid() {
    mainScope.launch {
      gridModeStore.updateData { currentMode ->
        when (currentMode) {
          GridMode.LIST -> GridMode.GRID
          GridMode.GRID -> GridMode.LIST
          GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
            GridMode.LIST
          } else {
            GridMode.GRID
          }
        }
      }
    }
  }

  override fun seekAmountChanged(seconds: Int) {
    mainScope.launch {
      seekTimeStore.updateData { seconds }
    }
  }

  override fun onSeekAmountRowClick() {
    dialog.value = SettingsViewState.Dialog.SeekTime
  }

  override fun autoRewindAmountChang(seconds: Int) {
    mainScope.launch {
      autoRewindAmountStore.updateData { seconds }
    }
  }

  override fun onAutoRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.AutoRewindAmount
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun getSupport() {
    navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/q-a"))
  }

  override fun suggestIdea() {
    navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/ideas"))
  }

  override fun openBugReport() {
    val url = "https://github.com/PaulWoitaschek/Voice/issues/new".toUri()
      .buildUpon()
      .appendQueryParameter("template", "bug.yml")
      .appendQueryParameter("version", appInfoProvider.versionName)
      .appendQueryParameter("androidversion", Build.VERSION.SDK_INT.toString())
      .appendQueryParameter("device", Build.MODEL)
      .toString()
    navigator.goTo(Destination.Website(url))
  }

  override fun openTranslations() {
    dismissDialog()
    navigator.goTo(Destination.Website("https://hosted.weblate.org/engage/voice/"))
  }

  override fun setAutoSleepTimer(checked: Boolean) {
    mainScope.launch {
      autoSleepTimer.updateData { currentPrefs ->
        currentPrefs.copy(enabled = checked)
      }
    }
  }

  override fun setAutoSleepTimerStart(
    hour: Int,
    minute: Int,
  ) {
    val time = LocalTime.of(hour, minute)
    mainScope.launch {
      autoSleepTimer.updateData { currentPrefs ->
        currentPrefs.copy(startTime = time)
      }
    }
  }

  override fun setAutoSleepTimerEnd(
    hour: Int,
    minute: Int,
  ) {
    val time = LocalTime.of(hour, minute)
    mainScope.launch {
      autoSleepTimer.updateData { currentPrefs ->
        currentPrefs.copy(endTime = time)
      }
    }
  }
}
