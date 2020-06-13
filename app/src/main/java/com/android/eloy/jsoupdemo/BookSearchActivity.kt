package com.android.eloy.jsoupdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.eloy.jsoupdemo.reader.BookFileManager
import com.android.eloy.jsoupdemo.reader.OnPageStateChangedListener
import com.android.eloy.jsoupdemo.reader.ReadView
import com.android.eloy.jsoupdemo.reader.response.Chapter
import com.android.eloy.jsoupdemo.reader.util.FileUtils
import com.qy.reader.crawler.xpath.exception.XpathSyntaxErrorException
import com.qy.reader.crawler.xpath.model.JXDocument
import com.qy.reader.crawler.xpath.model.JXNode
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URLEncoder

class BookSearchActivity : AppCompatActivity() {

    private val mReadView : ReadView by lazy {
        findViewById<ReadView>(R.id.readview)
    }

    private var mBookCategoryList : List<Chapter> ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileUtils.context = App.getContext()
        setContentView(R.layout.activity_booksearch)

        //init readerview

        mReadView.setOnPageStateChangedListener(object : OnPageStateChangedListener {
            override fun onPageChanged(currentPage: Int, currentChapter: Int) {
                Log.e("test", "onpagechanged currentpage $currentPage currentChapter $currentChapter")
            }

            override fun onChapterChanged(
                currentChapter: Int,
                fromChapter: Int,
                fromUser: Boolean
            ) {
                Log.e("test", "onchapterchanged $currentChapter $fromChapter $fromUser")
            }

            override fun onCenterClick() {
                Log.e("test", "oncenterclick")
            }

            override fun onChapterLoadFailure(targetChapter: Int, currentChapter: Int, chapters: List<Chapter>) {
                searchBookContent(targetChapter, chapters) {
                    if (currentChapter == targetChapter + 1) {
                        //open pre
                        mReadView.preChapter()
                    }else if (currentChapter == targetChapter -1){
                        //open next
                        mReadView.nextChapter()
                    }
                }
            }
        })
    }

    fun searchBook(searchkey: String = "大魏宫廷") {
        Thread {

            //八一中文网
            var url = "https://www.zwdu.com/search.php?keyword=%s"

            url = String.format(url, URLEncoder.encode(searchkey, "UTF-8"))

            val connect: Connection = Jsoup.connect(url)

//            connect.header("Referer", getRefer())

            try {
                val document = connect.validateTLSCertificates(false).get()

                val jxDocument = JXDocument(document)

//                Log.e("test", "jxDocument $document")

                //get book info
                val list = jxDocument.selN("//div[@class='result-item result-game-item']")

//                Log.e("test", "result content $list")

                val content = list[0]

                //parse book category info author,title,desc,link,img
                val author = getNodeStr(
                    content,
                    "//div[@class='result-game-item-info']//p[1]/span[2]/text()"
                )
                val title = getNodeStr(
                    content,
                    "//h3[@class='result-item-title result-game-item-title']//a/@title"
                )
                val desc = getNodeStr(content, "//p[@class='result-game-item-desc']/text()")
                val link = getNodeStr(
                    content,
                    "//h3[@class='result-item-title result-game-item-title']//a/@href"
                )
                val img = getNodeStr(content, "//div[@class='result-game-item-pic']//a//img/@src")

                Log.e("test", "author $author title $title desc $desc link $link img $img")

                //load book page
                searchBookCategory(link!!)

//                list.forEach {
//                    val imgList = getNodeList(it, "//img[@class='lazy']/@data-original")
//                    imgList?.let { it1 -> result.addAll(it1) }
//                }

            } catch (e: Exception) {
                Log.e("test", "exception $e")
            }


        }.start()
    }

    fun searchBookCategory(link: String) {
        val connect: Connection = Jsoup.connect(link)
        val document = connect.validateTLSCertificates(false).get()
        val jxDocument = JXDocument(document)
        val list = jxDocument.selN("//div[@id=list]//dl")
        val content = list[0]

        val resultList = getCatItem(content, link)

        resultList?.forEach {
            //每个章节详情
//            Log.e("test", "item ${it.toString()}")
        }

        //抓取单个章节数据

        mBookCategoryList = resultList

        searchBookContent(0, resultList!!)
    }

    private fun searchBookContent(index: Int, bookCategoryList : List<Chapter>, listener : (()->Unit) ?=null) {
        //设置几种方案，网站schema+章节link, 查找到的link+章节link
        //方式一

        Thread{
            val item = bookCategoryList[index]
            val url = "https://www.zwdu.com" + item.link

            val connect: Connection = Jsoup.connect(url)
            val document = connect.validateTLSCertificates(false).get()
            val jxDocument = JXDocument(document)

            val list = jxDocument.selN("//div[@id=content]/text()")

            val content = list[0]

            BookFileManager.getInstance().saveContentFile(item.sourceKey, "0", item.title, content.toString())

            mReadView.initChapterList(item.sourceKey, "0", bookCategoryList, index + 1, 1)

            listener?.invoke()

            Log.e("test", "小说// $content")

        }.start()

    }

    private fun getCatItem(startNode: Any, baselink: String): List<Chapter>? {

        val xpath = "//dd//a"

        try {

            val selectList = if (startNode is JXDocument) {
                startNode.sel(xpath)
            } else if (startNode is JXNode) {
                startNode.sel(xpath)
            } else {
                null
            }

            val list = mutableListOf<Chapter>()

            selectList?.let {
                for (item in it) {
                    if (item is JXNode) {
                        val link = item.sel("/@href")[0]
                        val title = item.sel("/text()")
                        list.add(Chapter("key1",title.toString(), link.toString(),baselink))
                    }
                }
            }

            return list

        } catch (e: XpathSyntaxErrorException) {
            return null
        }
    }

    private fun getNodeStr(startNode: Any, xpath: String): String? {
        val rs = StringBuilder()
        try {
            val list: List<*>
            list = if (startNode is JXDocument) {
                startNode.sel(xpath)
            } else if (startNode is JXNode) {
                startNode.sel(xpath)
            } else {
                return ""
            }
            for (node in list) {
                rs.append(node.toString())
            }
        } catch (e: XpathSyntaxErrorException) {
        }
        return rs.toString()
    }


    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.searchbook -> {
                searchBook()
            }
        }
    }
}