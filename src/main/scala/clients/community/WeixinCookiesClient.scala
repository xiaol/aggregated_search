package clients.community

// Created by ZG on 15/9/21.
//

import akka.actor.Actor
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import akka.event.Logging
import redis.RedisClients
import utils.ExceptionHandler.safely

case object GetWeixinCookie

class WeixinCookiesClient extends Actor{

  implicit val system = context.system
  val log = Logging(context.system, this)

  val weiboApi = "https://login.weibo.cn/login/?backURL=http%3A%2F%2Fm." +
    "weibo.cn%2F&backTitle=%E6%89%8B%E6%9C%BA%E6%96%B0%E6%B5%AA%E7%BD%91&vt=4"

  def receive = {
    case GetWeiboCookie => sender ! Cookie(getCookies)
  }

  def createCookies():String = {
    log.error("Starting driver...")

    var cook = ""

    val capabilities:DesiredCapabilities = DesiredCapabilities.htmlUnit()
    capabilities.setBrowserName("Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J)")
    capabilities.setVersion("4.0")
    capabilities.setJavascriptEnabled(false)
    val driver:WebDriver = new HtmlUnitDriver(capabilities)

    try{
      driver.get(weiboApi)
      driver.findElement(By.name("mobile")).sendKeys("13466670915")
      driver.findElement(By.xpath("//*[@type=\"password\"]")).sendKeys("aini580qo0o")
      driver.findElement(By.name("submit")).click()
      val cookies = driver.manage().getCookies

      cookies.toArray.foreach(c => if(c.toString.startsWith("SUB=")){cook=c.toString.split(" ").head})
      log.error(s"Successful, driver get cookie: $cook")
    } catch safely{
      case ex: Throwable => log.error(s"Faield, driver get cookie field with Exception: $ex")
    } finally {
      Thread.sleep(2000)
      driver.quit()
    }
    cook
  }

  def getCookies:String = {
    var cook = ""
    val cookies = RedisClients.getSet("weibo:cookies")
    if(cookies.isEmpty){
      log.error("No cookies found in cache, create one.")
      cook = createCookies()
      if(cook.nonEmpty){
        setCookies(cook)
      }
    }else{
      cook = cookies.get
      log.error(s"Found cookies in cache: $cook")
    }

    cook
  }

  def setCookies(cookies:String):Unit = {
    RedisClients.setSet("weibo:cookies", cookies, 60*60*3)
  }

}
