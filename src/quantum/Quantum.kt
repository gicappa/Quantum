package quantum

import java.awt.*
import java.awt.event.KeyEvent
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*

fun main(args: Array<String>) {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    SwingUtilities.invokeLater { QuantumApp().run() }
}

val javaKeywords = listOf("abstract", "continue", "for", "new", "switch",
        "assert", "default", "goto", "package", "synchronized",
        "boolean", "do", "if", "private", "this",
        "break", "double", "implements", "protected", "throw",
        "byte", "else", "import", "public", "throws",
        "case", "enum", "instanceof", "return", "transient",
        "catch", "extends", "int", "short", "try",
        "char", "final", "interface", "static", "void",
        "class", "finally", "long", "strictfp", "volatile",
        "const", "float", "native", "super", "while")

class QuantumApp : JPanel(GridBagLayout()) {
    companion object {
        const val APP_NAME = "Quantum"
    }

    val frame = JFrame(QuantumApp.APP_NAME)
    var editorArea: EditorArea
    var menuBar: JMenuBar
    var content: Content

    init {
        content = Content()
        editorArea = EditorArea(content)
        menuBar = createMenuBar()

        add(editorArea.pane, editorArea.constraints)
    }

    fun run() {
        // TODO Understand if the code to display the icon is needed
        // or if it's enough the packaging system
        //
        // val url = ClassLoader.getSystemResource("quantum/quantum.png")
        // val kit = Toolkit.getDefaultToolkit()
        // val img = kit.createImage(url)
        // Application.getApplication().dockIconImage = img
        // frame.iconImage = img

        frame.jMenuBar = menuBar
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.background = Color(33, 37, 43, 255)
        frame.add(this)
        frame.pack()
        frame.isVisible = true
        frame.setSize(600, 400)
    }

    fun createMenuBar(): JMenuBar {
        val bar = JMenuBar()
        bar.name = QuantumApp.APP_NAME
        val menu = JMenu("File")
        menu.add(createMenuItem("Open", KeyEvent.VK_O) { load() })
        menu.add(createMenuItem("Save", KeyEvent.VK_S) { save() })
        bar.add(menu)
        return bar
    }

    private fun createMenuItem(itemName: String, key: Int, function: (Any) -> Unit): JMenuItem {
        val item = JMenuItem(itemName)
        item.accelerator = KeyStroke.getKeyStroke(key,
                Toolkit.getDefaultToolkit().menuShortcutKeyMask)
        item.addActionListener(function)
        return item
    }

    fun load() {
        // TODO separate responsibility: dialog and loading should be separate
        val fileDialog = FileDialog(frame, "Open file", FileDialog.LOAD)

        fileDialog.directory = System.getProperty("user.home")
        fileDialog.isVisible = true
        fileDialog.file ?: return

        val d = fileDialog.directory ?: return
        val f = fileDialog.file ?: return
        val absFilename = File(d, f).absolutePath
        val path: Path = Paths.get(absFilename)
        Files.newBufferedReader(path, Charset.forName("UTF-8")).use { reader ->
            content.text = reader.readText()
        }
        // TODO doesn't work for file with a dot inside
        content.title = fileDialog.file.split(".").first()
        content.type = fileDialog.file.split(".").last()

        editorArea.update(content)
    }

    fun save() {
        // TODO separate responsibility: dialog and loading should be separate
        val fileDialog = FileDialog(frame, "Save file", FileDialog.SAVE)

        fileDialog.directory = System.getProperty("user.home")
        fileDialog.file = "${content.title}.${content.type}"
        fileDialog.isVisible = true

        val path: Path = Paths.get(fileDialog.file ?: return)
        Files.newBufferedWriter(path, Charset.forName("UTF-8")).use { writer ->
            writer.write(content.text, 0, content.text.length)
        }
    }
}

class Content(var title: String = "untitled",
              var type: String = "txt",
              var text: String = "") {
}

class EditorArea(var content: Content) {

    var editorTextArea: JTextPane
    var pane: JScrollPane
    var constraints: GridBagConstraints

    init {
        editorTextArea = createEditorTextPane()
        editorTextArea.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent) {
                update(e.document)
            }

            override fun insertUpdate(e: DocumentEvent) {
                update(e.document)
            }

            override fun removeUpdate(e: DocumentEvent) {
                update(e.document)
            }

            fun update(document: Document) {
                content.text = document.getText(0, document.length)
            }
        })
        pane = createScrollPane(editorTextArea)
        constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.BOTH
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        pane.autoscrolls = true
    }

    fun createScrollPane(component: Component): JScrollPane {
        val scrollPane = JScrollPane(component)
        scrollPane.border = BorderFactory.createEmptyBorder()
        return scrollPane
    }

    private fun createEditorTextPane(): JTextPane {
        val pane = JTextPane()
        pane.isEditable = true
        pane.background = Color(46, 46, 46, 255)
        pane.caret = createCaret()
        pane.caret.blinkRate = 350
        pane.autoscrolls = true
        pane.foreground = Color(157, 163, 176, 255)
        return pane
    }

    fun createCaret(): Caret {

        return object : DefaultCaret() {
            override fun paint(g: Graphics) {
                var comp: JTextComponent = component ?: return
                try {
                    val r = comp.modelToView(dot) ?: return
                    r.width = 3
                    g.color = Color(82, 139, 255, 255)
                    if (isVisible) g.fillRect(r.x, r.y, r.width, r.height)
                } catch (e: BadLocationException) {
                    return
                }
            }
        }
    }

    fun update(newContent: Content) {
        content = newContent
        editorTextArea.text = content.text
    }

}