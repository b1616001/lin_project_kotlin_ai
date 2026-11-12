package com.example.lin_new_project_kotlin_ai.`fun`

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Browser
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import java.util.regex.Pattern

/**
 * 連結識別、解析、高亮與點擊處理工具
 */
object UrlUtilsMethod {

    enum class LinkType {
        WEB,
        EMAIL,
        PHONE,
        OTHER
    }

    data class ExtractedLink(
        val originalText: String,
        val targetUrl: String,
        val type: LinkType,
        val start: Int,
        val end: Int
    )

    // 支援 http, https, ftp, svn, 以及 www. 開頭之網址
    private val URL_PATTERN = Pattern.compile(
        "\\b((?:https?|ftp|svn)://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        Pattern.CASE_INSENSITIVE
    )

    // Email 格式
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"
    )

    // 電話號碼格式 (7至15碼數字，支援連字號與括弧)
    private val PHONE_PATTERN = Pattern.compile(
        "(?:\\+?\\d{1,3}[- ]?)?\\(?\\d{2,4}\\)?[- ]?\\d{3,4}[- ]?\\d{3,4}"
    )

    /**
     * 讓 TextView 自動解析 URL、Email、電話並高亮設置點擊連結
     */
    fun handleText(
        tv: TextView,
        content: String,
        linkColor: Int = Color.parseColor("#03DAC5"),
        onLinkClick: ((url: String, type: LinkType) -> Unit)? = null
    ): TextView {
        val sp = SpannableStringBuilder(content)
        val links = extractLinks(content)

        for (link in links) {
            val span = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (onLinkClick != null) {
                        onLinkClick(link.targetUrl, link.type)
                    } else {
                        openLinkSafely(widget.context, link.targetUrl, link.type)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = linkColor
                    ds.isUnderlineText = true
                }
            }
            if (link.start >= 0 && link.end <= sp.length && link.start < link.end) {
                sp.setSpan(span, link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        tv.text = sp
        tv.movementMethod = LinkMovementMethod.getInstance()
        return tv
    }

    /**
     * 處理 HTML 資料的高亮與回應
     */
    fun handleHtmlText(
        tv: TextView,
        content: String?,
        linkColor: Int = Color.parseColor("#03DAC5"),
        onLinkClick: ((url: String, type: LinkType) -> Unit)? = null
    ): TextView {
        if (content.isNullOrEmpty()) {
            tv.text = ""
            return tv
        }
        val spanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val sp = SpannableStringBuilder(spanned)
        val urlSpans = sp.getSpans(0, sp.length, URLSpan::class.java)

        for (urlSpan in urlSpans) {
            val start = sp.getSpanStart(urlSpan)
            val end = sp.getSpanEnd(urlSpan)
            val url = urlSpan.url ?: continue

            val span = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (onLinkClick != null) {
                        onLinkClick(url, LinkType.WEB)
                    } else {
                        openLinkSafely(widget.context, url, LinkType.WEB)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = linkColor
                    ds.isUnderlineText = true
                }
            }

            sp.removeSpan(urlSpan)
            sp.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tv.text = sp
        tv.movementMethod = LinkMovementMethod.getInstance()
        return tv
    }

    /**
     * 從字串中萃取所有可識別的連結清單
     */
    fun extractLinks(content: String): List<ExtractedLink> {
        val result = mutableListOf<ExtractedLink>()
        if (content.isEmpty()) return result

        // 1. 搜尋 URL
        val urlMatcher = URL_PATTERN.matcher(content)
        while (urlMatcher.find()) {
            val raw = urlMatcher.group()
            val target = if (raw.startsWith("www.", ignoreCase = true)) {
                "https://$raw"
            } else {
                raw
            }
            result.add(
                ExtractedLink(
                    originalText = raw,
                    targetUrl = target,
                    type = LinkType.WEB,
                    start = urlMatcher.start(),
                    end = urlMatcher.end()
                )
            )
        }

        // 2. 搜尋 Email
        val emailMatcher = EMAIL_PATTERN.matcher(content)
        while (emailMatcher.find()) {
            val raw = emailMatcher.group()
            val overlap = result.any { it.start <= emailMatcher.start() && it.end >= emailMatcher.end() }
            if (!overlap) {
                result.add(
                    ExtractedLink(
                        originalText = raw,
                        targetUrl = "mailto:$raw",
                        type = LinkType.EMAIL,
                        start = emailMatcher.start(),
                        end = emailMatcher.end()
                    )
                )
            }
        }

        // 3. 搜尋 電話號碼 (至少 7 個數字)
        val phoneMatcher = PHONE_PATTERN.matcher(content)
        while (phoneMatcher.find()) {
            val raw = phoneMatcher.group().trim()
            val digitsCount = raw.count { it.isDigit() }
            if (digitsCount in 7..15) {
                val overlap = result.any {
                    (it.start < phoneMatcher.end() && it.end > phoneMatcher.start())
                }
                if (!overlap) {
                    result.add(
                        ExtractedLink(
                            originalText = raw,
                            targetUrl = "tel:${raw.replace(" ", "").replace("-", "")}",
                            type = LinkType.PHONE,
                            start = phoneMatcher.start(),
                            end = phoneMatcher.end()
                        )
                    )
                }
            }
        }

        return result.sortedBy { it.start }
    }

    /**
     * 安全開啟連結
     */
    fun openLinkSafely(context: Context, url: String, type: LinkType = LinkType.WEB) {
        try {
            val uri = Uri.parse(url)
            val intent = when (type) {
                LinkType.EMAIL -> Intent(Intent.ACTION_SENDTO, uri)
                LinkType.PHONE -> Intent(Intent.ACTION_DIAL, uri)
                else -> {
                    val viewIntent = Intent(Intent.ACTION_VIEW, uri)
                    viewIntent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
                    viewIntent
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "無法開啟連結: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
