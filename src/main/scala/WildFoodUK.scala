import java.io.{File, PrintWriter}
import java.net.{URLEncoder, URI}
import javax.imageio.ImageIO

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.ClientContext
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import org.apache.http.protocol.BasicHttpContext
import org.openqa.selenium.By
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import scala.collection.JavaConversions._

/**
 * Created by szymonwartak on 23/10/15.
 */
object WildFoodUK extends App {
  val baseDir = "data"
  val driver = new HtmlUnitDriver()
  "http://www.wildfooduk.com/mushroom-guides"
  val mushroomUrl = "http://www.wildfooduk.com/mushroom-guides/"
  val mushroomUrlR = "http://www.wildfooduk.com/mushroom-guides/([^/]+).*".r
  driver.get(mushroomUrl)
  val mushroomUrls = driver.findElementsByClassName("mushroom_name").flatMap(x => try { Some(x.findElement(By.cssSelector("a")).getAttribute("href")) } catch { case _ => None }).toSet
  mushroomUrls.take(3).foreach(getMushroom)

  def getMushroom(mushroomUrl: String) {
//    val mushroomUrl = mushroomUrls.head
    val dirName = mushroomUrl match { case mushroomUrlR(name) => name }
    val mushroomDir = s"$baseDir/$dirName"
    new File(mushroomDir).mkdir()

    driver.get(mushroomUrl)
    val main = driver.findElementById("mainbox")

    val name = main.findElement(By.cssSelector("h1")).getText
    val List(imgTable, detailsTable) = main.findElements(By.cssSelector("table")).toList

    val fullImgR = """.*cropped/([^/]+\.[a-z]+)""".r
    imgTable.
      findElements(By.cssSelector("img")).
      map(_.getAttribute("src")).
      filter(_.matches(fullImgR.toString)).
      filter{ x =>
        val imgName = x match { case fullImgR(name) => name }
//        println(s"saving ${x.replaceAll(" ", "%20")} to $mushroomDir/$imgName")
//        true
        !saveImage(new URI(x.replaceAll(" ", "%20")), new File(s"$mushroomDir/$imgName"))
      }.
      map(x => s"failed! $mushroomUrl - image:$x") foreach println

    val SEP = "\t"
    val details = detailsTable.findElements(By.cssSelector("tr")).
      map { tr =>
        tr.findElements(By.cssSelector("td")).
          filter(_.getText.trim != "").
          map(_.getText).mkString(SEP)
      }.
      filter(_.size > 0)
    val detailsWriter = new PrintWriter(new File(s"$mushroomDir/details"))
    detailsWriter.println(s"name\t$name")
    details.foreach(detailsWriter.println)
    detailsWriter.close()
  }

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
  //    val url = new URI("http://www.wildfooduk.com/uploads/mushrooms/29/cropped/Horse%20Mushroom.jpg")
  //    val file = new File("downloaded.jpg")
  // is false?
  //    saveImage(url, file)

}
