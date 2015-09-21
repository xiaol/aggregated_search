package utils

// Created by ZG on 15/9/10.
// 

import java.util.Calendar
import java.text.SimpleDateFormat

object DateUtils {

  def getCurrentDate: String = getCurrentDateTime("YYYY-MM-dd HH:mm:ss")

  def getCurrentDay: String = getCurrentDateTime("YYYY-MM-dd")

  def getCurrentTime: String = getCurrentDateTime("HH:mm:ss")

  private def getCurrentDateTime(dateTimeFormat: String): String = {
    val dateFormat = new SimpleDateFormat(dateTimeFormat)
    val cal = Calendar.getInstance()
    dateFormat.format(cal.getTime)
  }
}