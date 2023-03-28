package actions

import utils.FileGenerator
import utils.FileHelperNew.shouldActivateFor
import utils.PluginUtils.showNotify
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class GenerateAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)
        if (shouldActivateFor(project!!)) {
            FileGenerator(project).generateAll()
        } else {
            showNotify("This project is not the flutter project")
        }
    }
}