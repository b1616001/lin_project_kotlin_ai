package com.example.lin_new_project_kotlin_ai

import com.example.lin_new_project_kotlin_ai.`fun`.NotebookFun
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class NotebookFunTest {

    @Test
    fun testNoteItemCreation() {
        val dummyFile = File("test_note.txt")
        val item = NotebookFun.NoteItem(
            name = "test_note.txt",
            size = 1024L,
            lastModified = 1700000000000L,
            file = dummyFile
        )

        assertEquals("test_note.txt", item.name)
        assertEquals(1024L, item.size)
        assertEquals(1700000000000L, item.lastModified)
        assertNotNull(item.file)
    }

    @Test
    fun testDefaultFolderName() {
        assertEquals("Fiction", NotebookFun.fileName)
    }
}
