package com.github.melisunsal.detectionplugin
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.*

class AntiPatternToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val infoLabel = JLabel("<html><h2>Anti Pattern Analysis</h2>" +
                "<p>This plugin detects various anti-patterns in your Java code.</p>" +
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
