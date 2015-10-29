package com.tacitknowledge.gradle.jspm

import com.moowork.gradle.node.task.NodeTask

abstract class JspmTask extends NodeTask
{
  JspmTask()
  {
    group = "Jspm"

    project.afterEvaluate {
      workingDir = "${project.buildDir}/${project.jspm.buildDir}/jspm"
      script = project.file( new File(project.node.nodeModulesDir, 'node_modules/jspm/jspm.js') )
      
      inputs.file new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json")
    }
  }
}
