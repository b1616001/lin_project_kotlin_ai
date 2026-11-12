package com.example.lin_new_project_kotlin_ai

import com.example.lin_new_project_kotlin_ai.`fun`.UrlUtilsMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilsMethodTest {

    @Test
    fun testExtractLinks_urls() {
        val text = "請造訪 https://github.com 和 http://example.com/test?page=1"
        val links = UrlUtilsMethod.extractLinks(text)

        assertEquals(2, links.size)
        assertEquals("https://github.com", links[0].originalText)
        assertEquals(UrlUtilsMethod.LinkType.WEB, links[0].type)
        assertEquals("http://example.com/test?page=1", links[1].originalText)
        assertEquals(UrlUtilsMethod.LinkType.WEB, links[1].type)
    }

    @Test
    fun testExtractLinks_emailAndPhone() {
        val text = "聯絡信箱: test@example.com，客服電話: 0912-345-678"
        val links = UrlUtilsMethod.extractLinks(text)

        assertTrue(links.any { it.type == UrlUtilsMethod.LinkType.EMAIL && it.targetUrl == "mailto:test@example.com" })
        assertTrue(links.any { it.type == UrlUtilsMethod.LinkType.PHONE && it.originalText == "0912-345-678" })
    }

    @Test
    fun testExtractLinks_empty() {
        val links = UrlUtilsMethod.extractLinks("")
        assertTrue(links.isEmpty())
    }
}
