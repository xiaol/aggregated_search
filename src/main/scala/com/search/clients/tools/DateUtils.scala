package com.search.clients.tools

// Created by ZG on 15/8/3.
// 

import java.util.Calendar
import java.text.SimpleDateFormat

object DateUtils {

  def getCurrentDate: String = getCurrentDateTime("YYYY-MM-dd HH:mm:ss")

  def getCurrentTime: String = getCurrentDateTime("HH:mm:ss")

  private def getCurrentDateTime(dateTimeFormat: String): String = {
    val dateFormat = new SimpleDateFormat(dateTimeFormat)
    val cal = Calendar.getInstance()
    dateFormat.format(cal.getTime)
  }
}
