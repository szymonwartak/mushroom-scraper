import java.io.{IOException, File}
import javax.imageio.ImageIO

import org.apache.http.{HttpStatus, HttpResponse}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.ClientContext
import org.apache.http.impl.client.{DefaultHttpClient, BasicCookieStore}
import org.apache.http.protocol.BasicHttpContext
import org.openqa.selenium.By
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import collection.JavaConversions._
import java.net.{URI, URL}

/**
 * Created by szymonwartak on 23/10/15.
 */
object WildFoodUK extends App {
  val driver = new HtmlUnitDriver()
  val mushroomUrl = "http://www.wildfooduk.com/mushroom-guides/horse-mushroom-mushroom/"
  getMushroom(mushroomUrl)

  def getMushroom(mushroomUrl: String) {
    driver.get(mushroomUrl)
    val main = driver.findElementById("mainbox")

    val name = main.findElement(By.cssSelector("h1")).getText
    val List(imgTable, detailsTable) = main.findElements(By.cssSelector("table")).toList

    val fullImgR = """.*cropped/[^/]+\.[a-z]+"""
    imgTable.findElements(By.cssSelector("img")).map(_.getAttribute("src")).filter(_.matches(fullImgR)).map(x => new URL(x))

    val url = new URI("http://www.wildfooduk.com/uploads/mushrooms/29/cropped/Horse%20Mushroom.jpg")
    val file = new File("downloaded.jpg")
    // is false?
    saveImage(url, file)

    val httpClient = new DefaultHttpClient()
    val localContext = new BasicHttpContext()
    val cookieStore = new BasicCookieStore()
    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore)
    def saveImage(uri: URI, file: File) = {
      val httpGet = new HttpGet(uri)
      val httpResponse = httpClient.execute(httpGet, localContext)
      val image = ImageIO.read(httpResponse.getEntity.getContent)
      ImageIO.write(image, "jpg", file)
    }
    
    val SEP = "\t"
    val details = detailsTable.findElements(By.cssSelector("tr")).map { tr => tr.findElements(By.cssSelector("td")).map(_.getText).mkString(SEP)}
  }
}
