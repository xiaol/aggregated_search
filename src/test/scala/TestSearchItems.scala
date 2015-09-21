
// Created by ZG on 15/9/18.
//

import cores.{SearchItems, SearchItem}
import utils.Base64Utils

object TestSearchItems extends App{

  val urlList = "aHR0cDovL25ld3Muc2luYS5jb20uY24vby8yMDE1LTA4LTA5L2RvYy1pZnhmdGtwczM2OTEzOTAuc2h0bWwmJmh0dHA6Ly9uZXdzLjE2My5jb20vMTUvMDgwOS8wNi9CMElBVTVMRzAwMDE0QUVELmh0bWwmJmh0dHA6Ly9uZXdzLnFxLmNvbS9hLzIwMTUwODA4LzAwMzQ5My5odG0mJmh0dHA6Ly9uZXdzLnNvaHUuY29tLzIwMTUwODA4L240MTg0Mjk5NDcuc2h0bWwmJmh0dHA6Ly9uZXdzLnFxLmNvbS9hLzIwMTUwODA4LzAwMjI5NC5odG0mJmh0dHA6Ly9uZXdzLjE2My5jb20vMTUvMDgwOC8wNi9CMEZQSjdRTzAwMDE0QUVELmh0bWwmJmh0dHA6Ly9uZXdzLjE2My5jb20vcGhvdG92aWV3LzAwQVAwMDAxLzk1NjU2Lmh0bWwmJmh0dHA6Ly9uZXdzLjE2My5jb20vMTUvMDgwNy8yMC9CMEVNTTJOUTAwMDExMjRKLmh0bWwmJmh0dHA6Ly9uZXdzLjE2My5jb20vMTUvMDgwNy8xOS9CMEVLRk5IUjAwMDExMjRKLmh0bWwmJmh0dHA6Ly9uZXdzLjE2My5jb20vMTUvMDgwOC8wMy9CMEZESEdINzAwMDE0QUVELmh0bWw"

//  println(Base64Utils.DecodeBase64(urlList).split("&&").foreach{url => SearchItem(url,"")})
  val searchItems = for(url <- Base64Utils.DecodeBase64(urlList).split("&&")) yield SearchItem(url,"")

  println(SearchItems({for(url <- Base64Utils.DecodeBase64(urlList).split("&&")) yield SearchItem(url,"")}.toList))

}
