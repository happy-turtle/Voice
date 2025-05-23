package voice.settings

interface SettingsListener {
  fun close()
  fun toggleDarkTheme()
  fun toggleGrid()
  fun seekAmountChanged(seconds: Int)
  fun onSeekAmountRowClick()
  fun autoRewindAmountChang(seconds: Int)
  fun onAutoRewindRowClick()
  fun dismissDialog()
  fun getSupport()
  fun suggestIdea()
  fun openBugReport()
  fun openTranslations()
  fun setAutoSleepTimer(checked: Boolean)
  fun setAutoSleepTimerStart(
    hour: Int,
    minute: Int,
  )

  fun setAutoSleepTimerEnd(
    hour: Int,
    minute: Int,
  )
}
