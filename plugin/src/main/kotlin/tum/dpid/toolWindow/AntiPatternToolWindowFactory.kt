package tum.dpid.toolWindow

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
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.*

class AntiPatternToolWindowFactory : ToolWindowFactory, DumbAware {
    companion object {
        private const val JAR_RESOURCE_PATH = "/jar/CodeOutput.jar"
    }

    private val outputArea = JTextArea(10, 50).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        selectionColor = JBColor.YELLOW
        border = JBUI.Borders.empty(10)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = JBUI.insets(10)
            fill = GridBagConstraints.HORIZONTAL
        }

        addLogoAndTitle(panel, gbc)
        addInfoLabel(panel, gbc)
        addRunButton(panel, gbc)
        addOutputArea(panel, gbc)

        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun addLogoAndTitle(panel: JPanel, gbc: GridBagConstraints) {
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(createLogoLabel(), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(createTitleLabel(), gbc)
    }

    private fun createLogoLabel(): JLabel {
        val imageUrl = javaClass.getResource("/icons/logo.png")
        val imageIcon = ImageIcon(imageUrl)
        val resizedImage = imageIcon.image.getScaledInstance(100, 100, Image.SCALE_SMOOTH)
        return JLabel(ImageIcon(resizedImage))
    }

    private fun createTitleLabel(): JLabel {
        return JLabel("<html><h2>Anti Pattern Analysis</h2></html>").apply {
            font = Font("Arial", Font.BOLD, 18)
        }
    }

    private fun addInfoLabel(panel: JPanel, gbc: GridBagConstraints) {
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        panel.add(
            JLabel(
                "<html><p>This plugin detects various anti-patterns in your Java code.</p>" +
                        "<p><strong>Detectable Anti-patterns:</strong></p>" +
                        "<ul><li>For Loop Database Anti-Performance</li></ul></html>"
            ), gbc
        )
    }

    private fun addRunButton(panel: JPanel, gbc: GridBagConstraints) {
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.weightx = 0.0
        panel.add(createRunButton(), gbc)
    }

    private fun createRunButton(): JButton {
        val iconUrl = javaClass.getResource("/icons/play-button-green-icon.png")
        val icon = ImageIcon(ImageIcon(iconUrl).image.getScaledInstance(20, 20, Image.SCALE_SMOOTH))
        return JButton("Run Analysis", icon).apply {
            preferredSize = Dimension(300, 60)
            minimumSize = Dimension(300, 60)
            maximumSize = Dimension(300, 60)
            font = Font("Arial", Font.BOLD, 14)
            toolTipText = "Click to start anti-pattern analysis"
            addActionListener { runAnalysis() }
        }
    }

    private fun addOutputArea(panel: JPanel, gbc: GridBagConstraints) {
        gbc.gridx = 0
        gbc.gridy++
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        panel.add(JBScrollPane(outputArea).apply {
            border = JBUI.Borders.empty(10)
        }, gbc)
    }

    private fun runAnalysis() {
        val tempDir = Files.createTempDirectory("antipattern-analysis")
        val tempJarPath = tempDir.resolve("CodeOutput.jar")

        try {
            copyJarToTempFile(tempJarPath)
            val output = executeJar(tempJarPath)
            outputArea.text = "⁠  \n$output\n  ⁠"
        } catch (e: Exception) {
            outputArea.text = "Error: ${e.message}"
        } finally {
            cleanupTempFiles(tempJarPath, tempDir)
        }
    }

    private fun copyJarToTempFile(tempJarPath: java.nio.file.Path) {
        javaClass.getResourceAsStream(JAR_RESOURCE_PATH)?.use { inputStream ->
            Files.copy(inputStream, tempJarPath, StandardCopyOption.REPLACE_EXISTING)
        } ?: throw IllegalStateException("Could not find CodeOutput.jar in resources.")
    }

    private fun executeJar(jarPath: java.nio.file.Path): String {
        val process = Runtime.getRuntime().exec("java -jar $jarPath")
        return BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }
    }

    private fun cleanupTempFiles(jarPath: java.nio.file.Path, dirPath: java.nio.file.Path) {
        Files.deleteIfExists(jarPath)
        Files.deleteIfExists(dirPath)
    }
}