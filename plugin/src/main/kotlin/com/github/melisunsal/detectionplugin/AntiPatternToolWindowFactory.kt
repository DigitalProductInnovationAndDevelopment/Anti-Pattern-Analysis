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
import javax.swing.*

class AntiPatternToolWindowFactory : ToolWindowFactory, DumbAware {
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
        val outputArea = JTextArea(10, 50)
        outputArea.isEditable = false
        outputArea.lineWrap = true
        outputArea.selectionColor = JBColor.YELLOW
        outputArea.wrapStyleWord = true
        outputArea.border = JBUI.Borders.empty(10)
        val scrollPane = JBScrollPane(outputArea)
        scrollPane.border = JBUI.Borders.empty(10)
        panel.add(scrollPane, gbc)

        runButton.addActionListener {
            runButton.isEnabled = false
            progressBar.isVisible = true
            progressBar.isIndeterminate = true
            // ------------ TEST CODE ------------
            try {
                outputArea.text =
                    "for (final var articleNumber : articleNumbers) {\n" +
                        "  getProductsByArticleNumber(articleNumber).forEach(dto -> productsById.put(dto.getId(), dto));\n" +
                    "  }"
            }
            catch (ex: Exception) {
                outputArea.text = "An error occurred: ${ex.message}"
                ex.printStackTrace()
            } finally {
                runButton.isEnabled = true
                progressBar.isVisible = false
                progressBar.isIndeterminate = false
            }
            // ------------ TEST CODE ------------

            /*SwingUtilities.invokeLater {
                try {
                    val process = Runtime.getRuntime().exec("java -jar /path/to/your/anti-pattern-detector.jar")
                    val reader = process.inputStream.bufferedReader()
                    val output = StringBuilder()
                    reader.forEachLine { line -> output.append(line).append("\n") }
                    outputArea.text = output.toString()
                } catch (ex: Exception) {
                    outputArea.text = "An error occurred: ${ex.message}"
                    ex.printStackTrace()
                } finally {
                    runButton.isEnabled = true
                    progressBar.isVisible = false
                    progressBar.isIndeterminate = false
                }
            }*/
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}