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
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.swing.*
import com.fasterxml.jackson.databind.ObjectMapper

class AntiPatternToolWindowFactory : ToolWindowFactory, DumbAware {
    companion object {
        private const val JAR_RESOURCE_PATH = "/jar/anti-pattern-detector-1.0.jar"
    }

    private var tempDir: Path? = null

    private val outputArea = JTextArea(10, 50).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        selectionColor = JBColor.YELLOW
        border = JBUI.Borders.empty(10)
    }

    private lateinit var projectDirectoryField: JTextField
    private lateinit var thirdPartyMethodPathField: JTextField
    private lateinit var exclusionsField: JTextField
    private lateinit var snapshotCsvFilePathField: JTextField
    private lateinit var methodExecutionThresholdField: JTextField

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = JBUI.insets(10)
            fill = GridBagConstraints.HORIZONTAL
        }

        addLogoAndTitle(panel, gbc)
        addInfoLabel(panel, gbc)
        addConfigFields(panel, gbc)
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

    private fun addConfigFields(panel: JPanel, gbc: GridBagConstraints) {
        gbc.gridx = 0
        gbc.gridy++
        gbc.gridwidth = 2

        panel.add(JLabel("Project Directory:"), gbc)
        gbc.gridy++
        projectDirectoryField = JTextField()
        panel.add(projectDirectoryField, gbc)

        gbc.gridy++
        panel.add(JLabel("Third Party Method Path:"), gbc)
        gbc.gridy++
        thirdPartyMethodPathField = JTextField()
        panel.add(thirdPartyMethodPathField, gbc)

        gbc.gridy++
        panel.add(JLabel("Exclusions (comma-separated):"), gbc)
        gbc.gridy++
        exclusionsField = JTextField()
        panel.add(exclusionsField, gbc)

        gbc.gridy++
        panel.add(JLabel("Snapshot CSV File Path:"), gbc)
        gbc.gridy++
        snapshotCsvFilePathField = JTextField()
        panel.add(snapshotCsvFilePathField, gbc)

        gbc.gridy++
        panel.add(JLabel("Method Execution Threshold (ms):"), gbc)
        gbc.gridy++
        methodExecutionThresholdField = JTextField()
        panel.add(methodExecutionThresholdField, gbc)
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
        tempDir = Files.createTempDirectory("antipattern-analysis")
        val tempJarPath = tempDir?.resolve("anti-pattern-detector-1.0.jar") ?: run {
            println("Error: tempDir is null")
            return
        }

        try {
            copyJarToTempFile(tempJarPath)

            val configContent = createConfigContent()
            val output = executeJar(tempJarPath, configContent)
            outputArea.text = "⁠  \n$output\n  ⁠"
        } catch (e: Exception) {
            outputArea.text = "Error: ${e.message}"
        } finally {
            cleanupTempFiles()
        }
    }

    private fun copyJarToTempFile(tempJarPath: Path) {
        javaClass.getResourceAsStream(JAR_RESOURCE_PATH)?.use { inputStream ->
            Files.copy(inputStream, tempJarPath, StandardCopyOption.REPLACE_EXISTING)
        } ?: throw IllegalStateException("Could not find CodeOutput.jar in resources.")
    }

    private fun createConfigContent(): String {
        val threshold = methodExecutionThresholdField.text.toIntOrNull() ?: 2000
        val config = mapOf(
            "projectDirectory" to projectDirectoryField.text,
            "thirdPartyMethodPaths" to listOf(thirdPartyMethodPathField.text),
            "exclusions" to exclusionsField.text.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            "snapshotCsvFilePath" to snapshotCsvFilePathField.text,
            "methodExecutionThresholdMs" to threshold
        )
        return ObjectMapper().writeValueAsString(config)
    }

    private fun executeJar(jarPath: Path, configContent: String): String {
        val outputDir = jarPath.parent

        val process = ProcessBuilder("java", "-jar", jarPath.toString(), configContent)
            .directory(outputDir.toFile())
            .redirectErrorStream(true)
            .start()

        val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }

        process.waitFor()

        val jsonFile = outputDir.resolve("analysisOutput.json")
        return if (Files.exists(jsonFile)) {
            Files.readString(jsonFile)
        } else {
            "Output JSON file not found. Process output:\n$output"
        }
    }

    private fun cleanupTempFiles() {
        tempDir?.let { dir ->
            try {
                println("Attempting to delete directory: ${dir.toAbsolutePath()}")
                if (Files.exists(dir)) {
                    Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .forEach { path ->
                            println("Deleting: $path")
                            Files.deleteIfExists(path)
                        }
                    println("Directory deleted successfully")
                } else {
                    println("Directory does not exist: $dir")
                }
            } catch (e: IOException) {
                println("Error while deleting directory: ${e.message}")
                e.printStackTrace()
            }
        } ?: println("tempDir is null, nothing to clean up")
        tempDir = null
    }
}