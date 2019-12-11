@file:JvmName("WebGenerator")

package thedarkcolour.kotlinforforge.webgenerator

import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import java.io.File
import java.nio.charset.Charset

fun main() = run()

fun run() {
    // val v = Files.newDirectoryStream(File("C:\\Things\\mods\\thedarkcolour.kotlinforforge\\thedarkcolour\\thedarkcolour.kotlinforforge").toPath())
    // val mavenMetadata = File("C:\\Things\\mods\\thedarkcolour.kotlinforforge\\thedarkcolour\\thedarkcolour.kotlinforforge\\maven-metadata.xml")

    //val webHtml = Jsoup.parse(File("..\\KotlinForForge\\thedarkcolour\\thedarkcolour.kotlinforforge\\web.html"), null).childNodes()[0]
    //webHtml.childNodes()[2].childNodes()[5].childNodes().filterIsInstance<Element>().forEach(::println)

    val thedarkcolour = File("C:\\Things\\mods\\KotlinForForge\\thedarkcolour")

    val web = File("C:\\Things\\mods\\KotlinForForge\\thedarkcolour\\web.html")
    val webHtml = Jsoup.parse(web, "UTF-8")

    for (file in thedarkcolour.listFiles()!!) {
        if (file.isDirectory) {
            val pre = webHtml.getElementsByAttributeValue("href", "../index.html")
                    .parents()
                    .first()
                    val attr = Attributes().put(Attribute("href", file.absolutePath.replace("${thedarkcolour.absolutePath}\\", "") + "/web.html"))

            if (pre.getElementsByAttributeValue("href", attr.get("href")).isEmpty()) {
                pre.appendChild(Element(Tag.valueOf("a"), webHtml.baseUri(), attr))

                val innerWeb = File("${file.absolutePath}\\web.html")
                innerWeb.createNewFile()
            }
            //webHtml.allElements.find {
            //    it.tagName() == "body"
            //}!!.allElements.find {
            //    it.tagName() == "pre"
            //}!!.allElements.find {
            //    print(it.attr("href"))
            //    it.className() == ""
            //}//.appendChild((Element(Tag.valueOf("a"), "hi")))
        }
    }

    FileUtils.writeStringToFile(web, webHtml.outerHtml(), Charset.defaultCharset())

    /*
    <body>
     <h1>Index of /thedarkcolour.kotlinforforge/</h1>
     <hr>
     <pre><a href="../web.html">../</a>
    <a href="1.0.0/web.html">1.0.0</a>
    <a href="maven-metadata.xml">maven-metadata.xml</a>
    <a href="maven-metadata.xml.md5">maven-metadata.xml.md5</a>
    <a href="maven-metadata.xml.sha1">maven-metadata.xml.sha1</a>
    </pre>
     <hr>
    </body>
     */
}

fun getPre(doc: Document): Element {
    return doc.getElementsByAttributeValue("href", "../web.html")
            .parents()
            .first() ?: doc.getElementsByAttributeValue("href", "../index.html")
            .parents()
            .first()
}