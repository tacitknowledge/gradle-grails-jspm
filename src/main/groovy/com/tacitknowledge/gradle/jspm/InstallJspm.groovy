package com.tacitknowledge.gradle.jspm

import com.moowork.gradle.node.npm.NpmTask

class InstallJspm extends NpmTask
{
  InstallJspm() {
    group = 'Jspm'
    description = 'Installs jspm bin (node) into project'

    project.afterEvaluate {
      def pkgName = project.jspm.version ? "jspm@${project.jspm.version}" : 'jspm'
      args = ['install', pkgName]

      workingDir = project.node.nodeModulesDir

      def jspmDir = new File(project.node.nodeModulesDir, 'node_modules/jspm')
      outputs.dir jspmDir
      enabled = !jspmDir.exists()
    }
  }
}
