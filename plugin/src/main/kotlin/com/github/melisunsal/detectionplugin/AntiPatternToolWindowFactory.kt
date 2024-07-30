package com.github.melisunsal.detectionplugin

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.*
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.imageio.ImageIO
import javax.swing.*


class AntiPatternToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val titleLabel = JLabel("<html><h2>Anti Pattern Analysis</h2></html>")
        panel.add(titleLabel)

        val imageUrl = this::class.java.getResource("/icons/logo.png")
        val imageIcon = ImageIcon(imageUrl).image
        val resizedImage = imageIcon.getScaledInstance(100, 100, Image.SCALE_DEFAULT)
        val resizedIcon = ImageIcon(resizedImage)
        val imageLabel = JLabel(resizedIcon)
        panel.add(imageLabel)

        // Add information label
        val infoLabel = JLabel(
                "<html><p>This plugin detects various anti-patterns in your Java code.</p>" +
                "<p><strong>Detectable Anti-patterns:</strong></p>" +
                "<ul><li>For Loop Database Anti-Performance</li></ul></html>")
        panel.add(infoLabel)

        val runButton = JButton("Run Analysis")
        panel.add(runButton)

        val outputArea = JTextArea(10, 50)
        outputArea.isEditable = false
        panel.add(JScrollPane(outputArea))

        runButton.addActionListener {
            try {
                val process = Runtime.getRuntime().exec("java -jar /path/to/your/anti-pattern-detector.jar")
                val reader = process.inputStream.bufferedReader()
                val output = StringBuilder()
                reader.forEachLine { line -> output.append(line).append("\n") }
                outputArea.text = output.toString()
            } catch (ex: Exception) {
                outputArea.text = "An error occurred: ${ex.message}"
            }
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
