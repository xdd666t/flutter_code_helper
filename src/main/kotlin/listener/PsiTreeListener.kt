package listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.util.castSafelyTo
import utils.FileGenerator
import utils.FileHelperNew
import java.util.*
import kotlin.concurrent.timerTask

class PsiTreeListener(private val project: Project) : PsiTreeChangeListener {
    override fun beforePropertyChange(event: PsiTreeChangeEvent) {
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        handleEvent(event)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        handleEvent(event)
    }

    override fun beforeChildAddition(event: PsiTreeChangeEvent) {
    }

    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        handleEvent(event)
    }

    override fun beforeChildrenChange(event: PsiTreeChangeEvent) {
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        handleEvent(event)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        handleEvent(event)
    }

    override fun beforeChildMovement(event: PsiTreeChangeEvent) {
    }

    override fun childAdded(event: PsiTreeChangeEvent) {
        handleEvent(event)
    }

    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {
    }

    private fun handleEvent(event: PsiTreeChangeEvent) {
        val folderList = FileHelperNew.getAutoFolder(project)
        if (folderList.isNotEmpty()) {
            val changeFile = event.child ?: return
            val psiDirectory = changeFile.parent.castSafelyTo<PsiDirectory>() ?: return
            // 统一数据
            folderList.forEachIndexed { index, value ->
                folderList[index] = if (value.endsWith("/")) value.removeSuffix("/") else value
            }
            val operatePath = psiDirectory.virtualFile.path
            for (folder in folderList) {
                if (operatePath.contains(folder)) {
                    //定义目录发生改变 这里延迟生成避免报错
                    Timer().schedule(timerTask {
                        ApplicationManager.getApplication().invokeLater {
                            FileGenerator(project).autoBuildYaml()
                        }
                    }, 300)
                }
            }
        }
    }
}