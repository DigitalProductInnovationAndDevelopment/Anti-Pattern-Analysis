package com.github.melisunsal.detectionplugin

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.*
import java.io.BufferedReader
import java.io.InputStreamReader
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

        val iconUrl = this::class.java.getResource("/icons/play-button-green-icon.png")
        val icon = ImageIcon(ImageIcon(iconUrl).image.getScaledInstance(20,20, Image.SCALE_DEFAULT))
        val runButton = JButton("Run Analysis", icon)
        runButton.preferredSize = Dimension(250, 50) // Set the width to 200 and the height to 50
        runButton.setFont(Font("Arial", Font.BOLD, 14))
        panel.add(runButton)

        val emptyLabel = JLabel(" ")
        emptyLabel.setFont(Font("Arial", Font.PLAIN, 20)) // Change the number to adjust the size
        panel.add(emptyLabel)

        val outputArea = JTextArea(10, 50)
        outputArea.isEditable = false
        outputArea.lineWrap = true
        outputArea.wrapStyleWord = true
        outputArea.border = JBUI.Borders.empty(10)
        val scrollPane = JBScrollPane(outputArea)
        scrollPane.border = JBUI.Borders.empty(10)
        panel.add(scrollPane)

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
