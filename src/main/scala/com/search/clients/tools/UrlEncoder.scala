package com.search.clients.tools

// Created by ZG on 15/7/20.
//

import java.net.URLEncoder

object UrlEncoder {
  def encode(str:String) = {
    URLEncoder.encode(str, "utf-8")
  }
}
