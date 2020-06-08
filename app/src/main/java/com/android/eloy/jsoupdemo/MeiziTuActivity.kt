package com.android.eloy.jsoupdemo

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sanwei.uikit.recyclerview.CommonAdapter
import com.android.sanwei.uikit.recyclerview.CommonViewHolder
import com.android.sanwei.uikit.recyclerview.loadmore.LoadMoreCallback
import com.android.sanwei.uikit.recyclerview.loadmore.OnLoadMoreListener
import com.android.sanwei.uikit.recyclerview.refresh.OnRefreshListener
import com.android.sanwei.uikit.recyclerview.refresh.RefreshCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.qy.reader.crawler.xpath.exception.XpathSyntaxErrorException
import com.qy.reader.crawler.xpath.model.JXDocument
import com.qy.reader.crawler.xpath.model.JXNode
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URI
import java.net.URISyntaxException


class MeiziTuActivity : AppCompatActivity() {

    private var position: Int = 1

    private var adapter: ImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = findViewById<RecyclerView>(R.id.list)

        list.layoutManager = (GridLayoutManager(this, 2))

        adapter = ImageAdapter()


        // load more
        adapter?.loadmoreModule?.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore(tag: String, callback: LoadMoreCallback) {
                loadMore(){
                    callback.onLoadComplete(tag)
                }
            }
        })

        //refresh
        adapter?.refreshModule?.setRefreshListener(object : OnRefreshListener {
            override fun onRefresh(tag: String, callback: RefreshCallback) {
                refresh(){
                    callback.onRefreshComplete(tag)
                }
            }
        })

        list.adapter = adapter

        adapter?.initialize()

        refresh()


    }

    fun loadMore(listener : (()-> Unit)?= null) {
        position++

        loadResource{
            adapter?.addData(it)
            position++
            listener?.invoke()
        }
    }

    fun refresh(listener : (()-> Unit)?= null) {
        position = 1

        loadResource{
            adapter?.setNewData(it)
            position++
            listener?.invoke()
        }
    }

    fun loadResource(function : (MutableList<String>?)-> Unit) {
        Thread {

            val connect: Connection = Jsoup.connect(getLoadUrl())

            connect.header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
            );
            connect.header("Accept-Encoding", "gzip, deflate, sdch");
            connect.header("Accept-Language", "zh-CN,zh;q=0.8");
            connect.header("Sec-Fetch-Dest", "document");
            connect.header("Upgrade-Insecure-Requests", "1");
            connect.header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36"
            )
            connect.header("Referer", getRefer())

            val result = mutableListOf<String>()
            try {
                val document = connect.get()

                val jxDocument = JXDocument(document)

                val list = jxDocument.selN("//div[@class='postlist']//ul")

                list.forEach {
                    val imgList = getNodeList(it, "//img[@class='lazy']/@data-original")
                    imgList?.let { it1 -> result.addAll(it1) }
                }

//                val imgs: Elements = document.getElementsByTag("img")
//
//                for (img in imgs) {
//                    val src = img.attr("data-original")
//
//                    if (!TextUtils.isEmpty(src)){
//                        result.add(src)
//                    }
//                    Log.e("test", "src $src")
//                }

                runOnUiThread {
                    function.invoke(result)

                }

            } catch (e: Exception) {
                function.invoke(null)
                Log.e("test", "exception $e")
            }


        }.start()
    }

    fun getLoadUrl(): String {
        return "https://www.mzitu.com/page/$position/";
    }

    fun getRefer(): String{
        if (position == 1){
            return "https://www.mzitu.com/"
        }
        return "https://www.mzitu.com/page/$position/"
    }

    @Throws(URISyntaxException::class)
    private fun urlVerification(
        link: String,
        linkWithHost: String
    ): String? {
        var link = link
        var linkWithHost = linkWithHost
        if (TextUtils.isEmpty(link)) {
            return link
        }
        if (link.startsWith("/")) {
            val original = URI(linkWithHost)
            val uri =
                URI(original.scheme, original.authority, link, null)
            link = uri.toString()
        } else if (!link.startsWith("http://") && !link.startsWith("https://")) {
            if (linkWithHost.endsWith("html") || linkWithHost.endsWith("htm")) {
                linkWithHost = linkWithHost.substring(0, linkWithHost.lastIndexOf("/") + 1)
            } else if (!linkWithHost.endsWith("/")) {
                linkWithHost = "$linkWithHost/"
            }
            link = linkWithHost + link
        }
        return link
    }

    private fun getNodeList(startNode: Any, xpath: String): List<String>? {

        try {

            val selectList =  if (startNode is JXDocument) {
                startNode.sel(xpath)
            } else if (startNode is JXNode) {
                startNode.sel(xpath)
            } else {
                null
            }

            val list = mutableListOf<String>()

            selectList?.let {
                for(item in it) {
                    list.add(item.toString())
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


    inner class ImageAdapter : CommonAdapter<String, CommonViewHolder>(
        R.layout.item_image,
        enableLoadMoreModule = true,
        enableRefreshModule = true
    ) {
        override fun convert(helper: CommonViewHolder, item: String?) {
            val image = helper.getView<ImageView>(R.id.image) ?: return
            val glideUrl = GlideUrl(item, LazyHeaders.Builder()
                .addHeader("Referer", getRefer())
                .addHeader("Sec-Fetch-Dest","image")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .build())
            Glide.with(image).load(glideUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(image)

        }

    }
}
