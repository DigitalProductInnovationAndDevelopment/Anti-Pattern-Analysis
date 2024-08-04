package com.github.melisunsal.detectionplugin

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.*

class AntiPatternToolWindowFactory : ToolWindowFactory, DumbAware {
    private val outputArea = JTextArea(10, 50)
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.insets = JBUI.insets(10)
        gbc.fill = GridBagConstraints.HORIZONTAL

        // Image first
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        val imageUrl = this::class.java.getResource("/icons/logo.png")
        val imageIcon = ImageIcon(imageUrl).image
        val resizedImage = imageIcon.getScaledInstance(100, 100, Image.SCALE_SMOOTH)
        val resizedIcon = ImageIcon(resizedImage)
        val imageLabel = JLabel(resizedIcon)
        panel.add(imageLabel, gbc)

        // Title next to image
        gbc.gridx = 1
        gbc.weightx = 1.0
        val titleLabel = JLabel("<html><h2>Anti Pattern Analysis</h2></html>")
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        panel.add(titleLabel, gbc)

        // Next row for information label
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        val infoLabel = JLabel(
            "<html><p>This plugin detects various anti-patterns in your Java code.</p>" +
                    "<p><strong>Detectable Anti-patterns:</strong></p>" +
                    "<ul><li>For Loop Database Anti-Performance</li></ul></html>"
        )
        panel.add(infoLabel, gbc)

        // Run button row
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 0.0
        val iconUrl = this::class.java.getResource("/icons/play-button-green-icon.png")
        val icon = ImageIcon(ImageIcon(iconUrl).image.getScaledInstance(20, 20, Image.SCALE_SMOOTH))
        val runButton = JButton("Run Analysis", icon)
        runButton.preferredSize = Dimension(300, 60)  // Increased button size
        runButton.minimumSize = Dimension(300, 60)    // Minimum size to ensure it doesn't shrink
        runButton.maximumSize = Dimension(300, 60)
        runButton.font = Font("Arial", Font.BOLD, 14)
        runButton.toolTipText = "Click to start anti-pattern analysis"
        panel.add(runButton, gbc)

        // Progress bar
        gbc.gridx = 1
        gbc.weightx = 1.0
        val progressBar = JProgressBar()
        progressBar.isVisible = false
        panel.add(progressBar, gbc)

        // Output area
        gbc.gridx = 0
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        outputArea.isEditable = false
        outputArea.lineWrap = true
        outputArea.selectionColor = JBColor.YELLOW
        outputArea.wrapStyleWord = true
        outputArea.border = JBUI.Borders.empty(10)
        val scrollPane = JBScrollPane(outputArea)
        scrollPane.border = JBUI.Borders.empty(10)
        panel.add(scrollPane, gbc)

        runButton.addActionListener {
            runAnalysis()
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun runAnalysis() {
        val jarResourcePath = "/jar/CodeOutput.jar"
        val tempDir = Files.createTempDirectory("antipattern-analysis")
        val tempJarPath = tempDir.resolve("CodeOutput.jar")

        try {
            // Copy the JAR from resources to a temporary file
            javaClass.getResourceAsStream(jarResourcePath).use { inputStream ->
                if (inputStream == null) {
                    outputArea.text = "Error: Could not find CodeOutput.jar in resources."
                    return
                }
                Files.copy(inputStream, tempJarPath, StandardCopyOption.REPLACE_EXISTING)
            }

            // Run the JAR
            val process = Runtime.getRuntime().exec("java -jar ${tempJarPath}")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()

            reader.forEachLine { line ->
                output.append(line).append("\n")
            }

            outputArea.text = "⁠  \n${output.toString()}\n  ⁠"
        } catch (e: Exception) {
            outputArea.text = "Error: ${e.message}"
        } finally {
            // Clean up: delete the temporary JAR file
            Files.deleteIfExists(tempJarPath)
            Files.deleteIfExists(tempDir)
        }
    }
}

