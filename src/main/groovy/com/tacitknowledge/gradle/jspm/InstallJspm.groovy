package com.tacitknowledge.gradle.jspm

import com.moowork.gradle.node.task.NpmTask

class InstallJspm extends NpmTask
{
  InstallJspm()
  {
    group = 'Jspm'
    description = 'Installs jspm bin (node) into project'

    def pkgName = project.jspm.version ? "jspm@${project.jspm.version}" : 'jspm'
    args = ['install', pkgName]

    project.afterEvaluate{
      workingDir = project.node.nodeModulesDir
      outputs.dir new File(project.node.nodeModulesDir, 'node_modules/jspm' )
    }
  }
}
