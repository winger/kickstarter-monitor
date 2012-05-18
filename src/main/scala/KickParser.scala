import java.io.File
import java.net.URL
import java.util.concurrent.{TimeUnit, Executors}
import javax.sound.sampled.{Clip, DataLine, AudioSystem}
import xml._

object KickParser extends App {
  val htmlParser = new HTML5Parser()

  def hasClass(node: Node, cls: String) = {
    node.attribute("class").map(_.text.split(" ").contains(cls)).getOrElse(false)
  }

  val sound = {
    val stream = AudioSystem.getAudioInputStream(new File("alarm.wav"))
    val format = stream.getFormat
    val info = new DataLine.Info(classOf[Clip], format)
    val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
    clip.open(stream)
    clip
  }
  
  def alarm() {
    sound.setFramePosition(0)
    sound.start()
  }

  def checkPage() {
    val query = new URL("http://www.kickstarter.com/projects/552506690/brydge-ipad-do-more")

    val page = htmlParser.load(query.openStream())

    val rewards = (page \\ "div").filter(hasClass(_, "NS-projects-reward"))
    val reward = rewards.find(div => div \ "h3" \ "span" contains <span>Pledge $180 or more</span>)
    reward match {
      case None => {
        println("Not found!")
        alarm()
      }
      case Some(div) =>
        if (hasClass(div, "clickable")) {
          println("Available!")
          alarm()
        } else {
          println("Still not available :(")
        }
    }
  }

  val executor = Executors.newSingleThreadScheduledExecutor()
  executor.scheduleAtFixedRate(new Runnable { def run() { checkPage() } }, 0, 5, TimeUnit.SECONDS)
}